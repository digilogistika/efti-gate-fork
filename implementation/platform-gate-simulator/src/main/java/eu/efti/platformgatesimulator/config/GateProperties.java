package eu.efti.platformgatesimulator.config;

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
    private String owner;
    private String cdaPath;
    private String gate;
    private String description;
    private String gateSuperApiKey;
    private String gateBaseUrl;
    private String platformBaseUrl;
    private int minSleep;
    private int maxSleep;

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }
}
