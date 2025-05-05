package eu.efti.platformgatesimulator.controller;

import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.platformgatesimulator.service.ReaderService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/gate-api")
@AllArgsConstructor
@Slf4j
public class GateApiController {
    private final ReaderService readerService;
    private final GateProperties gateProperties;

    @GetMapping(
            path = "/consignments",
            produces = "application/xml"
    )
    public ResponseEntity<Object> getConsignmentSubsets(@RequestParam String datasetId, @RequestParam Set<String> subsetId) {
        try {
            SupplyChainConsignment supplyChainConsignment = readerService.readFromFile(gateProperties.getCdaPath() + datasetId, subsetId.stream().toList());
            return ResponseEntity.ok(supplyChainConsignment);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }


    public ResponseEntity<Void> postConsignmentFollowup(String datasetId, String body) {
        return null;
    }
}
