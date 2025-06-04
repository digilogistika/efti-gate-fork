package eu.efti.authorityapp.controller;

import eu.efti.authorityapp.config.GateProperties;
import eu.efti.authorityapp.controller.api.IdentifiersControllerApi;
import eu.efti.authorityapp.dto.RequestIdDto;
import eu.efti.authorityapp.service.ConfigService;
import eu.efti.commons.dto.IdentifiersResponseDto;
import eu.efti.commons.dto.SearchWithIdentifiersRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
@Slf4j
public class IdentifiersController implements IdentifiersControllerApi {
    private final GateProperties gateProperties;
    private final RestTemplate restTemplate;
    private final ConfigService configService;

    private final String gateApiKey = configService.getApiKey();

    @Override
    @PostMapping("/control/identifiers")
    public ResponseEntity<RequestIdDto> requestIdentifiers(@RequestBody SearchWithIdentifiersRequestDto searchIdentifiersDto) {
        log.info("Forwarding POST request to gate for identifier: {}", searchIdentifiersDto);

        try {
            String gateUrl = gateProperties.getBaseUrl() + "/v1/control/uil";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", gateApiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<SearchWithIdentifiersRequestDto> requestEntity = new HttpEntity<>(searchIdentifiersDto, headers);

            ResponseEntity<RequestIdDto> response = restTemplate.postForEntity(
                    gateUrl,
                    requestEntity,
                    RequestIdDto.class
            );

            log.info("Gate responded with status: {}", response.getStatusCode());
            return response;

        } catch (Exception e) {
            log.error("Error forwarding request to gate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @GetMapping("/control/identifiers")
    public ResponseEntity<IdentifiersResponseDto> getRequestIdentifiers(@RequestParam String requestId) {
        log.info("Forwarding GET request to gate for requestId: {}", requestId);

        try {
            String gateUrl = gateProperties.getBaseUrl() + "/v1/control/uil?requestId=" + requestId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-Key", gateApiKey);

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<IdentifiersResponseDto> response = restTemplate.exchange(
                    gateUrl,
                    HttpMethod.GET,
                    requestEntity,
                    IdentifiersResponseDto.class
            );

            log.info("Gate responded with status: {}", response.getStatusCode());
            return response;

        } catch (Exception e) {
            log.error("Error forwarding request to gate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
