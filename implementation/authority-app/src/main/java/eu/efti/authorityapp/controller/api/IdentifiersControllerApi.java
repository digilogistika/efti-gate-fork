package eu.efti.authorityapp.controller.api;

import eu.efti.commons.dto.IdentifiersResponseDto;
import eu.efti.commons.enums.CountryIndicator;
import eu.efti.commons.validator.ValueOfEnum;
import eu.efti.v1.codes.CountryCode;
import eu.efti.v1.edelivery.IdentifierType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "For Authorities", description = "Endpoint used by Authorities to query and retrieve data from the Gate.")
@RequestMapping("/api/v1")
public interface IdentifiersControllerApi {

    @Operation(summary = "[For Authorities] Query for Identifiers",
            description = "Sends a query to the Gate to find data based on transport identifiers and other filter criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful query. The response body contains the list of matching identifiers.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdentifiersResponseDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The API key is missing or invalid.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden. The API key is valid but does not have permission to access this resource.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. An error occurred either in the Authority App or in the downstream Gate.", content = @Content)
    })
    @GetMapping("/identifiers/{identifier}")
    ResponseEntity<IdentifiersResponseDto> getIdentifiers(
            @PathVariable
            @NotBlank(message = "IDENTIFIER_MISSING")
            @Length(max = 17, message = "IDENTIFIER_TOO_LONG")
            @Pattern(regexp = "^[A-Za-z0-9]*$", message = "IDENTIFIER_INCORRECT_FORMAT")
            String identifier,

            @RequestParam(required = false)
            @Pattern(regexp = "^\\d$", message = "MODE_CODE_INCORRECT_FORMAT")
            String modeCode,

            @RequestParam(required = false)
            List<@ValueOfEnum(enumClass = IdentifierType.class, message = "IDENTIFIER_TYPE_INCORRECT") String> identifierType,

            @RequestParam(required = false)
            @ValueOfEnum(enumClass = CountryCode.class, message = "REGISTRATION_COUNTRY_INCORRECT")
            String registrationCountryCode,

            @RequestParam(required = false)
            Boolean dangerousGoodsIndicator,

            @RequestParam(required = false)
            List<@ValueOfEnum(enumClass = CountryIndicator.class, message = "GATE_INDICATOR_INCORRECT") String> eftiGateIndicator
    );
}
