package eu.efti.eftigate.service;

import eu.efti.eftigate.dto.AuthorityUserRegistrationRequestDto;
import eu.efti.eftigate.dto.AuthorityUserRegistrationResponseDto;
import eu.efti.eftigate.entity.AuthorityUserEntity;
import eu.efti.eftigate.exception.AuthorityUserAlreadyExistsException;
import eu.efti.eftigate.repository.AuthorityUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorityIdentityService {
    private final AuthorityUserRepository authorityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthorityUserRegistrationResponseDto registerAuthorityUser(AuthorityUserRegistrationRequestDto authorityUserRegistrationRequestDto) {
        log.info("Registering authority user with params: {}", authorityUserRegistrationRequestDto);

        if (authorityUserRepository.existsByAuthorityId(authorityUserRegistrationRequestDto.getAuthorityId())) {
            log.warn("Registration failed: authority user with ID {} already exists", authorityUserRegistrationRequestDto.getAuthorityId());
            throw new AuthorityUserAlreadyExistsException("Authority user with this ID already exists");
        }

        byte[] randomBytes = new byte[128];
        secureRandom.nextBytes(randomBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).replaceAll("_", "-");
        String encodedSecret = passwordEncoder.encode(secret);

        AuthorityUserEntity authorityUserEntity = new AuthorityUserEntity();
        authorityUserEntity.setAuthorityId(authorityUserRegistrationRequestDto.getAuthorityId());
        authorityUserEntity.setPermissionLevel(authorityUserRegistrationRequestDto.getPermissionLevel());
        authorityUserEntity.setSecret(encodedSecret);

        authorityUserRepository.save(authorityUserEntity);
        log.info("Authority with ID {} registered successfully", authorityUserRegistrationRequestDto.getAuthorityId());

        AuthorityUserRegistrationResponseDto responseDto = new AuthorityUserRegistrationResponseDto();
        responseDto.setApiKey(authorityUserRegistrationRequestDto.getAuthorityId() + "_" + secret);
        return responseDto;
    }

    public String deleteAuthority(final String authorityId) {
        log.info("Attempting to delete authority with ID: {}", authorityId);

        final AuthorityUserEntity authorityUser = this.authorityUserRepository.findByAuthorityId(authorityId)
                .orElseThrow(() -> {
                    log.warn("Authority with ID {} does not exist", authorityId);
                    return new RuntimeException("Authority not found with id: " + authorityId);
                });

        this.authorityUserRepository.delete(authorityUser);
        log.info("Successfully deleted authority with ID: {}", authorityId);
        return "Authority with ID " + authorityId + " was deleted successfully.";
    }
}
