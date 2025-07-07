package eu.efti.platformgatesimulator.controller;

import eu.efti.commons.exception.TechnicalException;
import eu.efti.commons.utils.EftiSchemaUtils;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.platformgatesimulator.service.ReaderService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/gate-api")
@AllArgsConstructor
@Slf4j
public class GateApiController {
    private final ReaderService readerService;
    private final GateProperties gateProperties;
    private final SerializeUtils serializeUtils;

    @GetMapping("v1/dataset/{datasetId}")
    public ResponseEntity<Object> getConsignmentSubsets(
            @PathVariable("datasetId") String datasetId,
            @RequestParam(value = "subsetId") Set<String> subsetId
    ) {
        log.info("GET on /api/gate-api/v1/dataset/{}?subsetId={}", datasetId, subsetId);
        try {
            List<String> subsets = Arrays
                    .stream(subsetId
                            .toString()
                            .replaceAll("\\[|\\]|\"", "")
                            .split(",")
                    )
                    .map(String::trim)
                    .toList();

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            log.info(attributes.getRequest().getRequestURL().toString());
            log.info(attributes.getRequest().getQueryString());
            final SupplyChainConsignment supplyChainConsignment = readerService.readFromFile(gateProperties.getCdaPath() + datasetId, subsets);
            if (supplyChainConsignment != null) {
                var xml = serializeUtils.mapDocToXmlString(EftiSchemaUtils.mapCommonObjectToDoc(serializeUtils, supplyChainConsignment));
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_XML).body(xml);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (TechnicalException | IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("v1/dataset/{datasetId}/follow-up")
    public ResponseEntity<Void> postConsignmentFollowup(
            @PathVariable("datasetId") String datasetId,
            @RequestBody String body
    ) {
        log.info("POST on /api/gate-api/v1/dataset/{}/follow-up with body {}", datasetId, body);
        return null;
    }
}
