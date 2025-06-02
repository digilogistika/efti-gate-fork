package eu.efti.platformgatesimulator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlatformRegistrationRequestDto {
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Platform ID must contain only alphanumeric characters and hyphens")
    private String platformId;

    @NotNull
    @URL
    private String requestBaseUrl;
}
