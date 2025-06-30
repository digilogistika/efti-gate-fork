package eu.efti.eftigate.service;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.dto.ErrorDto;
import eu.efti.commons.dto.IdentifiersRequestDto;
import eu.efti.commons.dto.IdentifiersResponseDto;
import eu.efti.commons.dto.SearchWithIdentifiersRequestDto;
import eu.efti.commons.dto.identifiers.api.IdentifierRequestResultDto;
import eu.efti.commons.enums.RequestStatusEnum;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.commons.enums.StatusEnum;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.entity.IdentifiersRequestEntity;
import eu.efti.eftigate.exception.DtoValidationException;
import eu.efti.eftigate.mapper.MapperUtils;
import eu.efti.eftigate.service.gate.EftiGateIdResolver;
import eu.efti.eftigate.service.request.IdentifiersRequestService;
import eu.efti.eftigate.utils.ControlUtils;
import eu.efti.eftilogger.model.ComponentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static eu.efti.commons.enums.ErrorCodesEnum.IDENTIFIER_TOO_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentifiersSearchServiceTest {

    private static final String REQUEST_ID = "test-request-id";
    private static final String GATE_ID = "gate-01";
    private static final int CONTROL_ID = 1;

    @Mock
    private ControlService controlService;
    @Mock
    private EftiGateIdResolver eftiGateIdResolver;
    @Mock
    private MapperUtils mapperUtils;
    @Mock
    private LogManager logManager;
    @Mock
    private IdentifiersRequestService identifiersRequestService;

    @InjectMocks
    private IdentifiersSearchService identifiersSearchService;

    private SearchWithIdentifiersRequestDto validSearchRequest;

    @BeforeEach
    void setUp() {
        validSearchRequest = new SearchWithIdentifiersRequestDto();
        validSearchRequest.setEftiGateIndicator(List.of("PL"));
    }


    @Test
    void searchIdentifiers_whenValidRequest_shouldReturnCompleteResponse() {
        // ARRANGE
        final ControlDto controlledControlDto = ControlDto.builder()
                .requestId(REQUEST_ID)
                .requestType(RequestTypeEnum.LOCAL_IDENTIFIERS_SEARCH).build();

        try (MockedStatic<ControlUtils> mockedControlUtils = mockStatic(ControlUtils.class)) {
            mockedControlUtils.when(() -> ControlUtils.fromLocalIdentifiersControl(any(), any()))
                    .thenReturn(controlledControlDto);

            when(controlService.validateDto(validSearchRequest)).thenReturn(Optional.empty());
            doNothing().when(controlService).createIdentifiersControl(any(ControlDto.class), eq(validSearchRequest));

            final ControlEntity pendingEntity = new ControlEntity();
            pendingEntity.setStatus(StatusEnum.PENDING);
            pendingEntity.setRequestId(REQUEST_ID);
            pendingEntity.setRequests(List.of(new IdentifiersRequestEntity()));

            final ControlEntity completeEntity = new ControlEntity();
            completeEntity.setStatus(StatusEnum.COMPLETE);
            completeEntity.setRequestId(REQUEST_ID);

            when(controlService.getControlEntityByRequestId(REQUEST_ID))
                    .thenReturn(pendingEntity)
                    .thenReturn(completeEntity);

            final ControlDto completeControlDto = ControlDto.builder().id(CONTROL_ID).requestId(REQUEST_ID).status(StatusEnum.COMPLETE).build();
            when(controlService.getControlDtoByRequestId(REQUEST_ID)).thenReturn(completeControlDto);

            final IdentifiersRequestEntity requestEntity = new IdentifiersRequestEntity();
            final IdentifiersRequestDto requestDto = new IdentifiersRequestDto();
            requestDto.setStatus(RequestStatusEnum.SUCCESS);
            requestDto.setGateIdDest(GATE_ID);

            when(identifiersRequestService.findAllForControlId(CONTROL_ID)).thenReturn(List.of(requestEntity));
            when(mapperUtils.requestToRequestDto(requestEntity, IdentifiersRequestDto.class)).thenReturn(requestDto);
            when(eftiGateIdResolver.resolve(GATE_ID)).thenReturn("PL");

            // ACT
            final IdentifiersResponseDto response = identifiersSearchService.searchIdentifiers(validSearchRequest);

            // ASSERT
            assertNotNull(response);
            assertEquals(REQUEST_ID, response.getRequestId());
            assertEquals(StatusEnum.COMPLETE, response.getStatus());
            assertEquals(1, response.getIdentifiers().size());

            final IdentifierRequestResultDto resultDto = response.getIdentifiers().get(0);
            assertEquals("COMPLETE", resultDto.getStatus());
            assertEquals("PL", resultDto.getGateIndicator());

            verify(logManager).logAppRequest(any(ControlDto.class), eq(validSearchRequest), eq(ComponentType.CA_APP), eq(ComponentType.GATE), anyString());
            verify(controlService).createIdentifiersControl(any(ControlDto.class), eq(validSearchRequest));
            verify(controlService, times(1)).updateControl(REQUEST_ID);
        }
    }

    @Test
    void searchIdentifiers_whenValidationFails_shouldThrowDtoValidationException() {
        // ARRANGE
        final ErrorDto errorDto = ErrorDto.fromErrorCode(IDENTIFIER_TOO_LONG);
        when(controlService.validateDto(validSearchRequest)).thenReturn(Optional.of(errorDto));

        // ACT & ASSERT
        final DtoValidationException exception = assertThrows(DtoValidationException.class, () -> {
            identifiersSearchService.searchIdentifiers(validSearchRequest);
        });

        assertEquals("Identifier too long", exception.getMessage());

        verify(controlService, never()).createIdentifiersControl(any(), any());
        verify(logManager, never()).logAppRequest(any(), any(), any(), any(), any());
    }

    @Test
    void getIdentifiersResponse_whenControlHasError_shouldReturnResponseWithErrorFields() {
        // ARRANGE
        final ErrorDto errorDto = ErrorDto.fromErrorCode(IDENTIFIER_TOO_LONG);
        final ControlDto controlDtoWithError = ControlDto.builder()
                .id(CONTROL_ID)
                .requestId(REQUEST_ID)
                .status(StatusEnum.ERROR)
                .error(errorDto)
                .build();

        when(controlService.getControlDtoByRequestId(REQUEST_ID)).thenReturn(controlDtoWithError);
        when(identifiersRequestService.findAllForControlId(CONTROL_ID)).thenReturn(Collections.emptyList());

        // ACT
        final IdentifiersResponseDto response = identifiersSearchService.getIdentifiersResponse(REQUEST_ID);

        // ASSERT
        assertNotNull(response);
        assertNull(response.getRequestId(), "RequestID should be null when there is a control error");
        assertEquals(StatusEnum.ERROR, response.getStatus());
        assertEquals("IDENTIFIER_TOO_LONG", response.getErrorCode());
        assertEquals("Identifier too long", response.getErrorDescription());
        assertTrue(response.getIdentifiers().isEmpty());
    }

    @Test
    void getIdentifiersResponse_shouldLogWhenFromGateIdIsBlank() {
        // ARRANGE
        final ControlDto controlDto = ControlDto.builder()
                .id(CONTROL_ID)
                .requestId(REQUEST_ID)
                .status(StatusEnum.COMPLETE)
                .fromGateId(null)
                .build();
        when(controlService.getControlDtoByRequestId(REQUEST_ID)).thenReturn(controlDto);
        when(identifiersRequestService.findAllForControlId(CONTROL_ID)).thenReturn(Collections.emptyList());

        // ACT
        identifiersSearchService.getIdentifiersResponse(REQUEST_ID);

        // ASSERT
        verify(logManager).logFromIdentifier(any(IdentifiersResponseDto.class), eq(ComponentType.GATE), eq(ComponentType.CA_APP), eq(controlDto), anyString());
    }

    @Test
    void handleAsyncException_whenTimeout_shouldThrowRuntimeException() {
        // ARRANGE
        final TimeoutException timeoutException = new TimeoutException("Timed out");

        // ACT & ASSERT
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            // Using reflection to test the private method
            ReflectionTestUtils.invokeMethod(identifiersSearchService, "handleAsyncException", timeoutException);
        });
        assertEquals("Processing timed out after 60 seconds", exception.getMessage());
        assertEquals(timeoutException, exception.getCause());
    }

    @Test
    void handleAsyncException_whenInterrupted_shouldThrowRuntimeExceptionAndInterrupt() {
        // ARRANGE
        final InterruptedException interruptedException = new InterruptedException("Interrupted");

        // ACT & ASSERT
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ReflectionTestUtils.invokeMethod(identifiersSearchService, "handleAsyncException", interruptedException);
        });

        assertEquals("Processing was interrupted", exception.getMessage());
        assertEquals(interruptedException, exception.getCause());
        assertTrue(Thread.currentThread().isInterrupted(), "Thread should be interrupted");

        // Clean up the interrupted status for subsequent tests
        Thread.interrupted();
    }

    @Test
    void mapRequestStatus_shouldMapCorrectly() {

        // PENDING states
        assertEquals("PENDING", ReflectionTestUtils.invokeMethod(identifiersSearchService, "mapRequestStatus", RequestStatusEnum.RECEIVED));
        assertEquals("PENDING", ReflectionTestUtils.invokeMethod(identifiersSearchService, "mapRequestStatus", RequestStatusEnum.IN_PROGRESS));
        assertEquals("PENDING", ReflectionTestUtils.invokeMethod(identifiersSearchService, "mapRequestStatus", RequestStatusEnum.RESPONSE_IN_PROGRESS));

        // COMPLETE state
        assertEquals("COMPLETE", ReflectionTestUtils.invokeMethod(identifiersSearchService, "mapRequestStatus", RequestStatusEnum.SUCCESS));

        // ERROR states
        assertEquals("ERROR", ReflectionTestUtils.invokeMethod(identifiersSearchService, "mapRequestStatus", RequestStatusEnum.SEND_ERROR));
        assertEquals("ERROR", ReflectionTestUtils.invokeMethod(identifiersSearchService, "mapRequestStatus", RequestStatusEnum.ERROR));
    }
}
