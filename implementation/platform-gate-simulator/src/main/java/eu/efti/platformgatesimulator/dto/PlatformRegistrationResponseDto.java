package eu.efti.platformgatesimulator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlatformRegistrationResponseDto {
    private final String Description = "Use this secret in request header 'X-API-Key <name>_<secret>'";
    private String name;
    private String secret;
}
