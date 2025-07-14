package eu.efti.eftigate.service;

import eu.efti.eftigate.dto.PlatformHeaderDto;
import eu.efti.eftigate.dto.PlatformRegistrationRequestDto;
import eu.efti.eftigate.dto.PlatformRegistrationResponseDto;
import eu.efti.eftigate.entity.PlatformEntity;
import eu.efti.eftigate.entity.PlatformHeaderEntity;
import eu.efti.eftigate.exception.PlatformAlreadyExistsException;
import eu.efti.eftigate.mapper.MapperUtils;
import eu.efti.eftigate.repository.PlatformHeaderRepository;
import eu.efti.eftigate.repository.PlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformIdentityService {
    private final PlatformRepository platformRepository;
    private final PlatformHeaderRepository platformHeaderRepository;
    private final MapperUtils mapperUtils;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PlatformRegistrationResponseDto registerPlatform(PlatformRegistrationRequestDto platformRegistrationRequestDto) {
        log.info("Registering platform with ID: {}", platformRegistrationRequestDto.getPlatformId());

        if (platformRepository.existsByPlatformId(platformRegistrationRequestDto.getPlatformId())) {
            log.info("Overwriting platform");
            platformRepository.deleteByPlatformId(platformRegistrationRequestDto.getPlatformId());
        }

        byte[] randomBytes = new byte[128];
        secureRandom.nextBytes(randomBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).replaceAll("_", "-");
        String encodedSecret = passwordEncoder.encode(secret);

        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setPlatformId(platformRegistrationRequestDto.getPlatformId());
        platformEntity.setRequestBaseUrl(platformRegistrationRequestDto.getRequestBaseUrl());
        platformEntity.setSecret(encodedSecret);

        if (platformRegistrationRequestDto.getHeaders() != null && !platformRegistrationRequestDto.getHeaders().isEmpty()) {
            List<PlatformHeaderEntity> headers = mapperUtils.headerDtoListToHeaderEntityList(platformRegistrationRequestDto.getHeaders());
            headers.forEach(h -> h.setPlatform(platformEntity));
            platformEntity.setHeaders(headers);
        }

        platformRepository.save(platformEntity);
        log.info("Platform {} registered successfully", platformRegistrationRequestDto.getPlatformId());

        PlatformRegistrationResponseDto responseDto = new PlatformRegistrationResponseDto();
        responseDto.setApiKey(platformRegistrationRequestDto.getPlatformId() + "_" + secret);
        return responseDto;
    }

    public List<PlatformHeaderDto> getPlatformRequestHeaders(String platformId) {
        PlatformEntity platformEntity = platformRepository.findByPlatformId(platformId);
        if (platformEntity == null) return List.of();

        List<PlatformHeaderEntity> platformHeaderEntities = platformHeaderRepository.findAllByPlatform(platformEntity);
        return mapperUtils.headerEntityListToHeaderDtoList(platformHeaderEntities);
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