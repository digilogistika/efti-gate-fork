package eu.efti.eftigate.controller.api;

import eu.efti.eftigate.dto.DatasetDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "[For Authorities]", description = "Endpoint used by Authorities to query and retrieve data from the Gate.")
@RequestMapping("/v1")
public interface DatasetControllerApi {

    @Operation(summary = "[For Authorities] Request a Dataset (UIL)",
            description = "Initiates a request to retrieve a full dataset from a specific Platform. The Gate sends the request and waits for the platform to respond with the data before returning it to the client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. The dataset was successfully retrieved from the Platform and is returned in the response body.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = DatasetDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The X-API-Key is missing or invalid.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden. The client is not allowed to access this endpoint.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. An error occurred while fetching the data from the platform.", content = @Content)
    })
    @GetMapping("/dataset/{gateId}/{platformId}/{datasetId}")
    ResponseEntity<DatasetDto> getDataset(
            @PathVariable
            @NotNull(message = "UIL_GATE_MISSING")
            @NotBlank(message = "UIL_GATE_MISSING")
            @Size(max = 255, message = "GATE_ID_TOO_LONG")
            @Pattern(regexp = "^[-@./#&+\\w\\s]*$", message = "GATE_ID_INCORRECT_FORMAT")
            @Schema(example = "regex = ^[-@./#&+\\w\\s]*$")
            String gateId,

            @PathVariable
            @NotNull(message = "UIL_PLATFORM_MISSING")
            @NotBlank(message = "UIL_PLATFORM_MISSING")
            @Size(max = 255, message = "PLATFORM_ID_TOO_LONG")
            @Pattern(regexp = "^[-@./#&+\\w\\s]*$", message = "PLATFORM_ID_INCORRECT_FORMAT")
            @Schema(example = "regex = ^[-@./#&+\\w\\s]*$")
            String platformId,

            @PathVariable
            @NotNull(message = "UIL_UUID_MISSING")
            @NotBlank(message = "UIL_UUID_MISSING")
            @Size(max = 36, message = "DATASET_ID_TOO_LONG")
            @Pattern(regexp = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", message = "DATASET_ID_INCORRECT_FORMAT")
            @Schema(example = "regex = [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
            String datasetId,

            @RequestParam(name = "subsets", defaultValue = "")
            @Schema(example = "EE01,EU01")
            List<String> subsetIds
    );
}
