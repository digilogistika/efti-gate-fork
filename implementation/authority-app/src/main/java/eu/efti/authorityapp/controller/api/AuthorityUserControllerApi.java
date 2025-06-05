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

@Tag(name = "Authority user", description = "Authority user creation and verification")
public interface AuthorityUserControllerApi {

    @Operation(summary = "Create Authority User", description = "Admin can create authority user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "409", description = "Some information already exists on the system", content = @Content(
                    schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @PostMapping("/admin/authority-user")
    ResponseEntity<Void> createAuthorityUser(final @RequestBody AuthorityUserDto authorityUserDto);

    @Operation(summary = "Verify Authority User", description = "Authority user verifies themselves.")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Some information is invalid",
            content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    @PostMapping("/public/control/authority-user/verify")
    ResponseEntity<JwtDto> verifyAuthorityUser(final @RequestBody AuthorityUserDto authorityUserDto);
}
