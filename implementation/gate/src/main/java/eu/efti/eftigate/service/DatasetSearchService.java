package eu.efti.eftigate.service;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.dto.ErrorDto;
import eu.efti.commons.dto.UilDto;
import eu.efti.commons.enums.ErrorCodesEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.RequestIdDto;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.utils.ControlUtils;
import eu.efti.eftilogger.model.ComponentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.efti.commons.enums.StatusEnum.PENDING;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class DatasetSearchService {

    private final GateProperties gateProperties;
    private final ControlService controlService;
    private final PlatformIntegrationService platformIntegrationService;
    private final LogManager logManager;

    public RequestIdDto getDataset(final UilDto uilDto) {
        boolean isLocal = gateProperties.isCurrentGate(uilDto.getGateId());
        ControlDto controlDto = ControlUtils.fromUilControl(
                uilDto,
                isLocal ? RequestTypeEnum.LOCAL_UIL_SEARCH : RequestTypeEnum.EXTERNAL_UIL_SEARCH
        );
        String requestId = controlDto.getRequestId();

        Optional<ErrorDto> violation = controlService.validateDto(uilDto);
        if (violation.isPresent()) {
            ControlService.updateControlWithError(controlDto, violation.get(), true);
            // This is an error and everything else should not be continued
        } else if (isLocal && !platformIntegrationService.platformExists(uilDto.getPlatformId())) {
            ControlService.updateControlWithError(
                    controlDto,
                    ErrorDto.fromErrorCode(ErrorCodesEnum.PLATFORM_ID_DOES_NOT_EXIST),
                    true
            );
            // This is an error and everything else should not be continued
        }

        logManager.logAppRequest(controlDto, uilDto, ComponentType.CA_APP, ComponentType.GATE, LogManager.FTI_008_FTI_014);
        controlService.createUilControl(controlDto);

        CompletableFuture<RequestIdDto> processingFuture = CompletableFuture.supplyAsync(
                () -> waitForResponse(requestId));
        RequestIdDto response;

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

    private RequestIdDto waitForResponse(final String requestId) {
        ControlEntity entity = controlService.getControlEntityByRequestId(requestId);
        while (entity.getStatus().equals(PENDING)) {
            log.info("Waiting for dataset response for requestId {}", requestId);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Thread interrupted while waiting for identifiers response", e);
                Thread.currentThread().interrupt();
            }
            controlService.updateControl(requestId);
            entity = controlService.getControlEntityByRequestId(requestId);
        }
        return buildResponse(controlService.getControlDtoByRequestId(requestId));
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
}
