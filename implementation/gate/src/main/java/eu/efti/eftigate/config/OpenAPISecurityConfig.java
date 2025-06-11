package eu.efti.eftigate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class OpenAPISecurityConfig {

    private static final String API_KEY_SCHEME_NAME = "X-API-Key";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME_NAME, createAPIKeyScheme()))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME_NAME))
                .info(new Info()
                        .title("Efti Gate")
                        .description("Efti gate")
                        .version("1.0"));
    }

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        System.setProperty("springdoc.swagger-ui.url", "/v3/api-docs");
        System.setProperty("server.forward-headers-strategy", "framework");
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(API_KEY_SCHEME_NAME)
                .description("Provide your API key in the X-API-Key header. Please contact the eFTI Gate administrator to obtain your API key.");
    }
}
