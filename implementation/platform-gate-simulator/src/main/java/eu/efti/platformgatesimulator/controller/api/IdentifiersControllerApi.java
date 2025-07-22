package eu.efti.platformgatesimulator.controller.api;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Identifiers Controller [FOR TESTING]", description = "Allows to upload eFTI datasets for requesting by authorities. This will not be required to be implemented by the platform developers and can be ignored.")
@RequestMapping("/identifiers")
public interface IdentifiersControllerApi {

    @PutMapping("/upload/consignment/{datasetId}")
    @Operation(
            summary = "Upload dataset endpoint",
            description = "Can be used to upload eFTI datasets that will be registered with the eFTI gate. This endpoint is used by the eFTI Gate maintainers for testing purposes and can be ignored by the platform developers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. content was uploaded", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    ResponseEntity<String> uploadConsignment(
            @PathVariable("datasetId")
            @NotNull(message = "Dataset ID is missing")
            @NotBlank(message = "Dataset ID is missing")
            @Size(max = 36, message = "Dataset ID is too long")
            @Pattern(regexp = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}", message = "Dataset ID has incorrect format")
            @Schema(example = "regex = [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
            String datasetId,

            @RequestPart final MultipartFile consignmentFile
    );
}
