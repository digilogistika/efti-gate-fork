package eu.efti.eftigate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
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
    private String name;

    @NotNull
    @URL
    private String uilRequestUrl;

    @NotNull
    @URL
    private String followUpRequestUrl;
}
