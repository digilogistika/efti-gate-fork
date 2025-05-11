package eu.efti.eftigate.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GateProperties {
    private String country;
    private String owner;
    private ApConfig ap;
    private String apikey;

    public boolean isCurrentGate(final String gateId) {
        return this.owner.equalsIgnoreCase(gateId);
    }

    @Data
    @Builder
    public static final class ApConfig {
        private String url;
        private String username;
        private String password;
    }
}
