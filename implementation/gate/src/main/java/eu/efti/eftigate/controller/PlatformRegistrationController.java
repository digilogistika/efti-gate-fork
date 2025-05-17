package eu.efti.eftigate.controller;

import eu.efti.eftigate.controller.api.PlatformRegistrationApiV1;
import eu.efti.eftigate.dto.PlatformRegistrationRequestDto;
import eu.efti.eftigate.dto.PlatformRegistrationResponseDto;
import eu.efti.eftigate.service.PlatformIdentityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/platforms")
@Slf4j
public class PlatformRegistrationController implements PlatformRegistrationApiV1 {
    private PlatformIdentityService platformIdentityService;

    public ResponseEntity<PlatformRegistrationResponseDto> registerPlatform(
            @RequestBody PlatformRegistrationRequestDto platformRegistrationRequestDto
    ) {
        log.info("POST on /api/v1/platforms");

        PlatformRegistrationResponseDto platformRegistrationResponseDto = platformIdentityService.registerPlatform(platformRegistrationRequestDto);
        return ResponseEntity.ok(platformRegistrationResponseDto);
    }
}
