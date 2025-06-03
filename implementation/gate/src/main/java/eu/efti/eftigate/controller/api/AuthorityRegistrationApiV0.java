package eu.efti.eftigate.controller.api;

import eu.efti.eftigate.dto.AuthorityUserRegistrationRequestDto;
import eu.efti.eftigate.dto.AuthorityUserRegistrationResponseDto;
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

@Tag(name = "Authority Registration API", description = "Used for registering new authorities.")
@RequestMapping("/api/authority")
public interface AuthorityRegistrationApiV0 {

    @Operation(
            summary = "Register an authority to the system",
            description = "User will be registered and a response will be returned with the API key that can be used by the authority."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "authority registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorityUserRegistrationResponseDto.class))),
            }
    )
    @PostMapping(value = "/v0/register")
    ResponseEntity<AuthorityUserRegistrationResponseDto> registerAuthority(
            @RequestBody AuthorityUserRegistrationRequestDto authorityUserRegistrationRequestDto
    );
}
