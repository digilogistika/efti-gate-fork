package eu.efti.eftigate.service.gate;

import eu.efti.eftigate.dto.GateDto;
import eu.efti.eftigate.entity.GateEntity;
import eu.efti.eftigate.repository.GateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GateAdministrationService {
    private final GateRepository gateRepository;

    public ResponseEntity<Void> registerGate(GateDto gateDto) {
        GateEntity gateEntity = gateRepository.findByGateId(gateDto.getGateId());

        if (gateEntity == null) {
            GateEntity newGateEntity = GateEntity.builder()
                    .gateId(gateDto.getGateId())
                    .country(gateDto.getCountry())
                    .build();
            gateRepository.save(newGateEntity);
            log.info("Gate {} added successfully", gateDto.getGateId());
            return ResponseEntity.ok().build();
        } else {
            log.warn("Gate {} already exists", gateDto.getGateId());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    public ResponseEntity<Void> deleteGate(String gateId) {
        GateEntity gateEntity = gateRepository.findByGateId(gateId);

        if (gateEntity != null) {
            gateRepository.delete(gateEntity);
            log.info("Gate {} deleted successfully", gateId);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Gate {} does not exist", gateId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
