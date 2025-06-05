package eu.efti.authorityapp.controller.api;

import eu.efti.authorityapp.dto.NoteResponseDto;
import eu.efti.commons.dto.PostFollowUpRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Follow up communication", description = "Interface to send a follow up communication to a platform ")
public interface NoteControllerApi {

    @Operation(summary = "Send follow up communication", description = "Send a follow up communication to a platform for a given control")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accepted"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/control/uil/follow-up")
    ResponseEntity<NoteResponseDto> createNote(final @RequestBody PostFollowUpRequestDto notesDto);
}
