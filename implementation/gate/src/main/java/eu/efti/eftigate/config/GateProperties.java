package eu.efti.eftigate.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GateProperties {
    private String country;
    private String owner;
    private ApConfig ap;
    private String description;

    public boolean isCurrentGate(final String gateId) {
        return this.owner.equalsIgnoreCase(gateId);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Data
    @Builder
    public static final class ApConfig {
        private String url;
        private String username;
        private String password;
    }
}
