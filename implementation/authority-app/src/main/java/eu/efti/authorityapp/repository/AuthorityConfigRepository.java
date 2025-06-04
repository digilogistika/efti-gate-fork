package eu.efti.authorityapp.repository;

import eu.efti.authorityapp.entity.AuthorityConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityConfigRepository extends JpaRepository<AuthorityConfigEntity, String> {

    @Query("SELECT a FROM AuthorityConfigEntity a")
    Optional<AuthorityConfigEntity> getConfig();

    @Query("SELECT COUNT(a) > 0 FROM AuthorityConfigEntity a")
    boolean hasConfig();

}
