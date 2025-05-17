package eu.efti.eftigate.controller.api;

import eu.efti.v1.edelivery.SaveIdentifiersRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Platform API", description = "REST API for platforms. This API is implemented by the gate for platforms to request")
@RequestMapping("/api/v1/platforms")
public interface PlatformApiV1 {

    @Operation(
            summary = "Add or update consignment identifiers",
            description = "Add or update identifiers for a given consignment",
            requestBody = @RequestBody(content = @Content(
                    mediaType = MediaType.APPLICATION_XML_VALUE,
                    schema = @Schema(implementation = SaveIdentifiersRequest.class)
            ))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Request has been accepted for processing", content = {}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = String.class))),
    })
    @PostMapping(
            consumes = MediaType.APPLICATION_XML_VALUE,
            path = "identifiers")
    ResponseEntity<String> postConsignmentIdentifiers(
            @Parameter(description = "Consignment identifiers following the schema `http://efti.eu/v1/consignment/identifier`", required = true)
            @org.springframework.web.bind.annotation.RequestBody String body,
            @Parameter(description = "Authentication key for the platform. This key is used to authenticate the platform and should be kept secret.", required = true)
            @RequestHeader(name = "X-API-Key", required = true) String apiKey
    );
}
