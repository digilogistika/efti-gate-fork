package eu.efti.eftigate.controller;

import eu.efti.eftigate.dto.PlatformRegistrationRequestDto;
import eu.efti.eftigate.dto.PlatformRegistrationResponseDto;
import eu.efti.eftigate.exception.XApiKeyValidationexception;
import eu.efti.eftigate.service.PlatformIdentityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/platform-registration")
@Slf4j
public class PlatformRegistrationController {
    private PlatformIdentityService platformIdentityService;

    @PostMapping
    public ResponseEntity<PlatformRegistrationResponseDto> registerPlatform(
            @RequestBody PlatformRegistrationRequestDto platformRegistrationRequestDto,
            @RequestHeader("X-API-Key") String apiKey
    ) {
        log.info("POST on /api/v1/platform-registration");
        try {
            platformIdentityService.validateXApiKeyHeader(apiKey);
        } catch (XApiKeyValidationexception e) {
            log.error("X-API-Key validation failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(null);
        }

        PlatformRegistrationResponseDto platformRegistrationResponseDto = platformIdentityService.registerPlatform(platformRegistrationRequestDto);
        return ResponseEntity.ok(platformRegistrationResponseDto);
    }
}
