package eu.efti.authorityapp.controller;

import eu.efti.authorityapp.config.GateProperties;
import eu.efti.authorityapp.controller.api.IdentifiersControllerApi;
import eu.efti.authorityapp.service.ConfigService;
import eu.efti.commons.dto.IdentifiersResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
@Slf4j
public class IdentifiersController implements IdentifiersControllerApi {
    private final GateProperties gateProperties;
    private final RestTemplate restTemplate;
    private final ConfigService configService;

    @Override
    @GetMapping("/identifiers/{identifier}")
    public ResponseEntity<IdentifiersResponseDto> getIdentifiers(
            String identifier,
            String modeCode,
            List<String> identifierType,
            String registrationCountryCode,
            Boolean dangerousGoodsIndicator,
            List<String> eftiGateIndicator) {
        log.info("Querying gate for identifiers with identifier: {}, modeCode: {}, identifierType: {}, registrationCountryCode: {}, dangerousGoodsIndicator: {}, eftiGateIndicator: {}",
                identifier, modeCode, identifierType, registrationCountryCode, dangerousGoodsIndicator, eftiGateIndicator);

        List<String> queryBuilder = new ArrayList<>();

        if (modeCode != null) {
            queryBuilder.add("modeCode=" + modeCode);
        }
        if (identifierType != null && !identifierType.isEmpty()) {
            queryBuilder.add("identifierType=" + String.join(",", identifierType));
        }
        if (registrationCountryCode != null) {
            queryBuilder.add("registrationCountryCode=" + registrationCountryCode);
        }
        if (dangerousGoodsIndicator != null) {
            queryBuilder.add("dangerousGoodsIndicator=" + dangerousGoodsIndicator);
        }
        if (eftiGateIndicator != null && !eftiGateIndicator.isEmpty()) {
            queryBuilder.add("eftiGateIndicator=" + String.join(",", eftiGateIndicator));
        }

        String queryParams = String.join("&", queryBuilder);

        String queryUrl = UriComponentsBuilder
                .newInstance()
                .scheme(gateProperties.getBaseUrl().getProtocol())
                .host(gateProperties.getBaseUrl().getHost())
                .port(gateProperties.getBaseUrl().getPort())
                .pathSegment("v1", "identifiers", identifier)
                .query(queryParams)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", configService.getApiKey());
        headers.set("Accept", "application/json");

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        try {
            log.info("Sending GET request for identifiers to gate at: {}", queryUrl);
            ResponseEntity<IdentifiersResponseDto> response = restTemplate.exchange(
                    queryUrl,
                    HttpMethod.GET,
                    requestEntity,
                    IdentifiersResponseDto.class
            );
            log.info("Received response from gate with status: {}", response.getStatusCode());

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());
        } catch (Exception e) {
            log.error("Error querying gate for identifiers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
