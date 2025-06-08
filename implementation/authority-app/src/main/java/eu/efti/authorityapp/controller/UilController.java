package eu.efti.authorityapp.controller;

import eu.efti.authorityapp.config.GateProperties;
import eu.efti.authorityapp.controller.api.UilControllerApi;
import eu.efti.authorityapp.dto.RequestIdDto;
import eu.efti.authorityapp.service.ConfigService;
import eu.efti.commons.dto.UilDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class UilController implements UilControllerApi {
    private final GateProperties gateProperties;
    private final RestTemplate restTemplate;
    private final ConfigService configService;

    @Override
    @PostMapping("/control/uil")
    public ResponseEntity<RequestIdDto> requestUil(@RequestBody UilDto uilDto) {
        log.info("Forwarding POST request to gate for UIL: {}", uilDto);

        try {
            String gateUrl = gateProperties.getBaseUrl() + "/v1/control/uil";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", configService.getApiKey());
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            HttpEntity<UilDto> requestEntity = new HttpEntity<>(uilDto, headers);

            ResponseEntity<RequestIdDto> response = restTemplate.postForEntity(
                    gateUrl,
                    requestEntity,
                    RequestIdDto.class
            );
            log.info("Gate responded with status: {}", response.getStatusCode());

            return ResponseEntity.status(response.getStatusCode())
                    .body(response.getBody());

        } catch (Exception e) {
            log.error("Error forwarding request to gate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @GetMapping("/control/uil")
    public ResponseEntity<RequestIdDto> getRequestUil(@RequestParam String requestId) {
        log.info("Forwarding GET request to gate for requestId: {}", requestId);

        try {
            String gateUrl = gateProperties.getBaseUrl() + "/v1/control/uil?requestId=" + requestId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", configService.getApiKey());
            headers.set("Accept", "application/json");

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<RequestIdDto> response = restTemplate.exchange(
                    gateUrl,
                    HttpMethod.GET,
                    requestEntity,
                    RequestIdDto.class
            );
            log.info("Gate responded with status: {}", response.getStatusCode());

            return ResponseEntity.status(response.getStatusCode())
                    .body(response.getBody());

        } catch (Exception e) {
            log.error("Error forwarding request to gate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
