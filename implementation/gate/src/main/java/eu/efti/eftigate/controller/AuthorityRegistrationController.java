package eu.efti.eftigate.controller;

import eu.efti.eftigate.controller.api.AuthorityRegistrationApiV0;
import eu.efti.eftigate.dto.AuthorityUserRegistrationRequestDto;
import eu.efti.eftigate.dto.AuthorityUserRegistrationResponseDto;
import eu.efti.eftigate.service.AuthorityIdentityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/authority")
@Slf4j
public class AuthorityRegistrationController implements AuthorityRegistrationApiV0 {
    private final AuthorityIdentityService authorityIdentityService;

    @Override
    public ResponseEntity<AuthorityUserRegistrationResponseDto> registerAuthority(
            @RequestBody @Validated AuthorityUserRegistrationRequestDto authorityUserRegistrationRequestDto
    ) {
        log.info("POST on /api/authority/v0/register");

        return ResponseEntity.ok(authorityIdentityService.registerAuthorityUser(authorityUserRegistrationRequestDto));
    }
}
