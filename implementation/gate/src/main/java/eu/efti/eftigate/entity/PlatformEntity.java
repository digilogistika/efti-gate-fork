package eu.efti.eftigate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "platform", catalog = "efti")
public class PlatformEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private int id;

    @Column(name = "name", length = 255, nullable = false, unique = true)
    private String name;

    // Although the url length is not limited by any standards, we limit it to 2048 characters here
    @Column(name = "uilrequesturl", length = 2048, nullable = false)
    private String uilRequestUrl;

    @Column(name = "followuprequesturl", length = 2048, nullable = false)
    private String followupRequestUrl;

    @Column(name = "secret", length = 1024, nullable = false)
    private String secret;
}
