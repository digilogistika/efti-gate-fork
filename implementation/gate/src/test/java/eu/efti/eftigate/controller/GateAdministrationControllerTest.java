package eu.efti.eftigate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.efti.commons.enums.CountryIndicator;
import eu.efti.eftigate.config.TestSecurityConfig;
import eu.efti.eftigate.config.security.PermissionLevel;
import eu.efti.eftigate.dto.*;
import eu.efti.eftigate.exception.AuthorityUserAlreadyExistsException;
import eu.efti.eftigate.exception.DefaultExceptionHandler;
import eu.efti.eftigate.exception.GateAlreadyExistsException;
import eu.efti.eftigate.exception.GateDoesNotExistException;
import eu.efti.eftigate.exception.PlatformAlreadyExistsException;
import eu.efti.eftigate.service.AuthorityIdentityService;
import eu.efti.eftigate.service.PlatformIdentityService;
import eu.efti.eftigate.service.gate.GateAdministrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GateAdministrationController.class)
@ContextConfiguration(classes = {GateAdministrationController.class, DefaultExceptionHandler.class})
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
class GateAdministrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    GateAdministrationService gateAdministrationService;

    @MockBean
    AuthorityIdentityService authorityIdentityService;

    @MockBean
    PlatformIdentityService platformIdentityService;

    @Test
    void givenValidGateDto_whenRegisterGate_thenReturnsOk() throws Exception {
        // Given
        GateDto gateDto = GateDto.builder()
                .gateId("test-gate")
                .country(CountryIndicator.FR)
                .build();

        when(gateAdministrationService.registerGate(any()))
                .thenReturn("Gate test-gate added successfully");

        // Then
        mockMvc.perform(post("/api/admin/gate/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(gateDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Gate test-gate added successfully"));
    }

    @Test
    void givenExistingGateId_whenRegisterGate_thenReturnsConflict() throws Exception {
        // Given
        GateDto gateDto = GateDto.builder()
                .gateId("existing-gate")
                .country(CountryIndicator.FR)
                .build();

        when(gateAdministrationService.registerGate(any()))
                .thenThrow(new GateAlreadyExistsException("Gate with this ID already exists"));

        // Then
        mockMvc.perform(post("/api/admin/gate/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(gateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Gate with this ID already exists"));
    }

    @Test
    void givenInvalidGateDto_whenRegisterGate_thenReturnsBadRequest() throws Exception {
        // Given
        GateDto gateDto = GateDto.builder()
                .gateId(null)  // Invalid: gateId is required
                .country(CountryIndicator.FR)
                .build();

        // Then
        mockMvc.perform(post("/api/admin/gate/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(gateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNullCountry_whenRegisterGate_thenReturnsBadRequest() throws Exception {
        // Given
        GateDto gateDto = GateDto.builder()
                .gateId("test-gate")
                .country(null)  // Invalid: country is required
                .build();

        // Then
        mockMvc.perform(post("/api/admin/gate/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(gateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenExistingGateId_whenDeleteGate_thenReturnsOk() throws Exception {
        // Given
        String gateId = "test-gate";
        when(gateAdministrationService.deleteGate(gateId))
                .thenReturn("Gate test-gate deleted successfully");

        // Then
        mockMvc.perform(delete("/api/admin/gate/delete/{gateId}", gateId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Gate test-gate deleted successfully"));
    }

    @Test
    void givenNonExistingGateId_whenDeleteGate_thenReturnsNotFound() throws Exception {
        // Given
        String gateId = "non-existing-gate";
        when(gateAdministrationService.deleteGate(gateId))
                .thenThrow(new GateDoesNotExistException("Gate with this ID does not exist"));

        // Then
        mockMvc.perform(delete("/api/admin/gate/delete/{gateId}", gateId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Gate with this ID does not exist"));
    }

    @Test
    void givenValidAuthorityRegistration_whenRegisterAuthority_thenReturnsOk() throws Exception {
        // Given
        AuthorityUserRegistrationRequestDto requestDto = AuthorityUserRegistrationRequestDto.builder()
                .authorityId("test-authority")
                .permissionLevel(PermissionLevel.AUTHORITY_ACCESS_POINT)
                .build();

        AuthorityUserRegistrationResponseDto responseDto = AuthorityUserRegistrationResponseDto.builder()
                .apiKey("test-api-key")
                .build();

        when(authorityIdentityService.registerAuthorityUser(any()))
                .thenReturn(responseDto);

        // Then
        mockMvc.perform(post("/api/admin/authority/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").value("test-api-key"));
    }

    @Test
    void givenInvalidAuthorityId_whenRegisterAuthority_thenReturnsBadRequest() throws Exception {
        // Given
        AuthorityUserRegistrationRequestDto requestDto = AuthorityUserRegistrationRequestDto.builder()
                .authorityId("invalid@authority")  // Invalid: contains special characters
                .permissionLevel(PermissionLevel.AUTHORITY_ACCESS_POINT)
                .build();

        // Then
        mockMvc.perform(post("/api/admin/authority/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenExistingAuthorityId_whenRegisterAuthority_thenReturnsConflict() throws Exception {
        // Given
        AuthorityUserRegistrationRequestDto requestDto = AuthorityUserRegistrationRequestDto.builder()
                .authorityId("existing-authority")
                .permissionLevel(PermissionLevel.AUTHORITY_ACCESS_POINT)
                .build();

        when(authorityIdentityService.registerAuthorityUser(any()))
                .thenThrow(new AuthorityUserAlreadyExistsException("Authority user with this ID already exists"));

        // Then
        mockMvc.perform(post("/api/admin/authority/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void givenNullPermissionLevel_whenRegisterAuthority_thenReturnsBadRequest() throws Exception {
        // Given
        AuthorityUserRegistrationRequestDto requestDto = AuthorityUserRegistrationRequestDto.builder()
                .authorityId("test-authority")
                .permissionLevel(null)  // Invalid: permission level is required
                .build();

        // Then
        mockMvc.perform(post("/api/admin/authority/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidPlatformRegistration_whenRegisterPlatform_thenReturnsOk() throws Exception {
        // Given
        PlatformRegistrationRequestDto requestDto = PlatformRegistrationRequestDto.builder()
                .platformId("test-platform")
                .requestBaseUrl("https://test-platform.com/api")
                .headers(List.of(PlatformHeaderDto.builder()
                        .key("X-Custom-Header")
                        .value("custom-value")
                        .build()))
                .build();

        PlatformRegistrationResponseDto responseDto = PlatformRegistrationResponseDto.builder()
                .apiKey("test-api-key")
                .build();

        when(platformIdentityService.registerPlatform(any()))
                .thenReturn(responseDto);

        // Then
        mockMvc.perform(post("/api/admin/platform/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").value("test-api-key"));
    }

    @Test
    void givenInvalidPlatformRegistration_whenRegisterPlatform_thenReturnsBadRequest() throws Exception {
        // Given
        PlatformRegistrationRequestDto requestDto = PlatformRegistrationRequestDto.builder()
                .platformId(null)  // Invalid: platformId is required
                .requestBaseUrl("https://test-platform.com/api")
                .build();

        // Then
        mockMvc.perform(post("/api/admin/platform/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidPlatformId_whenRegisterPlatform_thenReturnsBadRequest() throws Exception {
        // Given
        PlatformRegistrationRequestDto requestDto = PlatformRegistrationRequestDto.builder()
                .platformId("invalid@platform")  // Invalid: contains special characters
                .requestBaseUrl("https://test-platform.com/api")
                .build();

        // Then
        mockMvc.perform(post("/api/admin/platform/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidUrl_whenRegisterPlatform_thenReturnsBadRequest() throws Exception {
        // Given
        PlatformRegistrationRequestDto requestDto = PlatformRegistrationRequestDto.builder()
                .platformId("test-platform")
                .requestBaseUrl("invalid-url")  // Invalid: not a valid URL
                .build();

        // Then
        mockMvc.perform(post("/api/admin/platform/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenExistingPlatformId_whenRegisterPlatform_thenReturnsConflict() throws Exception {
        // Given
        PlatformRegistrationRequestDto requestDto = PlatformRegistrationRequestDto.builder()
                .platformId("existing-platform")
                .requestBaseUrl("https://test-platform.com/api")
                .build();

        when(platformIdentityService.registerPlatform(any()))
                .thenThrow(new PlatformAlreadyExistsException("Platform with this platformId already exists"));

        // Then
        mockMvc.perform(post("/api/admin/platform/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDto)))
                .andExpect(status().isConflict());
    }

}
