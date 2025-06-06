package eu.efti.authorityapp.config.security;

import eu.efti.authorityapp.config.GateProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final GateProperties gateProperties;
    private static final String API_KEY_HEADER = "X-API-Key";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/admin")) {
            Optional<String> apiKey = getApiKey(request);

            if (apiKey.isEmpty() || !isValidApiKey(apiKey.get())) {
                log.info("API key: {}", apiKey.orElse("missing"));
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
                response.setContentType("application/json");
                return;
            }

            authenticateUser();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser() {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_API_USER")
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "api-user",
                        null,
                        authorities
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("User authenticated via API key");
    }

    private Optional<String> getApiKey(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(API_KEY_HEADER));
    }

    private boolean isValidApiKey(String apiKey) {
        return gateProperties.getSuperApiKey() != null &&
                gateProperties.getSuperApiKey().equals(apiKey);
    }
}
