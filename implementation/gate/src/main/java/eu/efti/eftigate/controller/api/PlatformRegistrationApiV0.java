package eu.efti.eftigate.controller.api;

import eu.efti.eftigate.dto.PlatformRegistrationRequestDto;
import eu.efti.eftigate.dto.PlatformRegistrationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Platform Registration API", description = "Used for managing new platforms registration")
@RequestMapping("/api/platform")
public interface PlatformRegistrationApiV0 {

    @Operation(
            summary = "Register a new platform to the system",
            description = "Add a new platform with its endpoint information to the system. The platform will be registered and a response will be returned with the API key that can be used by the platform."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Platform registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformRegistrationResponseDto.class))),
            }
    )
    @PostMapping(value = "/v0/register")
    ResponseEntity<PlatformRegistrationResponseDto> registerPlatform(
            @RequestBody PlatformRegistrationRequestDto platformRegistrationRequestDto
    );
}
