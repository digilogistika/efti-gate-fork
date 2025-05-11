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
    private final WebClient.Builder webClientBuilder;
    private final PlatformIdentityService platformIdentityService;

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
                    .uri(platformIdentityService.getFollowUpRequestUrl(controlDto.getPlatformId()))
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

            String uri = platformIdentityService.getUilRequestUrl(controlDto.getPlatformId()) +
                    "?datasetId=" + controlDto.getDatasetId() +
                    "&subsetId=" + "full" + // TODO: add reals subsets
                    "&requestId=" + controlDto.getRequestId();

            webClientBuilder
                    .build()
                    .get()
                    .uri(uri)
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
