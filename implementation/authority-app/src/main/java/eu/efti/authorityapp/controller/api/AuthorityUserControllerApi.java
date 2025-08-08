package eu.efti.authorityapp.controller.api;

import eu.efti.authorityapp.dto.AuthorityUserDto;
import eu.efti.authorityapp.dto.ExceptionResponseDto;
import eu.efti.authorityapp.dto.JwtDto;
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

@Tag(name = "[For Admins]", description = "Endpoints for managing authority users.")
@Tag(name = "[Public]", description = "Endpoints for public operations like user authentication.")
@RequestMapping("/api")
public interface AuthorityUserControllerApi {

    @Tag(name = "[For Admins]")
    @Operation(summary = "[For Admins] Create Authority User", description = "Allows an administrator to create a new authority user in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. The user was created successfully."),
            @ApiResponse(responseCode = "409", description = "Conflict. A user with the provided details already exists.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content)
    })
    @PostMapping("/admin/authority-user/create")
    ResponseEntity<Void> createAuthorityUser(final @RequestBody AuthorityUserDto authorityUserDto);

    @Tag(name = "[Public]")
    @Operation(summary = "[Public] Verify Authority User", description = "Verifies a user's credentials and returns a JWT if successful. This token is used for authenticating subsequent actions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. Verification successful, JWT is returned.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request. The provided user information is invalid or incorrect.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PostMapping("/public/authority-user/verify")
    ResponseEntity<JwtDto> verifyAuthorityUser(final @RequestBody AuthorityUserDto authorityUserDto);

    @Tag(name = "[Public]")
    @Operation(summary = "[Public] Validate JWT", description = "Validates an existing JWT to confirm its authenticity and check if it has expired.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK. The JWT is valid."),
            @ApiResponse(responseCode = "400", description = "Bad Request. The provided JWT is invalid or expired.", content = @Content)
    })
    @PostMapping("/public/authority-user/validate")
    ResponseEntity<Void> validateAuthorityUser(final @RequestBody String jwt);
}
