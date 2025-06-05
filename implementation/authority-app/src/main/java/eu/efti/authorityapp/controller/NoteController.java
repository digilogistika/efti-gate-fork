package eu.efti.authorityapp.controller;

import eu.efti.authorityapp.config.GateProperties;
import eu.efti.authorityapp.controller.api.NoteControllerApi;
import eu.efti.authorityapp.dto.NoteResponseDto;
import eu.efti.authorityapp.dto.RequestIdDto;
import eu.efti.authorityapp.service.ConfigService;
import eu.efti.commons.dto.PostFollowUpRequestDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
@Slf4j
public class NoteController implements NoteControllerApi {

    private final GateProperties gateProperties;
    private final ConfigService configService;
    private final RestTemplate restTemplate;

    @Override
    @PostMapping("/control/uil/follow-up")
    public ResponseEntity<NoteResponseDto> createNote(final @RequestBody PostFollowUpRequestDto notesDto) {
        log.info("Forwarding POST request to gate for Note: {}", notesDto);

        try {
            String gateUrl = gateProperties.getBaseUrl() + "/v1/control/uil";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", configService.getApiKey());
            headers.set("Content-Type", "application/json");

            HttpEntity<PostFollowUpRequestDto> requestEntity = new HttpEntity<>(notesDto, headers);

            ResponseEntity<NoteResponseDto> response = restTemplate.postForEntity(
                    gateUrl,
                    requestEntity,
                    NoteResponseDto.class
            );

            log.info("Gate responded with status: {}", response.getStatusCode());
            return response;

        } catch (Exception e) {
            log.error("Error forwarding request to gate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
