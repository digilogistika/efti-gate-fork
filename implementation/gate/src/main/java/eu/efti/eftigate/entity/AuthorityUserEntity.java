package eu.efti.eftigate.entity;

import eu.efti.eftigate.config.security.PermissionLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "authorityuser", catalog = "efti")
public class AuthorityUserEntity {
    @Id
    @Column(name = "authorityid", length = 255, nullable = false, unique = true)
    private String authorityId;

    @Column(name = "secret", length = 1024, nullable = false)
    private String secret;


    @Enumerated(EnumType.STRING)
    @Column(name = "permissionlevel", nullable = false)
    private PermissionLevel permissionLevel;
}
