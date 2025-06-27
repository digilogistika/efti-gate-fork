package eu.efti.eftigate.service;

import eu.efti.commons.dto.*;
import eu.efti.commons.dto.identifiers.api.IdentifierRequestResultDto;
import eu.efti.commons.enums.*;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.entity.IdentifiersRequestEntity;
import eu.efti.eftigate.exception.DtoValidationException;
import eu.efti.eftigate.mapper.MapperUtils;
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
    private final IdentifiersRequestService identifiersRequestService;

    public IdentifiersResponseDto searchIdentifiers(SearchWithIdentifiersRequestDto searchRequestDto) {
        ControlDto controlDto = ControlUtils.fromLocalIdentifiersControl(searchRequestDto, RequestTypeEnum.LOCAL_IDENTIFIERS_SEARCH);
        String requestId = controlDto.getRequestId();
        Optional<ErrorDto> violation = controlService.validateDto(searchRequestDto);
        if (violation.isPresent()) {
            ControlService.updateControlWithError(controlDto, violation.get(), true);
            throw new DtoValidationException(violation.get().getErrorDescription());
        }
        logManager.logAppRequest(controlDto, searchRequestDto, ComponentType.CA_APP, ComponentType.GATE, LogManager.FTI_008_FTI_014);
        controlService.createIdentifiersControl(controlDto, searchRequestDto);

        return CompletableFuture
                .supplyAsync(() -> waitForResponse(requestId, searchRequestDto.getEftiGateIndicator().size()))
                .orTimeout(60, TimeUnit.SECONDS)
                .exceptionally(this::handleAsyncException)
                .join();
    }


    private IdentifiersResponseDto waitForResponse(String requestId, int requestCount) {
        ControlEntity entity = controlService.getControlEntityByRequestId(requestId);
        while (entity.getStatus().equals(PENDING)) {
            log.info("Waiting for identifiers response for requestId: {}", requestId);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Thread interrupted while waiting for identifiers response", e);
                Thread.currentThread().interrupt();
            }
            if (entity.getRequests().size() == requestCount) {
                controlService.updateControl(requestId);
            }
            entity = controlService.getControlEntityByRequestId(requestId);
        }
        return getIdentifiersResponse(requestId);
    }

    public IdentifiersResponseDto getIdentifiersResponse(final String requestId) {
        final ControlDto controlDto = controlService.getControlDtoByRequestId(requestId);
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

    private IdentifiersResponseDto handleAsyncException(Throwable throwable) {
        if (throwable instanceof TimeoutException) {
            throw new RuntimeException("Processing timed out after 60 seconds", throwable);
        } else if (throwable instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing was interrupted", throwable);
        } else {
            throw new RuntimeException("Processing failed", throwable);
        }
    }
}
