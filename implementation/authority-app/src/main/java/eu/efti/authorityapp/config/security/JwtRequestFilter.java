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

        Optional<String> tokenOpt = getToken(request); // This gets the content of "Bearer <token>"
        log.info("tere" + tokenOpt);
        log.info("Request" + request + response);
        if (tokenOpt.isPresent()) {
            String token = tokenOpt.get();
            log.info("Comparing token from header: '{}' with hardcoded API key: '{}'", token, URL_API_KEY);
            if (URL_API_KEY.equals(token)) {
                log.info("Authenticating user via API key passed as a Bearer token.");
                SecurityContextHolder.getContext().setAuthentication(buildApiKeyAuthToken());
            } else {
                try {
                    Claims tokenBody = parseToken(token);
                    SecurityContextHolder.getContext().setAuthentication(buildAuthToken(tokenBody));
                } catch (Exception e) {
                    log.warn("Invalid JWT token: {}", e.getMessage());
                }
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