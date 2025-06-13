package eu.efti.authorityapp.controller.api;

import eu.efti.authorityapp.dto.RequestIdDto;
import eu.efti.commons.dto.IdentifiersResponseDto;
import eu.efti.commons.dto.SearchWithIdentifiersRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Identifiers query", description = "Interface to search for identifiers")
@RequestMapping("/api/v1")
public interface IdentifiersControllerApi {
    @Operation(summary = "Send an identifiers query", description = "Send a query to retrieve identifiers matching the search criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @PostMapping("/identifiers")
    ResponseEntity<RequestIdDto> requestIdentifiers(final @RequestBody SearchWithIdentifiersRequestDto identifiersRequestDto);

    @Operation(summary = "Get a response to an identifiers query", description = "Get a response to an identifiers query for given request id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema()))
    })
    @GetMapping("/identifiers")
    ResponseEntity<IdentifiersResponseDto> getRequestIdentifiers(final @Parameter String requestId);
}
