package eu.efti.platformgatesimulator.service;

import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.platformgatesimulator.dto.PlatformRegistrationRequestDto;
import eu.efti.platformgatesimulator.dto.PlatformRegistrationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ApiKeyService {
    private final GateProperties gateProperties;
    private String apiKey = null;

    public ApiKeyService(GateProperties gateProperties) {
        this.gateProperties = gateProperties;
    }

    private String fetchApiKey() {
        RestClient restClient = RestClient.builder().build();

        PlatformRegistrationRequestDto body = new PlatformRegistrationRequestDto();
        body.setRequestBaseUrl(gateProperties.getPlatformBaseUrl() + "/v1/dataset");

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = today.format(formatter);
        body.setPlatformId(gateProperties.getOwner());

        ResponseEntity<PlatformRegistrationResponseDto> response = restClient.post()
                .uri(gateProperties.getGateBaseUrl() + "/api/admin/platform/register")
                .header("X-API-Key", gateProperties.getGateSuperApiKey())
                .body(body)
                .retrieve()
                .toEntity(PlatformRegistrationResponseDto.class);

        if (response.getBody() != null) {
            PlatformRegistrationResponseDto responseBody = response.getBody();
            log.info("API key successfully fetched: {}", gateProperties.getOwner());
            return responseBody.getApiKey();
        } else {
            log.error("Failed to fetch API key: response body is null");
        }
        return formattedDate;
    }


    public String getApiKey() {
        if (this.apiKey == null) {
            this.apiKey = fetchApiKey();
        }
        return this.apiKey;
    }
}