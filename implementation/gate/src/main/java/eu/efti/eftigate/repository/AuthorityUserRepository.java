package eu.efti.eftigate.repository;

import eu.efti.eftigate.entity.AuthorityUserEntity;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface AuthorityUserRepository extends Repository<AuthorityUserEntity, Long> {
    boolean existsByAuthorityId(String authorityId);

    Optional<AuthorityUserEntity> findByAuthorityId(String authorityId);

    void save(AuthorityUserEntity authorityUserEntity);

    List<AuthorityUserEntity> findAll();
}
