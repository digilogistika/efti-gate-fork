package eu.efti.eftigate.controller.api;

import eu.efti.commons.dto.FollowUpRequestDto;
import eu.efti.eftigate.dto.NoteResponseDto;
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

@Tag(name = "[For Authorities]", description = "Endpoints used by Authorities to query and retrieve data from the Gate.")
@RequestMapping("/v1")
public interface NoteControllerApi {

    @Operation(summary = "[For Authorities] Send a Follow-up Note",
            description = "Accepts a follow-up communication and queues it for delivery to the target Platform via its eDelivery Access Point.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Accepted. The follow-up request has been successfully queued for delivery.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponseDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request. The request body is invalid or the target could not be found.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = NoteResponseDto.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The X-API-Key is missing or invalid.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden. The client is not allowed to access this endpoint.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. An unexpected error occurred on the server.", content = @Content)
    })
    @PostMapping("/follow-up")
    ResponseEntity<NoteResponseDto> createNote(final @RequestBody FollowUpRequestDto notesDto);
}
