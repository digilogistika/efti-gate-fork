package eu.efti.eftigate.service;

import eu.efti.commons.utils.MappingException;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.eftigate.dto.PlatformHeaderDto;
import eu.efti.eftigate.service.request.ValidationService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class PlatformApiService {
    private final SerializeUtils serializeUtils;
    private final WebClient.Builder webClientBuilder;
    private final PlatformIdentityService platformIdentityService;
    private final ValidationService validationService;


    public void callPostConsignmentFollowup(String platformId, String datasetId, String body) throws PlatformIntegrationServiceException {
        try {
            log.info("Sending follow up request to platform with id: {}", platformId);

            List<PlatformHeaderDto> headers = platformIdentityService.getPlatformRequestHeaders(platformId);

            webClientBuilder
                    .build()
                    .post()
                    .uri(platformIdentityService.getRequestBaseUrl(platformId) + "/" + datasetId + "/follow-up")
                    .headers(consumer -> {
                        for (PlatformHeaderDto header : headers) {
                            consumer.add(header.getKey(), header.getValue());
                        }
                    })
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Successfully sent follow up request to platform with id: {}", platformId);
        } catch (HttpClientErrorException e) {
            log.error("Error while sending follow up request to platform with id: {}", platformId, e);
            throw new PlatformIntegrationServiceException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }


    public SupplyChainConsignment callGetConsignmentSubsets(String platformId, String datasetId, Set<String> subsetIds) throws PlatformIntegrationServiceException {
        try {
            log.info("Sending UIL request to platform with id: {}", platformId);

            String uri = platformIdentityService.getRequestBaseUrl(platformId) +
                    "/" + datasetId +
                    "?" + "&subsetId=" + String.join(",", subsetIds);

            List<PlatformHeaderDto> headers = platformIdentityService.getPlatformRequestHeaders(platformId);

            ResponseEntity<String> response = webClientBuilder
                    .build()
                    .get()
                    .uri(uri)
                    .headers(consumer -> {
                        for (PlatformHeaderDto header : headers) {
                            consumer.add(header.getKey(), header.getValue());
                        }
                    })
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            assert response != null;
            assert response.getBody() != null;

            String xml = response.getBody();
            return serializeUtils.mapXmlStringToJaxbObject(xml, SupplyChainConsignment.class, validationService.getGateSchema());
        } catch (MappingException e) {
            throw new PlatformIntegrationServiceException("Got invalid content from platform", e);
        } catch (HttpClientErrorException e) {
            throw new PlatformIntegrationServiceException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Error while sending UIL request to platform with id: {}", platformId, e);
            throw new PlatformIntegrationServiceException("Error while sending UIL request to platform", e);
        }
    }
}
