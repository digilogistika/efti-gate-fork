package eu.efti.authorityapp.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final SecretKey key;
    private static final String URL_API_KEY = "cak0130dLkXMC9"; // Hardcoded API key
    private static final String API_KEY_HEADER_NAME = "X-API-Key";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();

        if (requestUri.startsWith("/api/admin")) {
            log.info("Skipping JWT authentication for admin API");
            chain.doFilter(request, response);
            return;
        }

        final String apiKeyFromHeader = request.getHeader(API_KEY_HEADER_NAME);
        if (apiKeyFromHeader != null && apiKeyFromHeader.equals(URL_API_KEY)) {
            log.info("Authenticating user via URL API key");
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(buildApiKeyAuthToken());
            chain.doFilter(request, response);
            return;
        }

        Optional<String> token = getToken(request);
        if (token.isPresent()) {
            try {
                Claims tokenBody = parseToken(token.get());
                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(buildAuthToken(tokenBody));
            } catch (Exception e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    private Optional<String> getToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Authentication buildAuthToken(Claims tokenBody) {
        return new UsernamePasswordAuthenticationToken(
                tokenBody.getSubject(), null, List.of(new SimpleGrantedAuthority("USER"))
        );
    }

    private Authentication buildApiKeyAuthToken() {
        return new UsernamePasswordAuthenticationToken(
                "api-key-user", null, List.of(new SimpleGrantedAuthority("USER"))
        );
    }
}