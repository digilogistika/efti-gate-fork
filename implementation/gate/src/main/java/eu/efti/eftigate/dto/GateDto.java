package eu.efti.eftigate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.efti.commons.enums.CountryIndicator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GateDto {
    @NotNull
    private CountryIndicator country;
    @NotNull
    private String gateId;
}
