package eu.efti.authorityapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.efti.authorityapp.config.security.PermissionLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorityUserRegistrationRequestDto {
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Authority ID must contain only alphanumeric characters and hyphens")
    private String authorityId;

    @NotNull
    private PermissionLevel permissionLevel;
}
