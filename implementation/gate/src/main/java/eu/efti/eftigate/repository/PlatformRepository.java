package eu.efti.eftigate.repository;

import eu.efti.eftigate.entity.PlatformEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.Repository;


public interface PlatformRepository extends Repository<PlatformEntity, Long> {
    boolean existsByName(@NotNull String name);

    void save(PlatformEntity platformEntity);

    PlatformEntity findByName(String name);
}
