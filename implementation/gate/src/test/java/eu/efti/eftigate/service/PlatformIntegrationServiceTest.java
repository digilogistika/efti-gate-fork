package eu.efti.eftigate.service;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.commons.exception.TechnicalException;
import eu.efti.eftigate.dto.RabbitRequestDto;
import eu.efti.eftigate.repository.PlatformRepository;
import eu.efti.eftigate.service.request.NotesRequestService;
import eu.efti.eftigate.service.request.UilRequestService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformIntegrationService Tests")
class PlatformIntegrationServiceTest {

    private static final String PLATFORM_ID = "test-platform-id";
    private static final String REQUEST_ID = "test-request-id";
    private static final String DATASET_ID = "test-dataset-id";
    private static final String NOTE_CONTENT = "Test note content";
    @Mock
    private UilRequestService uilRequestService;
    @Mock
    private NotesRequestService notesRequestService;
    @Mock
    private PlatformApiService platformApiService;
    @Mock
    private DomibusIntegrationService domibusIntegrationService;
    @Mock
    private PlatformRepository platformRepository;
    @InjectMocks
    private PlatformIntegrationService platformIntegrationService;

    @Nested
    @DisplayName("handle() method tests")
    class HandleMethodTests {

        private RabbitRequestDto rabbitRequestDto;
        private ControlDto controlDto;
        private SupplyChainConsignment mockConsignment;

        @BeforeEach
        void setUp() {
            rabbitRequestDto = new RabbitRequestDto();
            controlDto = new ControlDto();
            controlDto.setPlatformId(PLATFORM_ID);
            controlDto.setRequestId(REQUEST_ID);
            controlDto.setDatasetId(DATASET_ID);
            controlDto.setSubsetIds(List.of("subset1", "subset2"));

            mockConsignment = mock(SupplyChainConsignment.class);

            // Default setup - platform exists
            when(platformRepository.existsByPlatformId(PLATFORM_ID)).thenReturn(true);
        }


        @Test
        @DisplayName("Should throw IllegalArgumentException when platform does not exist")
        void shouldThrowIllegalArgumentExceptionWhenPlatformDoesNotExist() {
            // Given
            when(platformRepository.existsByPlatformId(PLATFORM_ID)).thenReturn(false);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> platformIntegrationService.handle(rabbitRequestDto, controlDto, Optional.empty()));

            assertEquals("platform " + PLATFORM_ID + " does not exist", exception.getMessage());
        }

        @Nested
        @DisplayName("REST API request handling")
        class RestApiRequestHandling {

            @Test
            @DisplayName("Should handle LOCAL_UIL_SEARCH request type successfully")
            void shouldHandleLocalUilSearchRequestSuccessfully() throws Exception {
                // Given
                controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH);
                when(platformApiService.callGetConsignmentSubsets(eq(PLATFORM_ID), eq(DATASET_ID), any(Set.class)))
                        .thenReturn(mockConsignment);

                // When
                platformIntegrationService.handle(rabbitRequestDto, controlDto, Optional.empty());

                // Then
                verify(uilRequestService).manageRestRequestInProgress(REQUEST_ID);
                verify(platformApiService).callGetConsignmentSubsets(eq(PLATFORM_ID), eq(DATASET_ID), any(Set.class));
                verify(uilRequestService).manageRestResponseReceived(REQUEST_ID, mockConsignment);
            }

            @Test
            @DisplayName("Should handle NOTE_SEND request type successfully")
            void shouldHandleNoteSendRequestSuccessfully() throws Exception {
                // Given
                controlDto.setRequestType(RequestTypeEnum.NOTE_SEND);
                Optional<String> note = Optional.of(NOTE_CONTENT);

                // When
                platformIntegrationService.handle(rabbitRequestDto, controlDto, note);

                // Then
                verify(notesRequestService).manageRestRequestInProgress(REQUEST_ID);
                verify(platformApiService).callPostConsignmentFollowup(PLATFORM_ID, DATASET_ID, NOTE_CONTENT);
                verify(notesRequestService).manageRestRequestDone(REQUEST_ID);
            }

            @Test
            @DisplayName("Should throw RuntimeException when NOTE_SEND has no note")
            void shouldThrowRuntimeExceptionWhenNoteSendHasNoNote() {
                // Given
                controlDto.setRequestType(RequestTypeEnum.NOTE_SEND);

                // When & Then
                assertThrows(RuntimeException.class,
                        () -> platformIntegrationService.handle(rabbitRequestDto, controlDto, Optional.empty()));
            }

            @Test
            @DisplayName("Should handle EXTERNAL_ASK_UIL_SEARCH request type successfully")
            void shouldHandleExternalAskUilSearchRequestSuccessfully() throws Exception {
                // Given
                controlDto.setRequestType(RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH);
                when(platformApiService.callGetConsignmentSubsets(eq(PLATFORM_ID), eq(DATASET_ID), any(Set.class)))
                        .thenReturn(mockConsignment);

                // When
                platformIntegrationService.handle(rabbitRequestDto, controlDto, Optional.empty());

                // Then
                verify(uilRequestService).manageRestRequestInProgress(REQUEST_ID);
                verify(platformApiService).callGetConsignmentSubsets(eq(PLATFORM_ID), eq(DATASET_ID), any(Set.class));
                verify(uilRequestService).manageRestResponseReceived(REQUEST_ID, mockConsignment);
            }

            @Test
            @DisplayName("Should handle EXTERNAL_NOTE_SEND request type successfully")
            void shouldHandleExternalNoteSendRequestSuccessfully() throws Exception {
                // Given
                controlDto.setRequestType(RequestTypeEnum.EXTERNAL_NOTE_SEND);
                Optional<String> note = Optional.of(NOTE_CONTENT);

                // When
                platformIntegrationService.handle(rabbitRequestDto, controlDto, note);

                // Then
                verify(platformApiService).callPostConsignmentFollowup(PLATFORM_ID, DATASET_ID, NOTE_CONTENT);
                verifyNoInteractions(notesRequestService);
            }

            @Test
            @DisplayName("Should throw TechnicalException for unexpected request type")
            void shouldThrowTechnicalExceptionForUnexpectedRequestType() {
                // Given - Using a request type that's not handled in the switch
                controlDto.setRequestType(RequestTypeEnum.LOCAL_IDENTIFIERS_SEARCH);

                // When & Then
                assertThrows(TechnicalException.class,
                        () -> platformIntegrationService.handle(rabbitRequestDto, controlDto, Optional.empty()));
            }

            @Test
            @DisplayName("Should throw RuntimeException when PlatformIntegrationServiceException occurs")
            void shouldThrowRuntimeExceptionWhenPlatformIntegrationServiceExceptionOccurs() throws Exception {
                // Given
                controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH);
                PlatformIntegrationServiceException platformException =
                        new PlatformIntegrationServiceException("Platform API error", new RuntimeException("API failure"));
                when(platformApiService.callGetConsignmentSubsets(eq(PLATFORM_ID), eq(DATASET_ID), any(Set.class)))
                        .thenThrow(platformException);

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> platformIntegrationService.handle(rabbitRequestDto, controlDto, Optional.empty()));

                assertEquals(platformException, exception.getCause());
            }
        }

        @Nested
        @DisplayName("Domibus integration (when platformUsesRestApi is false)")
        class DomibusIntegrationTests {

            // Note: The current implementation always uses REST API (platformUsesRestApi = true)
            // These tests would be relevant if the logic for determining platformUsesRestApi changes

            @Test
            @DisplayName("Should verify current implementation always uses REST API")
            void shouldVerifyCurrentImplementationAlwaysUsesRestApi() {
                // Given
                controlDto.setRequestType(RequestTypeEnum.LOCAL_UIL_SEARCH);

                // When
                platformIntegrationService.handle(rabbitRequestDto, controlDto, Optional.empty());

                // Then
                // Verify that domibus integration is not called (REST API is used instead)
                verifyNoInteractions(domibusIntegrationService);
            }
        }
    }
}