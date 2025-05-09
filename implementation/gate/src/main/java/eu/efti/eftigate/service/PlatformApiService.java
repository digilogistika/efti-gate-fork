package eu.efti.eftigate.service;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.dto.PostFollowUpRequestDto;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.v1.edelivery.ObjectFactory;
import eu.efti.v1.edelivery.PostFollowUpRequest;
import eu.efti.v1.edelivery.UIL;
import jakarta.xml.bind.JAXBElement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@AllArgsConstructor
public class PlatformApiService {
    private final SerializeUtils serializeUtils;
    private final ObjectFactory objectFactory = new ObjectFactory();
    private WebClient.Builder webClientBuilder;

    @Async
    public CompletableFuture<Void> sendFollowUpRequest(PostFollowUpRequestDto postFollowUpRequestDto, ControlDto controlDto) {
        try {
            final PostFollowUpRequest postFollowUpRequest = new PostFollowUpRequest();
            final UIL uil = new UIL();

            uil.setPlatformId(controlDto.getPlatformId());
            uil.setGateId(controlDto.getGateId());
            uil.setDatasetId(controlDto.getDatasetId());
            postFollowUpRequest.setUil(uil);
            postFollowUpRequest.setMessage(postFollowUpRequestDto.getMessage());
            postFollowUpRequest.setRequestId(postFollowUpRequestDto.getRequestId());

            final JAXBElement<PostFollowUpRequest> note = objectFactory.createPostFollowUpRequest(postFollowUpRequest);
            String body = serializeUtils.mapJaxbObjectToXmlString(note, PostFollowUpRequest.class);

            log.info("Sending follow up request to platform with id: {}", controlDto.getPlatformId());
            webClientBuilder
                    .build()
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("localhost")
                            .port("8070")
                            .path("/gate-api/follow-up")
                            .build()
                    )
                    .contentType(MediaType.APPLICATION_XML)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Successfully sent follow up request to platform with id: {}", controlDto.getPlatformId());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error while sending follow up request to platform with id: {}", controlDto.getPlatformId(), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Async
    public CompletableFuture<Void> sendUilRequest(ControlDto controlDto) {
        try {
            log.info("Sending UIL request to platform with id: {}", controlDto.getPlatformId());

            webClientBuilder
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("localhost")
                            .port(8070)
                            .path("/gate-api/consignments")
                            .queryParam("datasetId", controlDto.getDatasetId())
                            .queryParam("subsetId", controlDto.getSubsetIds())
                            .queryParam("requestId", controlDto.getRequestId())
                            .build()
                    )
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error while sending UIL request to platform with id: {}", controlDto.getPlatformId(), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
