package eu.efti.commons.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FollowUpRequestDto implements ValidatableDto {
    @Size(max = 255, message = "NOTE_TOO_LONG")
    private String message;
    private String requestId;
}
