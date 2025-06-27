package eu.efti.eftigate.service;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.dto.ErrorDto;
import eu.efti.commons.dto.UilDto;
import eu.efti.commons.enums.ErrorCodesEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.DatasetDto;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.exception.DtoValidationException;
import eu.efti.eftigate.utils.ControlUtils;
import eu.efti.eftilogger.model.ComponentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

    public DatasetDto getDataset(final UilDto uilDto) {
        boolean isLocal = gateProperties.isCurrentGate(uilDto.getGateId());
        ControlDto controlDto = ControlUtils.fromUilControl(
                uilDto,
                isLocal ? RequestTypeEnum.LOCAL_UIL_SEARCH : RequestTypeEnum.EXTERNAL_UIL_SEARCH
        );
        String requestId = controlDto.getRequestId();

        Optional<ErrorDto> violation = controlService.validateDto(uilDto);
        if (violation.isPresent()) {
            ControlService.updateControlWithError(controlDto, violation.get(), true);
            throw new DtoValidationException(violation.get().getErrorDescription());
        } else if (isLocal && !platformIntegrationService.platformExists(uilDto.getPlatformId())) {
            ControlService.updateControlWithError(
                    controlDto,
                    ErrorDto.fromErrorCode(ErrorCodesEnum.PLATFORM_ID_DOES_NOT_EXIST),
                    true
            );
            throw new DtoValidationException("Platform id " + uilDto.getPlatformId() + " doesn't exist");
        }

        logManager.logAppRequest(controlDto, uilDto, ComponentType.CA_APP, ComponentType.GATE, LogManager.FTI_008_FTI_014);
        controlService.createUilControl(controlDto);

        return CompletableFuture
                .supplyAsync(() -> waitForResponse(requestId))
                .orTimeout(60, TimeUnit.SECONDS)
                .exceptionally(this::handleAsyncException)
                .join();
    }

    private DatasetDto waitForResponse(final String requestId) {
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

    private DatasetDto buildResponse(final ControlDto controlDto) {
        final DatasetDto result = DatasetDto.builder()
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

    private DatasetDto handleAsyncException(Throwable throwable) {
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
