package eu.efti.authorityapp.config;

import io.jsonwebtoken.Jwts;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;

@Configuration
@ComponentScan(basePackages = {"eu.efti"})
public class AuthorityAppConfig {

    @Bean
    @ConfigurationProperties(prefix = "gate")
    public GateProperties gateProperties() {
        return new GateProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "authority")
    public AuthorityAppProperties authorityAppProperties() {
        return new AuthorityAppProperties();
    }

    @Bean
    public SecretKey jwtKey() {
        return Jwts.SIG.HS256.key().build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
