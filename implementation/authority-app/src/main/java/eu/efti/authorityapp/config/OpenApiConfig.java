package eu.efti.authorityapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        String version = getClass().getPackage().getImplementationVersion();
        version = version == null ? "version not available in local env" : version;

        return new OpenAPI()
                .info(new Info()
                        .version(version));
    }
}
