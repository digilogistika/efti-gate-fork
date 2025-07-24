package eu.efti.platformgatesimulator.controller.api;

import eu.efti.v1.consignment.common.SupplyChainConsignment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Tag(name = "Example subsets [FOR TESTING]", description = "Allows integrating platforms to get a better idea of what the subsets will look like that must be provided to the credible authority upon request.")
@RequestMapping("/example")
public interface ExampleSubsetsControllerApi {
    @Operation(
            summary = "Get Example Subsets endpoint",
            description = "This endpoint is meant for use by integrating platforms who want to get a better idea of what the subsets will look like, that must be given to CAs upon request. Subsets from this endpoint are extracted from one file that contains fields that a subset consists of. Full file can be requested by using the subset 'full'. WARNING results from this endpoint can be incomplete and/or contain non valid values. The responsibility of data validity is in the hands of the platform integrators. This API does not validate the subsets for business rules. More about business rules can be read here: <a href='https://svn.gefeg.com/svn/efti-publication/Draft/CDS/br1.htm'>https://svn.gefeg.com/svn/efti-publication/Draft/CDS/br1.htm</a>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subsets extracted from a consignment master file", content = {
                    @Content(
                            mediaType = "application/xml",
                            schema = @Schema(implementation = SupplyChainConsignment.class, name = "consignment")
                    )
            }),
            @ApiResponse(responseCode = "400", description = "Something went wrong", content = @Content(schema = @Schema()))
    })
    @GetMapping("/subsets")
    ResponseEntity<Object> getSubsets(
            @RequestParam(value = "subsets")
            @Schema(example = "EE01,EU01", description = "More info on available subsets can be viewed from fintraffic website <a href='https://model.fintraffic-efti-dev.aws.fintraffic.cloud/#efti'>https://model.fintraffic-efti-dev.aws.fintraffic.cloud/#efti</a>")
            @NotEmpty(message = "Subsets are empty")
            Set<String> subsets
    );
}
