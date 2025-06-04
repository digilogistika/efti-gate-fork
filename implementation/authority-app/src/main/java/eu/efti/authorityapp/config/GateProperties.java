package eu.efti.authorityapp.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GateProperties {
    private String name;
    private String baseUrl;
    private String superApiKey;
}
