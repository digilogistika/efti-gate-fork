package eu.efti.platformgatesimulator.controller.api;

import eu.efti.v1.edelivery.PostFollowUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Tag(name = "Gate api", description = "REST API for the gate to request. This should be implemented by the platforms")
@RequestMapping("/gate-api")
public interface GateApi {

    @Operation(
            summary = "Get UIL consignments asynchronously",
            description = "Get the UIL consignments asynchronously. This endpoint should return immediately and start processing the request. resulting consignment should be returned to the gates /uil endpoint"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Request has been accepted for processing", content = {}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = String.class))),
    })
    @GetMapping(
            path = "/consignments"
    )
    ResponseEntity<Object> getConsignmentSubsets(
            @Parameter(description = "Dataset id of the consignment", required = true)
            @RequestParam String datasetId,
            @Parameter(description = "Subset ids to be used for filtering the consignment", required = true)
            @RequestParam Set<String> subsetId,
            @Parameter(description = "Request id. this should be returned in the async response to the gate.", required = true)
            @RequestParam String requestId
    );

    @Operation(
            summary = "Follow up communication",
            description = "Follow up communication to a platform for a given control",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = MediaType.APPLICATION_XML_VALUE,
                    schema = @Schema(implementation = PostFollowUpRequest.class)
            ))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Request has been accepted for processing", content = {}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = String.class))),
    })
    @PostMapping(
            path = "/follow-up",
            consumes = MediaType.APPLICATION_XML_VALUE
    )
    ResponseEntity<Void> postConsignmentFollowup(
            @Parameter(description = "Follow up communication body", required = true)
            @RequestBody String body
    );
}
