package eu.efti.authorityapp.controller;

import eu.efti.authorityapp.config.GateProperties;
import eu.efti.authorityapp.controller.api.DatasetControllerApi;
import eu.efti.authorityapp.dto.DatasetDto;
import eu.efti.authorityapp.dto.PdfGenerationResult;
import eu.efti.authorityapp.service.ConfigService;
import eu.efti.authorityapp.service.DataProcessingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class DatasetController implements DatasetControllerApi {
    private final GateProperties gateProperties;
    private final RestTemplate restTemplate;
    private final ConfigService configService;
    private final DataProcessingService dataProcessingService;

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
                .query("subsets={subsets}")
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
            final DatasetDto datasetDto = response.getBody();

            if (response.getStatusCode() != HttpStatus.OK || datasetDto == null || datasetDto.getData() == null) {
                log.error("Dataset response is not OK or data is null. Status: {}", response.getStatusCode());
                return response;
            }


            try {
                log.info("Generating PDF for request ID: {}", datasetDto.getRequestId());
                final PdfGenerationResult pdfResult = dataProcessingService.generatePdf(
                        datasetDto.getRequestId(),
                        datasetDto.getData());

                datasetDto.setPdfData(pdfResult.pdfBytes());
                datasetDto.setEftiData(pdfResult.consignment());
                log.info("Successfully generated and embedded PDF into the response.");
            } catch (final Exception e) {
                log.error("PDF generation failed for datasetId: {}. Returning data without PDF.", datasetId, e);
            }

            return ResponseEntity.ok(datasetDto);


        } catch (Exception e) {
            log.error("Error querying gate for dataset", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
