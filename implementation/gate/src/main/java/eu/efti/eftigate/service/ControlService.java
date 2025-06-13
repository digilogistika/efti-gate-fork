package eu.efti.eftigate.service;

import eu.efti.commons.constant.EftiGateConstants;
import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.dto.ErrorDto;
import eu.efti.commons.dto.IdentifiersRequestDto;
import eu.efti.commons.dto.IdentifiersResponseDto;
import eu.efti.commons.dto.PostFollowUpRequestDto;
import eu.efti.commons.dto.RequestDto;
import eu.efti.commons.dto.SearchWithIdentifiersRequestDto;
import eu.efti.commons.dto.UilDto;
import eu.efti.commons.dto.ValidableDto;
import eu.efti.commons.dto.identifiers.ConsignmentDto;
import eu.efti.commons.dto.identifiers.api.IdentifierRequestResultDto;
import eu.efti.commons.enums.ErrorCodesEnum;
import eu.efti.commons.enums.RequestStatusEnum;
import eu.efti.commons.enums.RequestType;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.commons.enums.StatusEnum;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.NoteResponseDto;
import eu.efti.eftigate.dto.RequestIdDto;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.entity.ErrorEntity;
import eu.efti.eftigate.entity.IdentifiersRequestEntity;
import eu.efti.eftigate.exception.AmbiguousIdentifierException;
import eu.efti.eftigate.mapper.MapperUtils;
import eu.efti.eftigate.repository.ControlRepository;
import eu.efti.eftigate.service.gate.EftiGateIdResolver;
import eu.efti.eftigate.service.request.RequestService;
import eu.efti.eftigate.service.request.RequestServiceFactory;
import eu.efti.eftigate.utils.ControlUtils;
import eu.efti.eftilogger.model.ComponentType;
import eu.efti.identifiersregistry.service.IdentifiersService;
import eu.efti.v1.edelivery.IdentifierQuery;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static eu.efti.commons.enums.ErrorCodesEnum.ID_NOT_FOUND;
import static eu.efti.commons.enums.RequestStatusEnum.ERROR;
import static eu.efti.commons.enums.RequestStatusEnum.IN_PROGRESS;
import static eu.efti.commons.enums.RequestStatusEnum.RECEIVED;
import static eu.efti.commons.enums.RequestStatusEnum.RESPONSE_IN_PROGRESS;
import static eu.efti.commons.enums.RequestStatusEnum.SEND_ERROR;
import static eu.efti.commons.enums.RequestTypeEnum.EXTERNAL_ASK_IDENTIFIERS_SEARCH;
import static eu.efti.commons.enums.StatusEnum.COMPLETE;
import static eu.efti.commons.enums.StatusEnum.PENDING;
import static java.lang.String.format;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class ControlService {

    public static final String ERROR_REQUEST_ID_NOT_FOUND = "Error requestId not found.";
    public static final String NOTE_WAS_NOT_SENT = "note was not sent";
    private final ControlRepository controlRepository;
    private final EftiGateIdResolver eftiGateIdResolver;
    private final IdentifiersService identifiersService;
    private final MapperUtils mapperUtils;
    private final RequestServiceFactory requestServiceFactory;
    private final LogManager logManager;
    private final Function<List<String>, RequestTypeEnum> gateToRequestTypeFunction;
    private final EftiAsyncCallsProcessor eftiAsyncCallsProcessor;
    private final GateProperties gateProperties;
    private final SerializeUtils serializeUtils;
    private final PlatformIntegrationService platformIntegrationService;

    @Value("${efti.control.pending.timeout:60}")
    private Integer timeoutValue;

    private static void createErrorControl(final ControlDto controlDto, final ErrorDto error, final boolean resetUuid) {
        controlDto.setStatus(StatusEnum.ERROR);
        controlDto.setError(error);
        if (resetUuid) {
            controlDto.setRequestId(null);
        }
        log.error("{}, {}", error.getErrorDescription(), error.getErrorCode());
    }

    public ControlEntity getById(final long id) {
        final Optional<ControlEntity> controlEntity = controlRepository.findById(id);
        return controlEntity.orElse(null);
    }

    @Transactional("controlTransactionManager")
    public RequestIdDto createUilControl(final UilDto uilDto) {
        log.info("create Uil control for dataset id : {}", uilDto.getDatasetId());
        boolean isLocal = gateProperties.isCurrentGate(uilDto.getGateId());
        return createControl(uilDto, ControlUtils
                        .fromUilControl(uilDto, isLocal ? RequestTypeEnum.LOCAL_UIL_SEARCH : RequestTypeEnum.EXTERNAL_UIL_SEARCH),
                (dto) -> validateControl(dto)
                        .or(() -> isLocal ? (platformIntegrationService.platformExists(dto.getPlatformId()) ? Optional.empty() : Optional.of(ErrorDto.fromErrorCode(ErrorCodesEnum.PLATFORM_ID_DOES_NOT_EXIST))) : Optional.empty()));
    }

    public RequestIdDto createIdentifiersControl(final SearchWithIdentifiersRequestDto identifiersRequestDto) {
        log.info("create Consignment control for identifier : {}", identifiersRequestDto.getIdentifier());
        return createControl(identifiersRequestDto, ControlUtils.fromLocalIdentifiersControl(identifiersRequestDto, RequestTypeEnum.LOCAL_IDENTIFIERS_SEARCH), this::validateControl);
    }

    public NoteResponseDto createNoteRequestForControl(final PostFollowUpRequestDto postFollowUpRequestDto) {
        log.info("create Note Request for control with requestId : {}", postFollowUpRequestDto.getRequestId());
        final ControlDto savedControl = getControlByRequestId(postFollowUpRequestDto.getRequestId());
        final boolean isCurrentGate = gateProperties.isCurrentGate(savedControl.getGateId());
        final String receiver = isCurrentGate ? savedControl.getPlatformId() : savedControl.getGateId();
        //log FTI023
        logManager.logNoteReceiveFromAapMessage(savedControl, serializeUtils.mapObjectToBase64String(postFollowUpRequestDto), receiver, ComponentType.CA_APP, ComponentType.GATE, true, RequestTypeEnum.NOTE_SEND, LogManager.FTI_023);
        if (savedControl.isFound()) {
            log.info("sending note to platform {}", savedControl.getPlatformId());
            return createNoteRequestForControl(
                    savedControl,
                    postFollowUpRequestDto,
                    dto -> validateControl(dto)
                            .or(() -> isCurrentGate ? (platformIntegrationService.platformExists(savedControl.getPlatformId()) ? Optional.empty() : Optional.of(ErrorDto.fromErrorCode(ErrorCodesEnum.PLATFORM_ID_DOES_NOT_EXIST))) : Optional.empty()));
        } else {
            return new NoteResponseDto(NOTE_WAS_NOT_SENT, ID_NOT_FOUND.name(), ID_NOT_FOUND.getMessage());
        }
    }

    private NoteResponseDto createNoteRequestForControl(final ControlDto controlDto, final PostFollowUpRequestDto notesDto, Function<PostFollowUpRequestDto, Optional<ErrorDto>> validate) {
        final Optional<ErrorDto> errorOptional = validate.apply(notesDto);
        final boolean isCurrentGate = gateProperties.isCurrentGate(controlDto.getGateId());
        final String receiver = isCurrentGate ? controlDto.getPlatformId() : controlDto.getGateId();
        if (errorOptional.isPresent()) {
            final ErrorDto errorDto = errorOptional.get();
            controlDto.setError(errorDto);
            //log fti025 not sent
            logManager.logNoteReceiveFromAapMessage(controlDto, serializeUtils.mapObjectToBase64String(notesDto), receiver, ComponentType.GATE, ComponentType.PLATFORM, false, isCurrentGate ? RequestTypeEnum.NOTE_SEND : RequestTypeEnum.EXTERNAL_NOTE_SEND, isCurrentGate ? LogManager.FTI_025 : LogManager.FTI_026);
            log.error("Not was not send : {}", errorDto.getErrorDescription());
            return new NoteResponseDto(NOTE_WAS_NOT_SENT, errorDto.getErrorCode(), errorDto.getErrorDescription());
        } else {
            controlDto.setNotes(notesDto.getMessage());
            getRequestService(RequestTypeEnum.NOTE_SEND).createAndSendRequest(controlDto, !gateProperties.isCurrentGate(controlDto.getGateId()) ? controlDto.getGateId() : null);
            //log fti025
            logManager.logNoteReceiveFromAapMessage(controlDto, serializeUtils.mapObjectToBase64String(notesDto), receiver, ComponentType.GATE, ComponentType.PLATFORM, true, isCurrentGate ? RequestTypeEnum.NOTE_SEND : RequestTypeEnum.EXTERNAL_NOTE_SEND, isCurrentGate ? LogManager.FTI_025 : LogManager.FTI_026);
            log.info("Note has been registered for control with request uuid '{}'", controlDto.getRequestId());
            return NoteResponseDto.builder().message("Note sent").build();
        }
    }

    private Optional<ErrorDto> validateControl(final ValidableDto validable) {
        final Validator validator;
        try (final ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        final Set<ConstraintViolation<ValidableDto>> violations = validator.validate(validable);

        if (violations.isEmpty()) {
            return Optional.empty();
        }

        //we manage only one error by control
        final ConstraintViolation<ValidableDto> constraintViolation = violations.iterator().next();

        return Optional.of(ErrorDto.fromErrorCode(ErrorCodesEnum.valueOf(constraintViolation.getMessage())));
    }

    public ControlDto getControlByRequestId(final String requestId) {
        log.info("get ControlEntity with request id : {}", requestId);
        final Optional<ControlEntity> optionalControlEntity = getByRequestId(requestId);
        if (optionalControlEntity.isPresent()) {
            return mapperUtils.controlEntityToControlDto(optionalControlEntity.get());
        } else {
            return buildNotFoundControlEntity();
        }
    }

    public Optional<ControlEntity> getByRequestId(final String requestId) {
        return controlRepository.findByRequestId(requestId);
    }

    public ControlDto updateControl(final String requestId) {
        log.info("get ControlEntity with request id : {}", requestId);
        final Optional<ControlEntity> optionalControlEntity = getByRequestId(requestId);
        if (optionalControlEntity.isPresent()) {
            return updatePendingControl(optionalControlEntity.get());
        } else {
            return buildNotFoundControlEntity();
        }
    }

    public ControlDto updatePendingControl(final ControlEntity controlEntity) {
        if (hasRequestInProgress(controlEntity)) {
            return mapperUtils.controlEntityToControlDto(controlEntity);
        }
        final RequestService<?> requestService = this.getRequestService(controlEntity.getRequestType());
        final boolean allRequestsContainsData = requestService.allRequestsContainsData(controlEntity.getRequests());
        if (allRequestsContainsData) {
            controlEntity.setStatus(StatusEnum.COMPLETE);
            return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
        } else {
            return handleExistingControlWithoutData(controlEntity);
        }
    }

    private boolean hasRequestInProgress(final ControlEntity controlEntity) {
        return getSecondsSinceCreation(controlEntity) < timeoutValue &&
                CollectionUtils.emptyIfNull(controlEntity.getRequests()).stream().anyMatch(request -> EftiGateConstants.IN_PROGRESS_STATUS.contains(request.getStatus()));
    }

    private long getSecondsSinceCreation(final ControlEntity controlEntity) {
        return ChronoUnit.SECONDS.between(controlEntity.getCreatedDate(), LocalDateTime.now());
    }

    private ControlDto handleExistingControlWithoutData(final ControlEntity controlEntity) {
        if (hasRequestInError(controlEntity)) {
            controlEntity.setStatus(StatusEnum.ERROR);
        } else if (shouldSetTimeoutTo(controlEntity)) {
            controlEntity.setStatus(StatusEnum.TIMEOUT);
            updateControlRequestsWithTimeoutStatus(controlEntity);
        } else if (PENDING.equals(controlEntity.getStatus())) {
            controlEntity.setStatus(StatusEnum.COMPLETE);
        }
        return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
    }

    private boolean shouldSetTimeoutTo(ControlEntity controlEntity) {
        return getSecondsSinceCreation(controlEntity) > timeoutValue &&
                CollectionUtils.emptyIfNull(controlEntity.getRequests())
                        .stream()
                        .anyMatch(request -> EftiGateConstants.IN_PROGRESS_STATUS.contains(request.getStatus()));
    }

    private void updateControlRequestsWithTimeoutStatus(final ControlEntity controlEntity) {
        controlEntity.getRequests().stream()
                .filter(request -> IN_PROGRESS.equals(request.getStatus()))
                .toList()
                .forEach(request -> {
                    request.setStatus(RequestStatusEnum.TIMEOUT);
                    final String requestType = request.getRequestType();
                    final RequestDto requestDto = mapperUtils.requestToRequestDto(request, EftiGateConstants.REQUEST_TYPE_CLASS_MAP.get(RequestType.valueOf(requestType)));
                    final RequestService<?> requestService = requestServiceFactory.getRequestServiceByRequestType(requestType);
                    requestService.save(requestDto);
                    if (controlEntity.isExternalAsk()) {
                        requestService.notifyTimeout(requestDto);
                    }
                });
    }

    private boolean hasRequestInError(final ControlEntity controlEntity) {
        return CollectionUtils.emptyIfNull(controlEntity.getRequests())
                .stream()
                .anyMatch(requestEntity -> RequestStatusEnum.ERROR == requestEntity.getStatus());
    }

    private RequestService<?> getRequestService(final RequestTypeEnum requestType) {
        return requestServiceFactory.getRequestServiceByRequestType(requestType);
    }

    private ControlDto buildNotFoundControlEntity() {
        return mapperUtils.controlEntityToControlDto(ControlEntity.builder()
                .status(StatusEnum.ERROR)
                .error(buildErrorEntity(ID_NOT_FOUND.name())).build());
    }

    private static ErrorEntity buildErrorEntity(final String errorCode) {
        return ErrorEntity.builder()
                .errorCode(errorCode)
                .errorDescription(ERROR_REQUEST_ID_NOT_FOUND).build();
    }

    public ControlDto createControlFrom(final IdentifierQuery identifierQuery, final String fromGateId) {
        ControlDto controlDto = ControlUtils.fromExternalIdentifiersControl(identifierQuery, EXTERNAL_ASK_IDENTIFIERS_SEARCH, fromGateId, gateProperties.getOwner());
        return this.save(controlDto);
        //should we save ?
    }

    public ControlDto save(final ControlDto controlDto) {
        return this.save(mapperUtils.controlDtoToControlEntity(controlDto));
    }

    public ControlDto save(final ControlEntity controlEntity) {
        return mapperUtils.controlEntityToControlDto(controlRepository.save(controlEntity));
    }

    private <T extends ValidableDto> RequestIdDto createControl(final T searchDto, final ControlDto controlDto, Function<T, Optional<ErrorDto>> validate) {
        validate.apply(searchDto).ifPresentOrElse(
                error -> createErrorControl(controlDto, error, true),
                () -> createControlFromType(searchDto, controlDto));
        return buildResponse(controlDto);
    }

    public void createUilControl(final ControlDto controlDto) {
        if (gateProperties.isCurrentGate(controlDto.getGateId()) && !checkOnLocalRegistry(controlDto)) {
            createErrorControl(controlDto, ErrorDto.fromErrorCode(ErrorCodesEnum.DATA_NOT_FOUND_ON_REGISTRY), false);
            final ControlDto savedControl = this.save(controlDto);
            //respond with the error
            if (controlDto.isExternalAsk()) {
                getRequestService(controlDto.getRequestType()).createAndSendRequest(savedControl, controlDto.getFromGateId(), RequestStatusEnum.ERROR);
            }
        } else {
            final ControlDto saveControl = this.save(controlDto);
            getRequestService(controlDto.getRequestType()).createAndSendRequest(saveControl, null);
            log.info("Uil control with request uuid '{}' has been register", saveControl.getRequestId());
        }
    }

    private boolean checkOnLocalRegistry(final ControlDto controlDto) {
        log.info("checking local registry for dataUuid {}", controlDto.getDatasetId());
        //log fti015
        logManager.logRequestRegistry(controlDto, null, ComponentType.GATE, ComponentType.REGISTRY, LogManager.FTI_015);
        final ConsignmentDto consignmentDto = this.identifiersService.findByUIL(controlDto.getDatasetId(), controlDto.getGateId(), controlDto.getPlatformId());
        //log fti016
        logManager.logRequestRegistry(controlDto, serializeUtils.mapObjectToBase64String(consignmentDto), ComponentType.REGISTRY, ComponentType.GATE, LogManager.FTI_016);
        return consignmentDto != null;
    }

    private void createIdentifiersControl(final ControlDto controlDto, final SearchWithIdentifiersRequestDto searchWithIdentifiersRequestDto) {
        final List<String> destinationGatesUrls = eftiGateIdResolver.resolve(searchWithIdentifiersRequestDto);

        controlDto.setRequestType(gateToRequestTypeFunction.apply(destinationGatesUrls));
        final ControlDto saveControl = this.save(controlDto);
        CollectionUtils.emptyIfNull(destinationGatesUrls).forEach(destinationUrl -> {
            if (StringUtils.isBlank(destinationUrl)) {
                getRequestService(saveControl.getRequestType()).createRequest(saveControl, RequestStatusEnum.ERROR);
            } else if (destinationUrl.equalsIgnoreCase(gateProperties.getOwner())) {
                eftiAsyncCallsProcessor.checkLocalRepoAsync(searchWithIdentifiersRequestDto, saveControl);
            } else {
                getRequestService(saveControl.getRequestType()).createAndSendRequest(saveControl, destinationUrl);
            }
        });
        log.info("Identifier control with request uuid '{}' has been register", saveControl.getRequestId());
    }

    private <T extends ValidableDto> void createControlFromType(final T searchDto, final ControlDto controlDto) {
        logManager.logAppRequest(controlDto, searchDto, ComponentType.CA_APP, ComponentType.GATE, LogManager.FTI_008_FTI_014);
        if (searchDto instanceof UilDto) {
            createUilControl(controlDto);
        } else if (searchDto instanceof final SearchWithIdentifiersRequestDto searchWithIdentifiersRequestDto) {
            createIdentifiersControl(controlDto, searchWithIdentifiersRequestDto);
        }
    }

    public RequestIdDto getControlEntity(final String requestId) {
        final ControlDto controlDto = getControlByRequestId(requestId);
        return buildResponse(controlDto);
    }

    private RequestIdDto buildResponse(final ControlDto controlDto) {
        final RequestIdDto result = RequestIdDto.builder()
                .requestId(controlDto.getRequestId())
                .status(controlDto.getStatus())
                .data(controlDto.getEftiData()).build();
        if (controlDto.isError() && controlDto.getError() != null) {
            result.setErrorDescription(controlDto.getError().getErrorDescription());
            result.setErrorCode(controlDto.getError().getErrorCode());
        }
        if (controlDto.getStatus() != PENDING) { // pending request are not logged
            logManager.logAppResponse(controlDto, result, ComponentType.GATE, gateProperties.getOwner(), ComponentType.CA_APP, null, LogManager.FTI_011_FTI_017);
        }
        return result;
    }

    public int updatePendingControls() {
        final List<ControlEntity> pendingControls = controlRepository.findByCriteria(PENDING, timeoutValue);
        CollectionUtils.emptyIfNull(pendingControls).forEach(this::updatePendingControl);
        return CollectionUtils.isNotEmpty(pendingControls) ? pendingControls.size() : 0;
    }

    public IdentifiersResponseDto getIdentifiersResponse(final String requestId) {
        final ControlDto controlDto = getControlByRequestId(requestId);
        final List<IdentifiersRequestEntity> requestEntities = requestServiceFactory.getRequestServiceByRequestType(RequestType.IDENTIFIER.name()).findAllForControlId(controlDto.getId());
        final List<IdentifiersRequestDto> requestDtos = requestEntities.stream().map(r -> mapperUtils.requestToRequestDto(r, IdentifiersRequestDto.class)).toList();
        return buildIdentifiersResponse(controlDto, requestDtos);
    }

    private IdentifiersResponseDto buildIdentifiersResponse(final ControlDto controlDto, final List<IdentifiersRequestDto> requestDtos) {
        final IdentifiersResponseDto result = IdentifiersResponseDto.builder()
                .requestId(controlDto.getRequestId())
                .status(controlDto.getStatus())
                .identifiers(getIdentifiersResultDtos(requestDtos))
                .build();
        if (controlDto.isError() && controlDto.getError() != null) {
            result.setRequestId(null);
            result.setErrorDescription(controlDto.getError().getErrorDescription());
            result.setErrorCode(controlDto.getError().getErrorCode());
        }

        if (StringUtils.isBlank(controlDto.getFromGateId())) {
            //log fti017
            logManager.logFromIdentifier(result, ComponentType.GATE, ComponentType.CA_APP, controlDto, LogManager.FTI_011_FTI_017);
        }
        return result;
    }

    private List<IdentifierRequestResultDto> getIdentifiersResultDtos(final List<IdentifiersRequestDto> requestDtos) {
        final List<IdentifierRequestResultDto> identifierResultDtos = new LinkedList<>();
        requestDtos.forEach(requestDto -> identifierResultDtos.add(
                IdentifierRequestResultDto.builder()
                        .consignments(requestDto.getIdentifiersResults() != null ? mapperUtils.consignmentDtoToApiDto(requestDto.getIdentifiersResults().getConsignments()) : Collections.emptyList())
                        .errorCode(requestDto.getError() != null ? requestDto.getError().getErrorCode() : null)
                        .errorDescription(requestDto.getError() != null ? requestDto.getError().getErrorDescription() : null)
                        .gateIndicator(eftiGateIdResolver.resolve(requestDto.getGateIdDest()))
                        .status(mapRequestStatus(requestDto.getStatus()))
                        .build())
        );
        return identifierResultDtos;
    }

    public void setError(final ControlDto controlDto, final ErrorDto errorDto) {
        controlDto.setStatus(StatusEnum.ERROR);
        controlDto.setError(errorDto);
        this.save(controlDto);
    }

    public ControlDto updateControlStatus(final ControlDto controlDto, final StatusEnum status) {
        controlDto.setStatus(status);
        return this.save(controlDto);
    }

    public ControlEntity getControlForCriteria(final String requestId, final RequestStatusEnum requestStatus) {
        Preconditions.checkArgument(requestId != null, "Request Uuid must not be null");
        final List<ControlEntity> controls = controlRepository.findByCriteria(requestId, requestStatus);
        if (CollectionUtils.isNotEmpty(controls)) {
            if (controls.size() > 1) {
                throw new AmbiguousIdentifierException(format("Control with request uuid '%s', and request with status '%s' is not unique, %d controls found!", requestId, requestStatus, controls.size()));
            } else {
                return controls.get(0);
            }
        }
        return null;
    }

    public Optional<ControlEntity> findByRequestId(final String controlRequestId) {
        return controlRepository.findByRequestId(controlRequestId);
    }

    private String mapRequestStatus(final RequestStatusEnum requestStatus) {
        if (List.of(RECEIVED, IN_PROGRESS, RESPONSE_IN_PROGRESS).contains(requestStatus)) {
            return PENDING.name();
        } else if (RequestStatusEnum.SUCCESS.equals(requestStatus)) {
            return COMPLETE.name();
        } else if (List.of(SEND_ERROR, ERROR).contains(requestStatus)) {
            return StatusEnum.ERROR.name();
        }
        return requestStatus.name();
    }
}
