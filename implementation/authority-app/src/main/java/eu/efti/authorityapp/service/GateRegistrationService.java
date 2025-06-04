package eu.efti.authorityapp.service;

import eu.efti.authorityapp.config.AuthorityAppProperties;
import eu.efti.authorityapp.config.GateProperties;
import eu.efti.authorityapp.config.security.PermissionLevel;
import eu.efti.authorityapp.dto.AuthorityUserRegistrationRequestDto;
import eu.efti.authorityapp.dto.AuthorityUserRegistrationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GateRegistrationService {

    private final AuthorityAppProperties authorityAppProperties;
    private final GateProperties gateProperties;
    private final RestTemplate restTemplate;
    private final ConfigService configService;

    @EventListener(ApplicationReadyEvent.class)
    public void registerWithGateOnStartup() {
        log.info("Application started - checking gate registration...");

        if (configService.hasApiKey()) {
            log.info("API key already exists in database - skipping registration");
            return;
        }

        log.info("No API key found - registering with gate...");
        registerWithGate();
    }

    private void registerWithGate() {
        log.info("Application started - registering with gate...");

        try {
            String registrationUrl = gateProperties.getBaseUrl() + "/api/authority/v0/register";

            AuthorityUserRegistrationRequestDto registrationRequest = AuthorityUserRegistrationRequestDto.builder()
                    .authorityId(authorityAppProperties.getId())
                    .permissionLevel(PermissionLevel.AUTHORITY_ACCESS_POINT)
                    .build();

            log.info("Registering authority '{}' with gate at: {}", authorityAppProperties.getId(), registrationUrl);

            ResponseEntity<AuthorityUserRegistrationResponseDto> response = restTemplate.postForEntity(
                    registrationUrl,
                    registrationRequest,
                    AuthorityUserRegistrationResponseDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AuthorityUserRegistrationResponseDto responseBody = response.getBody();
                configService.saveApiKey(responseBody.getApiKey());

                log.info("Successfully registered with gate. API key retrieved.");
                log.debug("Registration response: {}", responseBody);
            } else {
                log.error("Failed to register with gate. Status: {}", response.getStatusCode());
                throw new RuntimeException("Gate registration failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error during gate registration", e);
            throw new RuntimeException("Failed to register with gate during startup", e);
        }
    }
}
