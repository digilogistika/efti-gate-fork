package eu.efti.eftigate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "platform", catalog = "efti")
public class PlatformEntity {
    @Id
    @Column(name = "platformid", length = 255, nullable = false, unique = true)
    private String platformId;

    // Although the url length is not limited by any standards, we limit it to 2048 characters here
    @Column(name = "requestbaseurl", length = 2048, nullable = false)
    private String requestBaseUrl;

    @Column(name = "secret", length = 1024, nullable = false)
    private String secret;
}
