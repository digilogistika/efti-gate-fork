package eu.efti.platformgatesimulator.controller.api;

import eu.efti.v1.consignment.common.SupplyChainConsignment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Tag(name = "Gate API controller", description = "Interfaces implemented by the platform for the eFTI Gate to request datasets from. These endpoints must be implemented by the platform developers for integration into eFTI Gate.")
public interface GateApiControllerApi {

    @Operation(
            summary = "Get Subsets endpoint for the eFTI Gate to request",
            description = "This endpoint is exposed for the eFTI Gate to request subsets from. The responding platform must respond with the right subsets that were requested and also make sure that the sent data is valid according to the schema and business rules laid out by the eFTI regulation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "NOTE that the schema provided requires also the namespace definition of http://efti.eu/v1/consignment/common to be added to the root of the element the root of the element also must have the name of 'consignment'. The schema provided in here is very big. Platform does not need to implement all of it. Only the subsets that it decides that it needs to conduct its business. For more info on the subsets please check out fintraffic data model for better visualisation: <a href='https://model.fintraffic-efti-dev.aws.fintraffic.cloud/#efti'>https://model.fintraffic-efti-dev.aws.fintraffic.cloud/#efti</a> and consult the dataset XSD schemas available from github: <a href='https://github.com/EFTI4EU/reference-implementation/tree/main/schema/xsd'>https://github.com/EFTI4EU/reference-implementation/tree/main/schema/xsd</a>", content = {
                    @Content(
                            mediaType = "application/xml",
                            schema = @Schema(implementation = SupplyChainConsignment.class, name = "consignment")
                    )
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @GetMapping("/v1/dataset/{datasetId}")
    ResponseEntity<Object> getConsignmentSubsets(
            @PathVariable("datasetId")
            @NotNull(message = "Dataset ID is missing")
            @NotBlank(message = "Dataset ID is missing")
            @Size(max = 36, message = "Dataset ID is too long")
            @Pattern(regexp = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", message = "Dataset ID has incorrect format")
            @Schema(example = "regex = [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
            String datasetId,

            @RequestParam(value = "subsetId")
            @Schema(example = "EE01,EU01")
            @NotEmpty(message = "Subsets are empty")
            Set<String> subsetId
    );

    @Operation(
            summary = "Post follow-up endpoint for the eFTI Gate to request",
            description = "This endpoint is exposed for the eFTI Gate to follow-up some dataset queries. If the authority checking the dataset with some ID determines that the dataset contains errors or is missing something they can create a follow-up message. This will be delivered to the eFTI platform containing the dataset with that ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @PostMapping("v1/dataset/{datasetId}/follow-up")
    ResponseEntity<Void> postConsignmentFollowup(
            @PathVariable("datasetId")
            @NotNull(message = "Dataset ID is missing")
            @NotBlank(message = "Dataset ID is missing")
            @Size(max = 36, message = "Dataset ID is too long")
            @Pattern(regexp = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", message = "Dataset ID has incorrect format")
            @Schema(example = "regex = [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
            String datasetId,

            @RequestBody
            @NotNull(message = "Message is null")
            @NotBlank(message = "Message is blank")
            @Schema(example = "Consignment is missing the digital signature of the consignee.")
            String body
    );
}
