package eu.efti.authorityapp.controller.api;

import eu.efti.authorityapp.dto.NoteResponseDto;
import eu.efti.commons.dto.FollowUpRequestDto;
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

@Tag(name = "[For Authorities]", description = "Endpoint for authority users, proxied through the Authority App.")
@RequestMapping("/api/v1")
public interface NoteControllerApi {

    @Operation(summary = "[For Authorities] Send a Follow-up Note",
            description = "Forwards a follow-up communication to the Gate. This app acts as a proxy, and the response returned to the client is the direct response from the Gate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accepted. The Gate has accepted the request for delivery to the platform.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponseDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request. The Gate rejected the request due to invalid data.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponseDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The API key used by the Authority App to communicate with the Gate is invalid.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden. The client is not allowed to access this resource.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. An error occurred either within this app or in the Gate.", content = @Content)
    })
    @PostMapping("/follow-up")
    ResponseEntity<NoteResponseDto> createNote(final @RequestBody FollowUpRequestDto notesDto);
}
