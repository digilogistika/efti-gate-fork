package eu.efti.eftigate.service.gate;

import eu.efti.commons.enums.CountryIndicator;
import eu.efti.eftigate.config.GateProperties;
import eu.efti.eftigate.dto.GateDto;
import eu.efti.eftigate.dto.MetaDataDto;
import eu.efti.eftigate.entity.AuthorityUserEntity;
import eu.efti.eftigate.entity.GateEntity;
import eu.efti.eftigate.entity.PlatformEntity;
import eu.efti.eftigate.exception.GateAlreadyExistsException;
import eu.efti.eftigate.exception.GateDoesNotExistException;
import eu.efti.eftigate.repository.AuthorityUserRepository;
import eu.efti.eftigate.repository.GateRepository;
import eu.efti.eftigate.repository.PlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GateAdministrationService {
    private final GateRepository gateRepository;
    private final GateProperties gateProperties;
    private final PlatformRepository platformRepository;
    private final AuthorityUserRepository authorityUserRepository;


    @EventListener(ApplicationReadyEvent.class)
    public void setSelfIndicator() {
        GateEntity gateEntity = GateEntity.builder()
                .gateId(gateProperties.getOwner())
                .country(CountryIndicator.valueOf(gateProperties.getCountry().toUpperCase()))
                .build();
        if (gateRepository.findByGateId(gateProperties.getOwner()) == null) {
            log.info("Setting self indicator to {}.", gateProperties.getCountry());
            gateRepository.save(gateEntity);
        } else {
            log.info("Indicator already set.");
        }
    }

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

    public MetaDataDto getMetadata() {
        log.info("Fetching metadata (IDs and names)");

        List<String> gateIds = gateRepository.findAll().stream()
                .map(GateEntity::getGateId)
                .collect(Collectors.toList());

        List<String> platformIds = platformRepository.findAll().stream()
                .map(PlatformEntity::getPlatformId)
                .collect(Collectors.toList());

        List<String> authorityNames = authorityUserRepository.findAll().stream()
                .map(AuthorityUserEntity::getAuthorityId)
                .collect(Collectors.toList());

        return MetaDataDto.builder()
                .gateIds(gateIds)
                .platformIds(platformIds)
                .authorityNames(authorityNames)
                .build();
    }
}
