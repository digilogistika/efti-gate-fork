package eu.efti.eftigate.controller.api;

import eu.efti.v1.consignment.identifier.SupplyChainConsignment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Tag(name = "Platform API controller", description = "Contains information about endpoints that are used by eFTI Platforms to communicate with the gate. Platform developers need to call these endpoints")
public interface PlatformApiControllerApi {


    @Operation(summary = "Upload Identifiers endpoint", description = "This endpoint is intended for the eFTI platforms that are integrated into the Gate. This endpoint must be called, with the correct identifiers data, once the platform want to upload some data into the gate about a consignment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accepted"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {
                    @Content(
                            mediaType = "application/problem+xml",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/v1/identifiers/{datasetId}",
            consumes = {"application/xml"}
    )
    ResponseEntity<Void> putConsignmentIdentifiers(
            @PathVariable("datasetId")
            @NotNull(message = "Dataset ID is missing")
            @NotBlank(message = "Dataset ID is missing")
            @Size(max = 36, message = "Dataset ID is too long")
            @Pattern(regexp = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", message = "Dataset ID has incorrect format")
            @Schema(example = "regex = [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
            String datasetId,


            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "NOTE that the schema provided requires also the namespace definition of http://efti.eu/v1/consignment/identifier to be added to the root of the element the root of the element also must have the name of 'consignment'. Platform does not need to fill out all the fields here. For more info on the subsets please check out fintraffic data model for better visualisation: <a href='https://model.fintraffic-efti-dev.aws.fintraffic.cloud/#efti'>https://model.fintraffic-efti-dev.aws.fintraffic.cloud/#efti</a> and consult the dataset XSD schemas available from github: <a href='https://github.com/EFTI4EU/reference-implementation/tree/main/schema/xsd'>https://github.com/EFTI4EU/reference-implementation/tree/main/schema/xsd</a>. For fintraffic visualisation please select the identifiers data model from the drop down menu.",
                    content = @Content(
                            mediaType = "application/xml",
                            schema = @Schema(implementation = SupplyChainConsignment.class)
                    )
            )
            String body
    );
}
