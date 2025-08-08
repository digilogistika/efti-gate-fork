package eu.efti.eftigate.controller.api;

import eu.efti.eftigate.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "[For Admins]", description = "API for management of the Gate's configuration database. Requires a super-user API key.")
@RequestMapping("/api/admin")
public interface GateAdministrationApi {

    @Operation(summary = "[For Admins] Register Gate", description = "Adds a new gate to the Gate's local configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gate successfully added."),
            @ApiResponse(responseCode = "409", description = "Conflict. A gate with the same ID already exists."),
    })
    @PostMapping("/gate/register")
    ResponseEntity<String> registerGate(@RequestBody @Validated GateDto gateDto);

    @Operation(summary = "[For Admins] Delete Gate", description = "Deletes a gate from the Gate's local configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gate successfully deleted."),
            @ApiResponse(responseCode = "404", description = "Not Found. The specified gate does not exist."),
    })
    @DeleteMapping("/gate/delete/{gateId}")
    ResponseEntity<String> deleteGate(@PathVariable String gateId);

    @Operation(summary = "[For Admins] Register Authority", description = "Registers a new authority in the Gate's local configuration and generates an API key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authority registered successfully. The response contains the generated API key.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorityUserRegistrationResponseDto.class))),
    })
    @PostMapping(value = "/authority/register")
    ResponseEntity<AuthorityUserRegistrationResponseDto> registerAuthority(
            @RequestBody @Validated AuthorityUserRegistrationRequestDto authorityUserRegistrationRequestDto
    );

    @Operation(summary = "[For Admins] Delete Authority", description = "Deletes an authority from the Gate's local configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authority successfully deleted."),
            @ApiResponse(responseCode = "404", description = "Not Found. The specified authority does not exist."),
    })
    @DeleteMapping("/authority/delete/{authorityId}")
    ResponseEntity<String> deleteAuthority(@PathVariable String authorityId);

    @Operation(summary = "[For Admins] Register Platform", description = "Registers a new platform in the Gate's local configuration and generates an API key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Platform registered successfully. The response contains the generated API key.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformRegistrationResponseDto.class))),
    })
    @PostMapping(value = "/platform/register")
    ResponseEntity<PlatformRegistrationResponseDto> registerPlatform(
            @RequestBody @Validated PlatformRegistrationRequestDto platformRegistrationRequestDto
    );

    @Operation(summary = "[For Admins] Delete Platform", description = "Deletes a platform from the Gate's local configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Platform successfully deleted."),
            @ApiResponse(responseCode = "404", description = "Not Found. The specified platform does not exist."),
    })
    @DeleteMapping("/platform/delete/{platformId}")
    ResponseEntity<String> deletePlatform(@PathVariable String platformId);

    @Operation(summary = "[For Admins] Get System Metadata", description = "Retrieves lists of all registered gates, platforms, and authorities from the Gate's local configuration.")
    @GetMapping("/metadata")
    ResponseEntity<MetaDataDto> getMetadata();
}
