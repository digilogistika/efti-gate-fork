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

@Tag(name = "[For Platforms]", description = "Endpoints used by Platforms to send data to the Gate. Platform developers need to call these endpoints")
public interface PlatformApiControllerApi {


    @Operation(summary = "[For Platforms] Upload Identifiers",
            description = "This endpoint is intended for Platforms to upload consignment identifiers to the Gate. The Gate validates and synchronously saves this data to its local identifiers registry, making it available for queries from authorities.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. The identifiers were successfully validated and saved in the Gate's registry."),
            @ApiResponse(responseCode = "400", description = "Bad Request. The request body is malformed or fails XML schema validation.", content = {
                    @Content(mediaType = "application/problem+xml", schema = @Schema(implementation = ProblemDetail.class))
            }),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The X-API-Key is missing, invalid, or does not correspond to a known platform.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden. The client is not allowed to access this endpoint.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. An unexpected error occurred on the server.", content = @Content)
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
