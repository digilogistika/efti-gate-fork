package eu.efti.authorityapp.repository;

import eu.efti.authorityapp.entity.AuthorityUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthorityUserRepository extends JpaRepository<AuthorityUserEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<AuthorityUserEntity> findByEmail(String email);
}
