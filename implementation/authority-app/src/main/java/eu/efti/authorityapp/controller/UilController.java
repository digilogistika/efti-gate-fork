package eu.efti.authorityapp.controller;

import eu.efti.authorityapp.config.GateProperties;
import eu.efti.authorityapp.controller.api.UilControllerApi;
import eu.efti.authorityapp.dto.RequestIdDto;
import eu.efti.authorityapp.service.ConfigService;
import eu.efti.commons.dto.UilDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    @PostMapping("/dataset")
    public ResponseEntity<RequestIdDto> requestUil(@RequestBody UilDto uilDto) {
        log.info("Forwarding POST request to gate for UIL: {}", uilDto);

        try {
            String gateUrl = gateProperties.getBaseUrl() + "/v1/dataset";

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
    @GetMapping("/dataset")
    public ResponseEntity<RequestIdDto> getRequestUil(@RequestParam String requestId) {
        log.info("Forwarding GET request to gate for requestId: {}", requestId);

        try {
            String gateUrl = gateProperties.getBaseUrl() + "/v1/dataset?requestId=" + requestId;

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
