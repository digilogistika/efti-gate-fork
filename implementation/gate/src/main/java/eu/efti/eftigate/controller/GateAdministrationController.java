package eu.efti.eftigate.controller;

import eu.efti.eftigate.controller.api.GateAdministrationApi;
import eu.efti.eftigate.dto.*;
import eu.efti.eftigate.service.AuthorityIdentityService;
import eu.efti.eftigate.service.PlatformIdentityService;
import eu.efti.eftigate.service.gate.GateAdministrationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class GateAdministrationController implements GateAdministrationApi {
    private final GateAdministrationService gateAdministrationService;
    private final AuthorityIdentityService authorityIdentityService;
    private PlatformIdentityService platformIdentityService;

    @Override
    public ResponseEntity<Void> registerGate(GateDto gateDto) {
        log.info("POST on /api/admin/gate/register with id: {} and indicator: {}",
                gateDto.getGateId(), gateDto.getCountry());
        return gateAdministrationService.registerGate(gateDto);
    }

    @Override
    public ResponseEntity<Void> deleteGate(String gateId) {
        log.info("DELETE on /api/admin/gate/delete with id: {}", gateId);
        return gateAdministrationService.deleteGate(gateId);
    }

    @Override
    public ResponseEntity<AuthorityUserRegistrationResponseDto> registerAuthority(
            AuthorityUserRegistrationRequestDto authorityUserRegistrationRequestDto) {
        log.info("POST on /api/admin/authority/register");
        return ResponseEntity.ok(authorityIdentityService.registerAuthorityUser(authorityUserRegistrationRequestDto));
    }

    @Override
    public ResponseEntity<PlatformRegistrationResponseDto> registerPlatform(
            PlatformRegistrationRequestDto platformRegistrationRequestDto) {
        log.info("POST on /api/admin/platform/register");
        PlatformRegistrationResponseDto platformRegistrationResponseDto = platformIdentityService.registerPlatform(platformRegistrationRequestDto);
        return ResponseEntity.ok(platformRegistrationResponseDto);
    }
}
