package eu.efti.platformgatesimulator.controller;

import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.platformgatesimulator.service.IdentifierService;
import eu.efti.platformgatesimulator.service.ReaderService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/gate-api")
@AllArgsConstructor
@Slf4j
public class GateApiController {
    private final ReaderService readerService;
    private final GateProperties gateProperties;
    private final IdentifierService identifierService;

    @GetMapping(
            path = "/consignments",
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<Object> getConsignmentSubsets(
            @RequestParam String datasetId,
            @RequestParam Set<String> subsetId,
            @RequestParam String requestId
    ) {
        try {
            SupplyChainConsignment supplyChainConsignment = readerService.readFromFile(gateProperties.getCdaPath() + datasetId, subsetId.stream().toList());
            String uilResponse = identifierService.buildBody(requestId, supplyChainConsignment);

            RestClient restClient = RestClient
                    .builder()
                    .baseUrl("http://localhost:8880/api/v1/platform")
                    .build();
            String response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uil")
                            .build()
                    )
                    .contentType(MediaType.APPLICATION_XML)
                    .body(uilResponse)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(
            path = "/follow-up",
            consumes = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<Void> postConsignmentFollowup(@RequestBody String body) {
        log.info("POST on /follow-up with body {}", body);
        return null;
    }
}
