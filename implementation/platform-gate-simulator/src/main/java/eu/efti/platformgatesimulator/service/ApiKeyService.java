package eu.efti.platformgatesimulator.service;

import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.platformgatesimulator.dto.PlatformRegistrationRequestDto;
import eu.efti.platformgatesimulator.dto.PlatformRegistrationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class ApiKeyService {
    private final GateProperties gateProperties;
    private final String filePath;

    public ApiKeyService(GateProperties gateProperties) {
        this.gateProperties = gateProperties;
        filePath = "./api-key-" + gateProperties.getOwner() + ".txt";
    }

    private void fetchApiKeyAndSave() {
        RestClient restClient = RestClient.builder().build();

        PlatformRegistrationRequestDto body = new PlatformRegistrationRequestDto();
        body.setFollowUpRequestUrl(gateProperties.getPlatformBaseUrl() + "/gate-api/follow-up");
        body.setUilRequestUrl(gateProperties.getPlatformBaseUrl() + "/gate-api/consignments");
        body.setName(gateProperties.getOwner());

        ResponseEntity<PlatformRegistrationResponseDto> response = restClient.post()
                .uri(gateProperties.getGateBaseUrl() + "/api/v1/platforms")
                .header("X-API-Key", gateProperties.getGateSuperApiKey())
                .body(body)
                .retrieve()
                .toEntity(PlatformRegistrationResponseDto.class);

        if (response.getBody() != null) {
            PlatformRegistrationResponseDto responseBody = response.getBody();
            saveApiKeyToFile(responseBody.getName(), responseBody.getSecret());
            log.info("API key successfully fetched and saved for: {}", responseBody.getName());
        } else {
            log.error("Failed to fetch API key: response body is null");
        }
    }

    private void saveApiKeyToFile(String name, String secret) {
        try {
            Path filePath = Paths.get(this.filePath);
            String content = name + "_" + secret;
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("API key saved to file: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save API key to file", e);
        }
    }

    private boolean isApiKeyInFile() {
        try {
            Path filePath = Paths.get(this.filePath);
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath);
                if (content.isEmpty()) {
                    log.error("API key file is empty");
                    return false;
                } else {
                    log.info("API key file found with content: {}", content);
                    return true;
                }
            } else {
                log.warn("API key file not found");
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to read API key from file", e);
            return false;
        }
    }

    public String getApiKey() {
        if (!isApiKeyInFile()) {
            fetchApiKeyAndSave();
        }

        try {
            Path filePath = Paths.get(this.filePath);
            return Files.readString(filePath);
        } catch (IOException e) {
            log.error("Failed to read API key from file", e);
            return null;
        }
    }
}