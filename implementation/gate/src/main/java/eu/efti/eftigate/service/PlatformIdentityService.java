package eu.efti.eftigate.service;

import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.PlatformRegistrationRequestDto;
import eu.efti.eftigate.dto.PlatformRegistrationResponseDto;
import eu.efti.eftigate.entity.PlatformEntity;
import eu.efti.eftigate.exception.PlatformRegistrationException;
import eu.efti.eftigate.exception.XApiKeyValidationexception;
import eu.efti.eftigate.repository.PlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformIdentityService {
    private final PlatformRepository platformRepository;
    private final PasswordEncoder passwordEncoder;
    private final GateProperties gateProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public PlatformRegistrationResponseDto registerPlatform(PlatformRegistrationRequestDto platformRegistrationRequestDto) {
        log.info("Registering platform with params: {}", platformRegistrationRequestDto);

        if (platformRepository.existsByName(platformRegistrationRequestDto.getName())) {
            log.warn("Registration failed: platform with name {} already exists", platformRegistrationRequestDto.getName());
            throw new PlatformRegistrationException("Platform with this name already exists");
        }

        byte[] randomBytes = new byte[128];
        secureRandom.nextBytes(randomBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).replaceAll("_", "-");
        String encodedSecret = passwordEncoder.encode(secret);

        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setName(platformRegistrationRequestDto.getName());
        platformEntity.setUilRequestUrl(platformRegistrationRequestDto.getUilRequestUrl());
        platformEntity.setFollowupRequestUrl(platformRegistrationRequestDto.getFollowUpRequestUrl());
        platformEntity.setSecret(encodedSecret);

        platformRepository.save(platformEntity);
        log.info("Platform {} registered successfully", platformRegistrationRequestDto.getName());

        PlatformRegistrationResponseDto responseDto = new PlatformRegistrationResponseDto();
        responseDto.setName(platformRegistrationRequestDto.getName());
        responseDto.setSecret(secret);
        return responseDto;
    }

    public String getPlatformNameFromHeader(String header) {
        String[] parts = header.split("_", 2);
        return parts[0];
    }

    public String getUilRequestUrl(String platformName) {
        PlatformEntity platformEntity = platformRepository.findByName(platformName);
        if (platformEntity == null) {
            log.warn("Platform with name {} does not exist", platformName);
            throw new RuntimeException("Platform with this name does not exist");
        }
        return platformEntity.getUilRequestUrl();
    }

    public String getFollowUpRequestUrl(String platformName) {
        PlatformEntity platformEntity = platformRepository.findByName(platformName);
        if (platformEntity == null) {
            log.warn("Platform with name {} does not exist", platformName);
            throw new RuntimeException("Platform with this name does not exist");
        }
        return platformEntity.getFollowupRequestUrl();
    }

    public void validateXApiKeyHeader(String header) {
        String[] parts = header.split("_", 2);
        if (parts.length != 2) {
            log.warn("Platform validation failed: invalid header format");
            throw new XApiKeyValidationexception("Invalid header format");
        }

        String name = parts[0];
        String secret = parts[1];

        if (name.isEmpty() || secret.isEmpty()) {
            log.warn("Platform validation failed: name or secret is empty");
            throw new XApiKeyValidationexception("Name or secret is empty");
        }

        validatePlatform(name, secret);
    }

    private void validatePlatform(String name, String secret) {
        if (Objects.equals(name, "admin")) {
            if (!Objects.equals(secret, gateProperties.getApikey())) {
                log.warn("Admin platform validation failed: invalid secret");
                throw new XApiKeyValidationexception("Invalid secret for admin platform");
            }
            log.info("Admin platform validated successfully");
            return;
        }

        PlatformEntity platformEntity = platformRepository.findByName(name);

        if (platformEntity == null) {
            log.warn("Platform validation failed: platform with name {} does not exist", name);
            throw new XApiKeyValidationexception("Platform with this name does not exist");
        }

        boolean isValid = passwordEncoder.matches(secret, platformEntity.getSecret());

        if (isValid) {
            log.info("Platform {} validated successfully", name);
        } else {
            log.warn("Platform validation failed: invalid secret for platform {}", name);
            throw new XApiKeyValidationexception("Invalid secret for platform");
        }
    }
}