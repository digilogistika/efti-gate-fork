package eu.efti.eftigate.service;

import eu.efti.eftigate.dto.PlatformRegistrationRequestDto;
import eu.efti.eftigate.dto.PlatformRegistrationResponseDto;
import eu.efti.eftigate.entity.PlatformEntity;
import eu.efti.eftigate.exception.PlatformRegistrationException;
import eu.efti.eftigate.repository.PlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;


@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformIdentityService {
    private final PlatformRepository platformRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PlatformRegistrationResponseDto registerPlatform(PlatformRegistrationRequestDto platformRegistrationRequestDto) {
        log.info("Registering platform with params: {}", platformRegistrationRequestDto);

        if (platformRepository.existsByPlatformId(platformRegistrationRequestDto.getPlatformId())) {
            log.warn("Registration failed: platform with platformId {} already exists", platformRegistrationRequestDto.getPlatformId());
            throw new PlatformRegistrationException("Platform with this platformId already exists");
        }

        byte[] randomBytes = new byte[128];
        secureRandom.nextBytes(randomBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).replaceAll("_", "-");
        String encodedSecret = passwordEncoder.encode(secret);

        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setPlatformId(platformRegistrationRequestDto.getPlatformId());
        platformEntity.setRequestBaseUrl(platformRegistrationRequestDto.getRequestBaseUrl());
        platformEntity.setSecret(encodedSecret);

        platformRepository.save(platformEntity);
        log.info("Platform {} registered successfully", platformRegistrationRequestDto.getPlatformId());

        PlatformRegistrationResponseDto responseDto = new PlatformRegistrationResponseDto();
        responseDto.setApiKey(platformRegistrationRequestDto.getPlatformId() + "_" + secret);
        return responseDto;
    }

    public String getPlatformIdFromHeader(String header) {
        String[] parts = header.split("_", 2);
        return parts[0];
    }

    public String getRequestBaseUrl(String platformId) {
        PlatformEntity platformEntity = platformRepository.findByPlatformId(platformId);
        if (platformEntity == null) {
            log.warn("Platform with ID {} does not exist", platformId);
            throw new RuntimeException("Platform with this ID does not exist");
        }
        return platformEntity.getRequestBaseUrl();
    }
}