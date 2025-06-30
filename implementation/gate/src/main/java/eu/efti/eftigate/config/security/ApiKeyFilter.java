package eu.efti.eftigate.config.security;

import eu.efti.eftigate.entity.AuthorityUserEntity;
import eu.efti.eftigate.entity.PlatformEntity;
import eu.efti.eftigate.exception.XApiKeyValidationException;
import eu.efti.eftigate.repository.AuthorityUserRepository;
import eu.efti.eftigate.repository.PlatformRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${gate.apikey}")
    private String superApiKey;

    @Setter
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private AuthorityUserRepository authorityUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        String xApiKeyHeader = req.getHeader("X-API-Key");
        String path = req.getRequestURI();

        // skip for static files and root path
        if (path.equals("/") ||
                path.startsWith("/index.html") ||
                path.startsWith("/favicon.ico") ||
                path.matches(".*\\.(js|css|png|jpg|jpeg|svg|woff2?)$")) {
            chain.doFilter(req, res);
            return;
        }

        // skip public endpoints
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/ws")) {
            chain.doFilter(req, res);
            return;
        }

        // platform api endpoints validation
        if ((path.startsWith("/v1/identifiers") && req.getMethod().equals("POST"))
                || path.startsWith("/api/platform/v0/whoami")
        ) {
            try {
                validatePlatformXApiKeyHeader(xApiKeyHeader);
                chain.doFilter(req, res);
            } catch (XApiKeyValidationException e) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for platform: " + e.getMessage());
            }
            return;
        }

        // authority access point validation
        if (path.startsWith("/v1/dataset") ||
                (path.startsWith("/v1/identifiers") && (req.getMethod().equals("POST") || req.getMethod().equals("GET"))) ||
                path.startsWith("/v1/follow-up")) {
            try {
                if (superApiKey.equals(xApiKeyHeader)) {
                    chain.doFilter(req, res);
                } else {
                    validateAuthorityXApiKeyHeader(xApiKeyHeader);
                    chain.doFilter(req, res);
                }
            } catch (XApiKeyValidationException e) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for authority: " + e.getMessage());
            }
            return;
        }


        // registration endpoint validation
        if (path.startsWith("/api/admin")) {
            if (superApiKey.equals(xApiKeyHeader)) {
                chain.doFilter(req, res);
            } else {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for registration endpoints");
            }
            return;
        }

        chain.doFilter(req, res);
    }

    private String[] getUserAndSecretFromHeader(String header) throws XApiKeyValidationException {
        if (header == null) throw new XApiKeyValidationException("X-API-Key header is missing");

        String[] parts = header.split("_", 2);
        if (parts.length != 2) {
            log.warn("User validation failed: invalid header format");
            throw new XApiKeyValidationException("Invalid header format");
        }

        String user = parts[0];
        String secret = parts[1];

        if (user.isEmpty() || secret.isEmpty()) {
            log.warn("User validation failed: user or secret is empty");
            throw new XApiKeyValidationException("user or secret is empty");
        }

        return new String[]{user, secret};
    }

    private void validateAuthorityXApiKeyHeader(String header) throws XApiKeyValidationException {
        String[] credentials = getUserAndSecretFromHeader(header);
        String authorityId = credentials[0];
        String secret = credentials[1];

        Optional<AuthorityUserEntity> authorityUserEntity = authorityUserRepository.findByAuthorityId(authorityId);

        if (authorityUserEntity.isEmpty()) {
            log.warn("Authority validation failed: authority with authorityId {} does not exist", authorityId);
            throw new XApiKeyValidationException("Authority with this authorityId does not exist");
        }

        boolean isValid = passwordEncoder.matches(secret, authorityUserEntity.get().getSecret());

        if (isValid) {
            log.info("Authority {} validated successfully", authorityId);
        } else {
            log.warn("Authority validation failed: invalid secret for authority {}", authorityId);
            throw new XApiKeyValidationException("Invalid secret for authority");
        }
    }


    private void validatePlatformXApiKeyHeader(String header) throws XApiKeyValidationException {
        String[] credentials = getUserAndSecretFromHeader(header);
        String platformId = credentials[0];
        String secret = credentials[1];

        PlatformEntity platformEntity = platformRepository.findByPlatformId(platformId);

        if (platformEntity == null) {
            log.warn("Platform validation failed: platform with platformId {} does not exist", platformId);
            throw new XApiKeyValidationException("Platform with this platformId does not exist");
        }

        boolean isValid = passwordEncoder.matches(secret, platformEntity.getSecret());

        if (isValid) {
            log.info("Platform {} validated successfully", platformId);
        } else {
            log.warn("Platform validation failed: invalid secret for platform {}", platformId);
            throw new XApiKeyValidationException("Invalid secret for platform");
        }
    }
}
