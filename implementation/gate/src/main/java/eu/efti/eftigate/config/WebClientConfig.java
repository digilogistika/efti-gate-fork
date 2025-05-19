package eu.efti.eftigate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient
                .builder()
                .exchangeStrategies(
                        ExchangeStrategies
                                .builder()
                                .codecs(configurer ->
                                        configurer
                                                .defaultCodecs()
                                                .maxInMemorySize(8 * 1024 * 1024) // 8MB
                                ).build()
                );
    }
}
