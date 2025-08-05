package eu.efti.eftigate.repository;

import eu.efti.eftigate.entity.AuthorityUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorityUserRepository extends JpaRepository<AuthorityUserEntity, Long> {
    boolean existsByAuthorityId(String authorityId);

    Optional<AuthorityUserEntity> findByAuthorityId(String authorityId);
}
