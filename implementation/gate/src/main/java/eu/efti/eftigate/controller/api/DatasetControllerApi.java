package eu.efti.eftigate.controller.api;

import eu.efti.eftigate.dto.RequestIdDto;
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

@Tag(name = "UIL query", description = "Interface to manage dataset request")
@RequestMapping("/v1")
public interface DatasetControllerApi {

    @Operation(summary = "Requesting dataset from the gate", description = "Send a query to get dataset from the platform through the gate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @GetMapping("/dataset/{gateId}/{platformId}/{datasetId}")
    ResponseEntity<RequestIdDto> getDataset(
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
