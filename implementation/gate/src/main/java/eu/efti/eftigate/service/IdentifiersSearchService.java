package eu.efti.eftigate.service;

import eu.efti.commons.dto.*;
import eu.efti.commons.dto.identifiers.api.IdentifierRequestResultDto;
import eu.efti.commons.enums.*;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.entity.IdentifiersRequestEntity;
import eu.efti.eftigate.mapper.MapperUtils;
import eu.efti.eftigate.repository.ControlRepository;
import eu.efti.eftigate.service.gate.EftiGateIdResolver;
import eu.efti.eftigate.service.request.IdentifiersRequestService;
import eu.efti.eftigate.utils.ControlUtils;
import eu.efti.eftilogger.model.ComponentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.efti.commons.enums.RequestStatusEnum.*;
import static eu.efti.commons.enums.StatusEnum.COMPLETE;
import static eu.efti.commons.enums.StatusEnum.PENDING;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class IdentifiersSearchService {

    private final ControlService controlService;
    private final EftiGateIdResolver eftiGateIdResolver;
    private final MapperUtils mapperUtils;
    private final LogManager logManager;
    private final ControlRepository controlRepository;
    private final IdentifiersRequestService identifiersRequestService;

    public IdentifiersResponseDto searchIdentifiers(SearchWithIdentifiersRequestDto searchRequestDto) {
        ControlDto controlDto = ControlUtils.fromLocalIdentifiersControl(searchRequestDto, RequestTypeEnum.LOCAL_IDENTIFIERS_SEARCH);
        String requestId = controlDto.getRequestId();

        Optional<ErrorDto> violation = controlService.validateDto(searchRequestDto);
        if (violation.isPresent()) {
            ControlService.updateControlWithError(controlDto, violation.get(), true);
            // This error and everything else should not be continued
        }

        logManager.logAppRequest(controlDto, searchRequestDto, ComponentType.CA_APP, ComponentType.GATE, LogManager.FTI_008_FTI_014);
        controlService.createIdentifiersControl(controlDto, searchRequestDto);

        CompletableFuture<IdentifiersResponseDto> processingFuture = CompletableFuture.supplyAsync(() -> waitForResponse(requestId));
        IdentifiersResponseDto response;

        try {
            response = processingFuture.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private IdentifiersResponseDto waitForResponse(String requestId) {
        StatusEnum controlStatus = getControlStatus(requestId);
        while (controlStatus.equals(StatusEnum.PENDING)) {
            log.info("Waiting for identifiers response for requestId: {}", requestId);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Thread interrupted while waiting for identifiers response", e);
                Thread.currentThread().interrupt();
            }
            controlStatus = getControlStatus(requestId);
        }
        return getIdentifiersResponse(requestId);
    }

    private StatusEnum getControlStatus(String requestId) {
        Optional<ControlEntity> controlEntity = controlRepository.findByRequestId(requestId);
        if (controlEntity.isPresent()) {
            return controlEntity.get().getStatus();
        } else {
            return StatusEnum.ERROR;
        }
    }

    public IdentifiersResponseDto getIdentifiersResponse(final String requestId) {
        final ControlDto controlDto = controlService.getControlByRequestId(requestId);
        final List<IdentifiersRequestEntity> requestEntities = identifiersRequestService.findAllForControlId(controlDto.getId());
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
