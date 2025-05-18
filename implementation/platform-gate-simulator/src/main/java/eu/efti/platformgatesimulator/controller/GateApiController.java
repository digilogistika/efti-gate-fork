package eu.efti.platformgatesimulator.controller;

import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.platformgatesimulator.controller.api.GateApi;
import eu.efti.platformgatesimulator.service.IdentifierService;
import eu.efti.platformgatesimulator.service.ReaderService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/gate-api")
@AllArgsConstructor
@Slf4j
public class GateApiController implements GateApi {
    private final ReaderService readerService;
    private final GateProperties gateProperties;
    private final IdentifierService identifierService;


    public ResponseEntity<Object> getConsignmentSubsets(
            @RequestParam String datasetId,
            @RequestParam Set<String> subsetId,
            @RequestParam String requestId
    ) {
        log.info("GET on /gate-api/consignments with params datasetId: {}, subsetId: {}, requestId: {}", datasetId, subsetId, requestId);
        try {
            SupplyChainConsignment supplyChainConsignment = readerService.readFromFile(gateProperties.getCdaPath() + datasetId, subsetId.stream().toList());
            String uilResponse = identifierService.buildBody(requestId, supplyChainConsignment);

            return ResponseEntity.ok(uilResponse);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Void> postConsignmentFollowup(@RequestBody String body) {
        log.info("POST on /follow-up with body {}", body);
        return null;
    }
}
