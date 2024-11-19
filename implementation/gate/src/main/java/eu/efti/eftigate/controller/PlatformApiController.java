package eu.efti.eftigate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform")
@Tag(name = "Platform API", description = "REST API for the platforms")
public class PlatformApiController {

    @PutMapping(value = "/v0/consignments/{datasetId}", consumes = "application/xml")
    @Operation(summary = "Add or update consignment identifiers", description = "Adds or updates identifiers for a consignment")
    public ResponseEntity<Void> putConsignmentIdentifiers(
            @Parameter(description = "dataset Id", required = true)
            @PathVariable("datasetId") String datasetId,
            @Parameter(description = "Consignment identifiers following the schema `http://efti.eu/v1/consignment/identifier`",
                    required = true)
            @RequestBody Object body) {
        return null;
    }
}
