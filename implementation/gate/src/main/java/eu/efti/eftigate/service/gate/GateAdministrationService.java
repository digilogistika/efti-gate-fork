package eu.efti.eftigate.service.gate;

import eu.efti.eftigate.dto.GateDto;
import eu.efti.eftigate.entity.GateEntity;
import eu.efti.eftigate.exception.GateAlreadyExistsException;
import eu.efti.eftigate.exception.GateDoesNotExistException;
import eu.efti.eftigate.repository.GateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GateAdministrationService {
    private final GateRepository gateRepository;

    public String registerGate(GateDto gateDto) {
        GateEntity gateEntity = gateRepository.findByGateId(gateDto.getGateId());

        if (gateEntity == null) {
            GateEntity newGateEntity = GateEntity.builder()
                    .gateId(gateDto.getGateId())
                    .country(gateDto.getCountry())
                    .build();
            gateRepository.save(newGateEntity);
            log.info("Gate {} added successfully", gateDto.getGateId());
            return String.format("Gate %s added successfully", gateDto.getGateId());
        } else {
            log.warn("Gate {} already exists", gateDto.getGateId());
            throw new GateAlreadyExistsException("Gate with this ID already exists");
        }
    }

    public String deleteGate(String gateId) {
        GateEntity gateEntity = gateRepository.findByGateId(gateId);

        if (gateEntity != null) {
            gateRepository.delete(gateEntity);
            log.info("Gate {} deleted successfully", gateId);
            return String.format("Gate %s deleted successfully", gateId);
        } else {
            log.warn("Gate {} does not exist", gateId);
            throw new GateDoesNotExistException("Gate with this ID does not exist");
        }
    }
}
