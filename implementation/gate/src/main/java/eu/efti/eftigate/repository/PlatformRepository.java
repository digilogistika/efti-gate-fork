package eu.efti.eftigate.repository;

import eu.efti.eftigate.entity.PlatformEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.Repository;


public interface PlatformRepository extends Repository<PlatformEntity, Long> {
    boolean existsByPlatformId(@NotNull String platformId);

    void save(PlatformEntity platformEntity);

    PlatformEntity findByPlatformId(String platformId);
}
