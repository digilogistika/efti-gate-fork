package eu.efti.eftigate.controller.api;

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

@Tag(name = "Identifiers query", description = "Interface to search for identifiers")
@RequestMapping("/v1")
public interface IdentifiersControllerApi {

    @Operation(summary = "Requesting identifiers from the gate", description = "Send a query to retrieve identifiers matching to the search criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
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
