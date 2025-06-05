package eu.efti.authorityapp.controller.api;

import eu.efti.authorityapp.dto.RequestIdDto;
import eu.efti.commons.dto.UilDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "UIL query", description = "Interface to manage dataset request")
public interface UilControllerApi {

    @Operation(summary = "Send an UIL query", description = "Send a query for given UIL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @PostMapping("/control/uil")
    ResponseEntity<RequestIdDto> requestUil(@RequestBody UilDto uilDto);


    @Operation(summary = "Get a response to an UIL query", description = "Get a dataset for a given request id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @GetMapping("/control/uil")
    ResponseEntity<RequestIdDto> getRequestUil(@RequestParam String requestId);
}
