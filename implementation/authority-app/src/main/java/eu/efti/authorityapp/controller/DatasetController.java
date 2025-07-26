package eu.efti.authorityapp.controller;

import eu.efti.authorityapp.config.GateProperties;
import eu.efti.authorityapp.controller.api.DatasetControllerApi;
import eu.efti.authorityapp.dto.DatasetDto;
import eu.efti.authorityapp.service.ConfigService;
import eu.efti.authorityapp.service.PdfGenerationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class DatasetController implements DatasetControllerApi {
    private final GateProperties gateProperties;
    private final RestTemplate restTemplate;
    private final ConfigService configService;
    private final PdfGenerationService pdfGenerationService;

    @Override
    @GetMapping("/dataset/{gateId}/{platformId}/{datasetId}")
    public ResponseEntity<DatasetDto> getDataset(String gateId, String platformId, String datasetId, List<String> subsetIds) {
        log.info("Querying gate for dataset with gateId: {}, platformId: {}, datasetId: {} and subsets: {}", gateId, platformId, datasetId, subsetIds);

        String queryUrl = UriComponentsBuilder
                .newInstance()
                .scheme(gateProperties.getBaseUrl().getProtocol())
                .host(gateProperties.getBaseUrl().getHost())
                .port(gateProperties.getBaseUrl().getPort())
                .pathSegment("v1", "dataset", gateId, platformId, datasetId)
                .query("subsets=full")
                .buildAndExpand(String.join(",", subsetIds))
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", configService.getApiKey());
        headers.set("Accept", "application/json");

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        try {
            log.info("Sending GET request for dataset to gate at: {}", queryUrl);
            ResponseEntity<DatasetDto> response = restTemplate.exchange(
                    queryUrl,
                    HttpMethod.GET,
                    requestEntity,
                    DatasetDto.class
            );

            log.info("Received response from gate with status: {}", response.getStatusCode());
            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());
        } catch (Exception e) {
            log.error("Error querying gate for dataset", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @GetMapping(value = "/dataset/pdf/{gateId}/{platformId}/{datasetId}", produces = "application/pdf")
    public ResponseEntity<byte[]> getDatasetAsPdf(String gateId, String platformId, String datasetId, List<String> subsetIds) {
        log.info("Request received to generate PDF for dataset with gateId: {}, platformId: {}, datasetId: {}", gateId, platformId, datasetId);

        final ResponseEntity<DatasetDto> datasetResponse = getDataset(gateId, platformId, datasetId, subsetIds);

        if (datasetResponse.getStatusCode() != HttpStatus.OK || !datasetResponse.hasBody()) {
            log.error("Failed to retrieve dataset data. Status: {}", datasetResponse.getStatusCode());
            return ResponseEntity.status(datasetResponse.getStatusCode()).build();
        }

        final DatasetDto datasetDto = datasetResponse.getBody();
        if (datasetDto == null || datasetDto.getData() == null) {
            log.error("Dataset response is not OK or data is null. Status: {}",
                    datasetDto != null ? datasetDto.getStatus() : "null");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        try {
            final byte[] pdfBytes = pdfGenerationService.generatePdf(
                    datasetDto.getRequestId(),
                    datasetDto.getData());

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline").filename("dataset_" + datasetId + ".pdf").build());

            log.info("Successfully generated and returning PDF for datasetId: {}", datasetId);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (final Exception e) {
            log.error("PDF generation failed for datasetId: {}", datasetId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
