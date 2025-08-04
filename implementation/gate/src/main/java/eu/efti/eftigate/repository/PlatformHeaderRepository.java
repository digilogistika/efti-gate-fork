package eu.efti.eftigate.repository;

import eu.efti.eftigate.entity.PlatformEntity;
import eu.efti.eftigate.entity.PlatformHeaderEntity;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface PlatformHeaderRepository extends Repository<PlatformHeaderEntity, Long> {
    List<PlatformHeaderEntity> findAllByPlatform(PlatformEntity platformEntity);
    void delete(PlatformHeaderEntity platformHeaderEntity);
}
