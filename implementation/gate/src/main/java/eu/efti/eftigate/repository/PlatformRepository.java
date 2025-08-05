package eu.efti.eftigate.repository;

import eu.efti.eftigate.entity.PlatformEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PlatformRepository extends JpaRepository<PlatformEntity, Long> {
    boolean existsByPlatformId(@NotNull String platformId);

    PlatformEntity findByPlatformId(String platformId);

    @Transactional
    void deleteByPlatformId(String platformId);
}
