package eu.efti.eftigate.repository;

import eu.efti.eftigate.entity.PlatformEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface PlatformRepository extends Repository<PlatformEntity, Long> {
    boolean existsByPlatformId(@NotNull String platformId);

    void save(PlatformEntity platformEntity);

    PlatformEntity findByPlatformId(String platformId);

    List<PlatformEntity> findAll();

    void deleteByPlatformId(String platformId);
}
