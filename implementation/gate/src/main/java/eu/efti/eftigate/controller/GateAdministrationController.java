package eu.efti.eftigate.controller;

import eu.efti.eftigate.controller.api.GateAdministrationApi;
import eu.efti.eftigate.dto.GateDto;
import eu.efti.eftigate.service.gate.GateAdministrationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class GateAdministrationController implements GateAdministrationApi {
    private final GateAdministrationService gateAdministrationService;

    @Override
    public ResponseEntity<Void> registerGate(GateDto gateDto) {
        log.info("Gate registration request with id: {} and indicator: {}",
                gateDto.getGateId(), gateDto.getCountry());
        return gateAdministrationService.registerGate(gateDto);
    }

    @Override
    public ResponseEntity<Void> deleteGate(String gateId) {
        log.info("Gate deletion request with id: {}", gateId);
        return gateAdministrationService.deleteGate(gateId);
    }
}
