package eu.efti.eftigate.controller.api;

import eu.efti.eftigate.dto.GateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<Void> registerGate(@RequestBody GateDto gateDto);

    @Operation(
            summary = "Delete gate.",
            description = "Delete gate from the gate's database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gate successfully deleted."),
            @ApiResponse(responseCode = "404", description = "Gate does not exist."),
    })
    @DeleteMapping("/gate/delete/{gateId}")
    ResponseEntity<Void> deleteGate(@PathVariable String gateId);
}
