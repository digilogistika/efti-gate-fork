package eu.efti.platformgatesimulator.controller;

import eu.efti.commons.utils.EftiSchemaUtils;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.platformgatesimulator.controller.api.ExampleSubsetsControllerApi;
import eu.efti.platformgatesimulator.service.ReaderService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@Slf4j
public class ExampleSubsetController implements ExampleSubsetsControllerApi {
    private final ReaderService readerService;
    private final GateProperties gateProperties;
    private final SerializeUtils serializeUtils;

    @Override
    public ResponseEntity<Object> getSubsets(Set<String> subsets) {
        try {
            List<String> ss = Arrays
                    .stream(subsets
                            .toString()
                            .replaceAll("\\[|\\]|\"", "")
                            .split(",")
                    )
                    .map(String::trim)
                    .toList();

            SupplyChainConsignment supplyChainConsignment = readerService.readFromFile(gateProperties.getCdaPath() + "real-data-dataset-example", ss);

            String xml = serializeUtils.mapDocToXmlString(EftiSchemaUtils.mapCommonObjectToDoc(serializeUtils, supplyChainConsignment));
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_XML).body(xml);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
