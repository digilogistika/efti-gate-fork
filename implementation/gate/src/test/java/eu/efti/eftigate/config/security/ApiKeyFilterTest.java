package eu.efti.eftigate.config.security;

import eu.efti.eftigate.entity.AuthorityUserEntity;
import eu.efti.eftigate.entity.PlatformEntity;
import eu.efti.eftigate.repository.AuthorityUserRepository;
import eu.efti.eftigate.repository.PlatformRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyFilterTest {

    private static final String SUPER_API_KEY = "super-secret-key";
    private static final String PLATFORM_ID = "platform123";
    private static final String PLATFORM_SECRET = "platform-secret";
    private static final String AUTHORITY_ID = "authority123";
    private static final String AUTHORITY_SECRET = "authority-secret";
    private static final String VALID_PLATFORM_API_KEY = PLATFORM_ID + "_" + PLATFORM_SECRET;
    private static final String VALID_AUTHORITY_API_KEY = AUTHORITY_ID + "_" + AUTHORITY_SECRET;
    @Mock
    private PlatformRepository platformRepository;
    @Mock
    private AuthorityUserRepository authorityUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private ApiKeyFilter apiKeyFilter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apiKeyFilter, "superApiKey", SUPER_API_KEY);
        apiKeyFilter.setPasswordEncoder(passwordEncoder);
    }

    // Platform endpoint tests
    @Test
    void shouldAllowValidPlatformApiKeyForIdentifiersPostEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(VALID_PLATFORM_API_KEY);
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");

        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setSecret("hashed-platform-secret");
        when(platformRepository.findByPlatformId(PLATFORM_ID)).thenReturn(platformEntity);
        when(passwordEncoder.matches(PLATFORM_SECRET, "hashed-platform-secret")).thenReturn(true);

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldNotValidatePlatformApiKeyForIdentifiersPostEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn("any-key");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        // Should not call platform validation since POST is for authority endpoints
        verify(platformRepository, never()).findByPlatformId(anyString());
    }

    @Test
    void shouldRejectInvalidPlatformApiKeyForIdentifiersPostEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(VALID_PLATFORM_API_KEY);
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");

        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setSecret("hashed-platform-secret");
        when(platformRepository.findByPlatformId(PLATFORM_ID)).thenReturn(platformEntity);
        when(passwordEncoder.matches(PLATFORM_SECRET, "hashed-platform-secret")).thenReturn(false);

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for platform: Invalid secret for platform");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectNonExistentPlatformForIdentifiersPostEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(VALID_PLATFORM_API_KEY);
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");
        when(platformRepository.findByPlatformId(PLATFORM_ID)).thenReturn(null);

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for platform: Platform with this platformId does not exist");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldAllowSuperApiKeyForDatasetEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(SUPER_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/dataset");
        when(request.getMethod()).thenReturn("GET");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldAllowSuperApiKeyForIdentifiersGetEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(SUPER_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("GET");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldAllowSuperApiKeyForFollowUpEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(SUPER_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/follow-up");
        when(request.getMethod()).thenReturn("POST");
        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldAllowValidAuthorityApiKeyForDatasetEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(VALID_AUTHORITY_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/dataset");
        when(request.getMethod()).thenReturn("GET");

        AuthorityUserEntity authorityUserEntity = new AuthorityUserEntity();
        authorityUserEntity.setSecret("hashed-authority-secret");
        when(authorityUserRepository.findByAuthorityId(AUTHORITY_ID)).thenReturn(Optional.of(authorityUserEntity));
        when(passwordEncoder.matches(AUTHORITY_SECRET, "hashed-authority-secret")).thenReturn(true);

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldAllowValidAuthorityApiKeyForIdentifiersGetEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(VALID_AUTHORITY_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("GET");

        AuthorityUserEntity authorityUserEntity = new AuthorityUserEntity();
        authorityUserEntity.setSecret("hashed-authority-secret");
        when(authorityUserRepository.findByAuthorityId(AUTHORITY_ID)).thenReturn(Optional.of(authorityUserEntity));
        when(passwordEncoder.matches(AUTHORITY_SECRET, "hashed-authority-secret")).thenReturn(true);

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldAllowValidAuthorityApiKeyForFollowUpEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(VALID_AUTHORITY_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/follow-up");
        when(request.getMethod()).thenReturn("POST");

        AuthorityUserEntity authorityUserEntity = new AuthorityUserEntity();
        authorityUserEntity.setSecret("hashed-authority-secret");
        when(authorityUserRepository.findByAuthorityId(AUTHORITY_ID)).thenReturn(Optional.of(authorityUserEntity));
        when(passwordEncoder.matches(AUTHORITY_SECRET, "hashed-authority-secret")).thenReturn(true);

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldRejectInvalidAuthorityApiKeyForDatasetEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(VALID_AUTHORITY_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/dataset");
        when(request.getMethod()).thenReturn("GET");

        AuthorityUserEntity authorityUserEntity = new AuthorityUserEntity();
        authorityUserEntity.setSecret("hashed-authority-secret");
        when(authorityUserRepository.findByAuthorityId(AUTHORITY_ID)).thenReturn(Optional.of(authorityUserEntity));
        when(passwordEncoder.matches(AUTHORITY_SECRET, "hashed-authority-secret")).thenReturn(false);

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for authority: Invalid secret for authority");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectNonExistentAuthorityForDatasetEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(VALID_AUTHORITY_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/dataset");
        when(request.getMethod()).thenReturn("GET");
        when(authorityUserRepository.findByAuthorityId(AUTHORITY_ID)).thenReturn(Optional.empty());

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for authority: Authority with this authorityId does not exist");
        verify(filterChain, never()).doFilter(request, response);
    }

    // Admin endpoint tests
    @Test
    void shouldAllowSuperApiKeyForAdminEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(SUPER_API_KEY);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/api/admin");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldRejectInvalidApiKeyForAdminEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn("invalid-key");
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/api/admin");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for registration endpoints");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectMissingApiKeyForAdminEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/api/admin");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for registration endpoints");
        verify(filterChain, never()).doFilter(request, response);
    }

    // Public endpoint tests
    @Test
    void shouldAllowAccessToSwaggerEndpoint() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldAllowAccessToApiDocsEndpoint() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/v3/api-docs");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldAllowAccessToActuatorEndpoint() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldAllowAccessToWebSocketEndpoint() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/ws");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    // Edge case tests
    @Test
    void shouldRejectMissingApiKeyForPlatformEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for platform: X-API-Key header is missing");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectInvalidHeaderFormatForPlatformEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn("invalid-format");
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for platform: Invalid header format");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectMissingApiKeyForAuthorityEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn(null);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/dataset");
        when(request.getMethod()).thenReturn("GET");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for authority: X-API-Key header is missing");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectEmptyUserInApiKey() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn("_secret");
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for platform: user or secret is empty");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldRejectEmptySecretInApiKey() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn("user_");
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for platform: user or secret is empty");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldHandleApiKeyWithMultipleUnderscores() throws ServletException, IOException {
        // Given
        String apiKeyWithMultipleUnderscores = "platform123_secret_with_underscores";
        when(request.getHeader("X-API-Key")).thenReturn(apiKeyWithMultipleUnderscores);
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("POST");

        PlatformEntity platformEntity = new PlatformEntity();
        platformEntity.setSecret("hashed-platform-secret");
        when(platformRepository.findByPlatformId("platform123")).thenReturn(platformEntity);
        when(passwordEncoder.matches("secret_with_underscores", "hashed-platform-secret")).thenReturn(true);

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void shouldNotValidateIdentifiersDeleteEndpoint() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-API-Key")).thenReturn("any-key");
        when(request.getHeader("x-real-ip")).thenReturn("1.2.3.4");
        when(request.getRequestURI()).thenReturn("/v1/identifiers");
        when(request.getMethod()).thenReturn("DELETE");

        // When
        apiKeyFilter.doFilterInternal(request, response, filterChain);

        // Then
        // DELETE method is not handled by either platform or authority validation
        verify(platformRepository, never()).findByPlatformId(anyString());
        verify(authorityUserRepository, never()).findByAuthorityId(anyString());
    }
}