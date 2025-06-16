package eu.efti.eftigate.service;

import eu.efti.commons.utils.MappingException;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.eftigate.dto.PlatformHeaderDto;
import eu.efti.eftigate.service.request.ValidationService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.xml.validation.Schema;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformApiServiceTest {

    private static final String PLATFORM_ID = "test-platform-id";
    private static final String DATASET_ID = "test-dataset-id";
    private static final String BASE_URL = "https://test-platform.com/api";
    private static final String REQUEST_BODY = "test request body";
    @Mock
    private SerializeUtils serializeUtils;
    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private PlatformIdentityService platformIdentityService;
    @Mock
    private ValidationService validationService;
    @Mock
    private Schema schema;
    private PlatformApiService platformApiService;

    @BeforeEach
    void setUp() {
        platformApiService = new PlatformApiService(
                serializeUtils,
                webClientBuilder,
                platformIdentityService,
                validationService
        );

        // Setup common WebClient builder behavior
        when(webClientBuilder.build()).thenReturn(webClient);
        when(platformIdentityService.getRequestBaseUrl(PLATFORM_ID)).thenReturn(BASE_URL);
    }

    @Test
    void callPostConsignmentFollowup_ShouldSendCorrectRequestWithHeaders() throws PlatformIntegrationServiceException {
        // Given
        List<PlatformHeaderDto> headers = Arrays.asList(
                new PlatformHeaderDto("Authorization", "Bearer token123"),
                new PlatformHeaderDto("X-API-Key", "api-key-456"),
                new PlatformHeaderDto("Content-Type", "application/json")
        );

        when(platformIdentityService.getPlatformRequestHeaders(PLATFORM_ID)).thenReturn(headers);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.TEXT_PLAIN)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(REQUEST_BODY)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        // When
        platformApiService.callPostConsignmentFollowup(PLATFORM_ID, DATASET_ID, REQUEST_BODY);

        // Then
        // Verify correct URI is called
        String expectedUri = BASE_URL + "/" + DATASET_ID + "/follow-up";
        verify(requestBodyUriSpec).uri(expectedUri);

        // Verify headers are set correctly
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestBodySpec).headers(headersCaptor.capture());

        HttpHeaders httpHeaders = new HttpHeaders();
        headersCaptor.getValue().accept(httpHeaders);

        assertEquals("Bearer token123", httpHeaders.getFirst("Authorization"));
        assertEquals("api-key-456", httpHeaders.getFirst("X-API-Key"));
        assertEquals("application/json", httpHeaders.getFirst("Content-Type"));

        // Verify correct content type and body
        verify(requestBodySpec).contentType(MediaType.TEXT_PLAIN);
        verify(requestBodySpec).bodyValue(REQUEST_BODY);

        // Verify platform identity service calls
        verify(platformIdentityService).getRequestBaseUrl(PLATFORM_ID);
        verify(platformIdentityService).getPlatformRequestHeaders(PLATFORM_ID);
    }


    @Test
    void callGetConsignmentSubsets_ShouldSendCorrectRequestWithHeaders() throws PlatformIntegrationServiceException {
        // Given
        Set<String> subsetIds = Set.of("subset1", "subset2", "subset3");
        List<PlatformHeaderDto> headers = Arrays.asList(
                new PlatformHeaderDto("Authorization", "Bearer token123"),
                new PlatformHeaderDto("X-Client-ID", "client-456")
        );

        String xmlResponse = "<SupplyChainConsignment>test</SupplyChainConsignment>";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(xmlResponse);
        SupplyChainConsignment expectedConsignment = new SupplyChainConsignment();

        when(platformIdentityService.getPlatformRequestHeaders(PLATFORM_ID)).thenReturn(headers);
        when(validationService.getGateSchema()).thenReturn(schema);
        when(serializeUtils.mapXmlStringToJaxbObject(xmlResponse, SupplyChainConsignment.class, schema))
                .thenReturn(expectedConsignment);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(Mono.just(responseEntity));

        // When
        SupplyChainConsignment result = platformApiService.callGetConsignmentSubsets(PLATFORM_ID, DATASET_ID, subsetIds);

        // Then
        assertSame(expectedConsignment, result);

        // Verify correct URI is called (note: the URI construction in original code seems to have issues with Set serialization)
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestHeadersUriSpec).uri(uriCaptor.capture());

        String actualUri = uriCaptor.getValue();
        assertTrue(actualUri.startsWith(BASE_URL + "/" + DATASET_ID + "?"));
        assertTrue(actualUri.contains("subsetId="));
        // Note: Set.toString() format is [subset1, subset2, subset3] which may not be what the API expects

        // Verify headers are set correctly
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestHeadersSpec).headers(headersCaptor.capture());

        HttpHeaders httpHeaders = new HttpHeaders();
        headersCaptor.getValue().accept(httpHeaders);

        assertEquals("Bearer token123", httpHeaders.getFirst("Authorization"));
        assertEquals("client-456", httpHeaders.getFirst("X-Client-ID"));

        // Verify service calls
        verify(platformIdentityService).getRequestBaseUrl(PLATFORM_ID);
        verify(platformIdentityService).getPlatformRequestHeaders(PLATFORM_ID);
        verify(validationService).getGateSchema();
        verify(serializeUtils).mapXmlStringToJaxbObject(xmlResponse, SupplyChainConsignment.class, schema);
    }

    @Test
    void callGetConsignmentSubsets_ShouldThrowExceptionOnMappingError() {
        // Given
        Set<String> subsetIds = Set.of("subset1");
        List<PlatformHeaderDto> headers = List.of(
                new PlatformHeaderDto("Authorization", "Bearer token123")
        );

        String xmlResponse = "<invalid>xml</invalid>";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(xmlResponse);
        MappingException mappingException = new MappingException("Invalid XML", new Exception("Mapping error"));

        when(platformIdentityService.getPlatformRequestHeaders(PLATFORM_ID)).thenReturn(headers);
        when(validationService.getGateSchema()).thenReturn(schema);
        when(serializeUtils.mapXmlStringToJaxbObject(xmlResponse, SupplyChainConsignment.class, schema))
                .thenThrow(mappingException);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(Mono.just(responseEntity));

        // When & Then
        PlatformIntegrationServiceException exception = assertThrows(
                PlatformIntegrationServiceException.class,
                () -> platformApiService.callGetConsignmentSubsets(PLATFORM_ID, DATASET_ID, subsetIds)
        );

        assertEquals(mappingException, exception.getCause());
    }


    @Test
    void callPostConsignmentFollowup_ShouldHandleEmptyHeaders() throws PlatformIntegrationServiceException {
        // Given
        List<PlatformHeaderDto> emptyHeaders = List.of();

        when(platformIdentityService.getPlatformRequestHeaders(PLATFORM_ID)).thenReturn(emptyHeaders);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any(Consumer.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.TEXT_PLAIN)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(REQUEST_BODY)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        // When
        platformApiService.callPostConsignmentFollowup(PLATFORM_ID, DATASET_ID, REQUEST_BODY);

        // Then
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestBodySpec).headers(headersCaptor.capture());

        HttpHeaders httpHeaders = new HttpHeaders();
        headersCaptor.getValue().accept(httpHeaders);

        assertTrue(httpHeaders.isEmpty());
    }
}