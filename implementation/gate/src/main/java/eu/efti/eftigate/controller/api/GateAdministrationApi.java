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

import java.util.List;

@Tag(name = "Gate administration api", description = "API for gate's administrator.")
@RequestMapping("/api/admin")
public interface GateAdministrationApi {

    @Operation(
            summary = "Add new gate.",
            description = "Add new gate to gate's database."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gate successfully added."),
            @ApiResponse(responseCode = "409", description = "Gate already exists."),
    })
    @PostMapping("/gate/register")
    ResponseEntity<String> registerGate(@RequestBody @Validated GateDto gateDto);

    @Operation(
            summary = "Delete gate.",
            description = "Delete gate from the gate's database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gate successfully deleted."),
            @ApiResponse(responseCode = "404", description = "Gate does not exist."),
    })
    @DeleteMapping("/gate/delete/{gateId}")
    ResponseEntity<String> deleteGate(@PathVariable String gateId);

    @Operation(
            summary = "Register an authority to the system",
            description = "User will be registered and a response will be returned with the API key that can be used by the authority."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "authority registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthorityUserRegistrationResponseDto.class))),
            }
    )
    @PostMapping(value = "/authority/register")
    ResponseEntity<AuthorityUserRegistrationResponseDto> registerAuthority(
            @RequestBody @Validated AuthorityUserRegistrationRequestDto authorityUserRegistrationRequestDto
    );

    @Operation(
            summary = "Register a new platform to the system",
            description = "Add a new platform with its endpoint information to the system. The platform will be registered and a response will be returned with the API key that can be used by the platform."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Platform registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformRegistrationResponseDto.class))),
            }
    )
    @PostMapping(value = "/platform/register")
    ResponseEntity<PlatformRegistrationResponseDto> registerPlatform(
            @RequestBody @Validated PlatformRegistrationRequestDto platformRegistrationRequestDto
    );

    @Operation(
            summary = "Get all gates.",
            description = "Get all registered gates from the database."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gates successfully retrieved.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GateDto.class)))
    })
    @GetMapping("/gates")
    ResponseEntity<List<GateDto>> getGates();
}
