package eu.efti.eftigate.service;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.dto.ErrorDto;
import eu.efti.commons.dto.UilDto;
import eu.efti.commons.enums.ErrorCodesEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.commons.enums.StatusEnum;
import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.DatasetDto;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.exception.DtoValidationException;
import eu.efti.eftigate.utils.ControlUtils;
import eu.efti.eftilogger.model.ComponentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetSearchServiceTest {

    private static final String REQUEST_ID = "test-request-id";
    private static final String GATE_ID = "gate-01";
    private static final String PLATFORM_ID = "platform-01";

    @Mock
    private GateProperties gateProperties;
    @Mock
    private ControlService controlService;
    @Mock
    private PlatformIntegrationService platformIntegrationService;
    @Mock
    private LogManager logManager;

    @InjectMocks
    private DatasetSearchService datasetSearchService;

    private UilDto uilDto;
    private ControlDto controlDto;
    private ControlEntity pendingEntity;

    @BeforeEach
    void setUp() {
        uilDto = new UilDto();
        uilDto.setGateId(GATE_ID);
        uilDto.setPlatformId(PLATFORM_ID);

        controlDto = ControlDto.builder().requestId(REQUEST_ID).gateId(GATE_ID).platformId(PLATFORM_ID).build();
        pendingEntity = new ControlEntity();
        pendingEntity.setStatus(StatusEnum.PENDING);
    }

    @Test
    void getDataset_whenLocalRequestAndSuccess_thenReturnDatasetDto() {
        // ARRANGE
        final byte[] eftiData = "eFTI data".getBytes();
        final ControlEntity foundEntity = new ControlEntity();
        foundEntity.setStatus(StatusEnum.COMPLETE);
        final ControlDto foundControlDto = ControlDto.builder()
                .requestId(REQUEST_ID)
                .status(StatusEnum.COMPLETE)
                .eftiData(eftiData)
                .build();

        when(gateProperties.isCurrentGate(GATE_ID)).thenReturn(true);
        when(controlService.validateDto(uilDto)).thenReturn(Optional.empty());
        when(platformIntegrationService.platformExists(PLATFORM_ID)).thenReturn(true);
        when(controlService.getControlEntityByRequestId(REQUEST_ID)).thenReturn(pendingEntity, foundEntity);
        when(controlService.getControlDtoByRequestId(REQUEST_ID)).thenReturn(foundControlDto);
        when(gateProperties.getOwner()).thenReturn(String.valueOf(ComponentType.GATE));

        try (MockedStatic<ControlUtils> controlUtils = Mockito.mockStatic(ControlUtils.class)) {
            controlUtils.when(() -> ControlUtils.fromUilControl(uilDto, RequestTypeEnum.LOCAL_UIL_SEARCH))
                    .thenReturn(controlDto);

            // ACT
            final DatasetDto result = datasetSearchService.getDataset(uilDto);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getRequestId()).isEqualTo(REQUEST_ID);
            assertThat(result.getStatus()).isEqualTo(StatusEnum.COMPLETE);
            assertThat(result.getData()).isEqualTo(eftiData);
            assertThat(result.getErrorCode()).isNull();
            assertThat(result.getErrorDescription()).isNull();

            verify(controlService).createUilControl(controlDto);
            verify(logManager).logAppRequest(eq(controlDto), eq(uilDto), eq(ComponentType.CA_APP), eq(ComponentType.GATE), anyString());
            verify(logManager).logAppResponse(eq(foundControlDto), eq(result), eq(ComponentType.GATE), eq(String.valueOf(ComponentType.GATE)), eq(ComponentType.CA_APP), eq(null), anyString());
        }
    }

    @Test
    void getDataset_whenExternalRequestAndSuccess_thenReturnDatasetDto() {
        // ARRANGE
        final ControlEntity foundEntity = new ControlEntity();
        foundEntity.setStatus(StatusEnum.COMPLETE);
        final ControlDto foundControlDto = ControlDto.builder().requestId(REQUEST_ID).status(StatusEnum.COMPLETE).build();

        when(gateProperties.isCurrentGate(GATE_ID)).thenReturn(false);
        when(controlService.validateDto(uilDto)).thenReturn(Optional.empty());
        when(controlService.getControlEntityByRequestId(REQUEST_ID)).thenReturn(pendingEntity, foundEntity);
        when(controlService.getControlDtoByRequestId(REQUEST_ID)).thenReturn(foundControlDto);

        try (MockedStatic<ControlUtils> controlUtils = Mockito.mockStatic(ControlUtils.class)) {
            controlUtils.when(() -> ControlUtils.fromUilControl(uilDto, RequestTypeEnum.EXTERNAL_UIL_SEARCH))
                    .thenReturn(controlDto);

            // ACT
            final DatasetDto result = datasetSearchService.getDataset(uilDto);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(StatusEnum.COMPLETE);
            verify(platformIntegrationService, never()).platformExists(anyString());
            verify(controlService).createUilControl(controlDto);
        }
    }

    @Test
    void getDataset_whenDtoIsInvalid_thenThrowDtoValidationException() {
        // ARRANGE
        final ErrorDto validationError = ErrorDto.fromErrorCode(ErrorCodesEnum.DATASET_ID_INCORRECT_FORMAT);
        validationError.setErrorDescription("Invalid Syntax");
        when(controlService.validateDto(uilDto)).thenReturn(Optional.of(validationError));

        try (
                MockedStatic<ControlUtils> controlUtils = Mockito.mockStatic(ControlUtils.class);
                MockedStatic<ControlService> controlServiceStatic = Mockito.mockStatic(ControlService.class)
        ) {
            controlUtils.when(() -> ControlUtils.fromUilControl(any(), any())).thenReturn(controlDto);

            // ACT & ASSERT
            DtoValidationException exception = assertThrows(DtoValidationException.class,
                    () -> datasetSearchService.getDataset(uilDto));

            assertThat(exception.getMessage()).isEqualTo("Invalid Syntax");
            controlServiceStatic.verify(() -> ControlService.updateControlWithError(controlDto, validationError, true));
            verify(controlService, never()).createUilControl(any());
            verify(logManager, never()).logAppRequest(any(), any(), any(), any(), any());
        }
    }

    @Test
    void getDataset_whenLocalPlatformDoesNotExist_thenThrowDtoValidationException() {
        // ARRANGE
        when(gateProperties.isCurrentGate(GATE_ID)).thenReturn(true);
        when(controlService.validateDto(uilDto)).thenReturn(Optional.empty());
        when(platformIntegrationService.platformExists(PLATFORM_ID)).thenReturn(false);

        try (
                MockedStatic<ControlUtils> controlUtils = Mockito.mockStatic(ControlUtils.class);
                MockedStatic<ControlService> controlServiceStatic = Mockito.mockStatic(ControlService.class)
        ) {
            controlUtils.when(() -> ControlUtils.fromUilControl(any(), any())).thenReturn(controlDto);
            controlServiceStatic.when(() -> ControlService.updateControlWithError(any(), any(), anyBoolean())).then(invocation -> null);


            // ACT & ASSERT
            DtoValidationException exception = assertThrows(DtoValidationException.class,
                    () -> datasetSearchService.getDataset(uilDto));

            assertThat(exception.getMessage()).isEqualTo("Platform id " + PLATFORM_ID + " doesn't exist");
            controlServiceStatic.verify(() -> ControlService.updateControlWithError(
                    eq(controlDto), any(ErrorDto.class), eq(true)));
        }
    }

    @Test
    void getDataset_whenSearchReturnsError_thenReturnDatasetDtoWithError() {
        // ARRANGE
        final ErrorDto errorDto = ErrorDto.fromErrorCode(ErrorCodesEnum.DATASET_ID_INCORRECT_FORMAT);
        final ControlEntity errorEntity = new ControlEntity();
        errorEntity.setStatus(StatusEnum.ERROR);
        final ControlDto errorControlDto = ControlDto.builder()
                .requestId(REQUEST_ID)
                .status(StatusEnum.ERROR)
                .error(errorDto)
                .build();

        when(gateProperties.isCurrentGate(GATE_ID)).thenReturn(true);
        when(controlService.validateDto(uilDto)).thenReturn(Optional.empty());
        when(platformIntegrationService.platformExists(PLATFORM_ID)).thenReturn(true);
        when(controlService.getControlEntityByRequestId(REQUEST_ID)).thenReturn(pendingEntity, errorEntity);
        when(controlService.getControlDtoByRequestId(REQUEST_ID)).thenReturn(errorControlDto);
        when(gateProperties.getOwner()).thenReturn(String.valueOf(ComponentType.GATE));


        try (MockedStatic<ControlUtils> controlUtils = Mockito.mockStatic(ControlUtils.class)) {
            controlUtils.when(() -> ControlUtils.fromUilControl(any(), any())).thenReturn(controlDto);

            // ACT
            final DatasetDto result = datasetSearchService.getDataset(uilDto);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getRequestId()).isEqualTo(REQUEST_ID);
            assertThat(result.getStatus()).isEqualTo(StatusEnum.ERROR);
            assertThat(result.getData()).isNull();
            assertThat(result.getErrorCode()).isEqualTo("DATASET_ID_INCORRECT_FORMAT");
            assertThat(result.getErrorDescription()).isEqualTo("datasetId format is incorrect.");

            verify(logManager).logAppResponse(any(), any(), any(), any(), any(), any(), any());
        }
    }

    @Test
    void getDataset_whenAsyncProcessingFails_thenThrowRuntimeException() {
        // ARRANGE
        final RuntimeException dbException = new RuntimeException("DB is down");
        when(gateProperties.isCurrentGate(GATE_ID)).thenReturn(true);
        when(controlService.validateDto(uilDto)).thenReturn(Optional.empty());
        when(platformIntegrationService.platformExists(PLATFORM_ID)).thenReturn(true);
        when(controlService.getControlEntityByRequestId(REQUEST_ID)).thenThrow(dbException);

        try (MockedStatic<ControlUtils> controlUtils = Mockito.mockStatic(ControlUtils.class)) {
            controlUtils.when(() -> ControlUtils.fromUilControl(any(), any())).thenReturn(controlDto);

            // ACT & ASSERT
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> datasetSearchService.getDataset(uilDto));

            assertThat(exception.getMessage()).isEqualTo("java.lang.RuntimeException: Processing failed");
            assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(exception.getCause().getCause())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("java.lang.RuntimeException: DB is down");
        }
    }
}
