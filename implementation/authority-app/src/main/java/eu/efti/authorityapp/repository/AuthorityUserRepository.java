package eu.efti.authorityapp.repository;

import eu.efti.authorityapp.entity.AuthorityUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityUserRepository extends JpaRepository<AuthorityUserEntity, Long> {
    boolean existsByEmail(String email);
    Optional<AuthorityUserEntity> findByEmail(String email);
}
