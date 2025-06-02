// 2) a One-liner filter bean
package eu.efti.eftigate.config.security;

import eu.efti.eftigate.entity.PlatformEntity;
import eu.efti.eftigate.exception.XApiKeyValidationException;
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

@Component
@Slf4j
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${gate.apikey}")
    private String validApiKey;

    @Setter
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlatformRepository platformRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        String xApiKeyHeader = req.getHeader("X-API-Key");
        String path = req.getRequestURI();

        // platform api endpoints validation
        try {
            if (path.startsWith("/api/platform/v0")) {
                validatePlatformXApiKeyHeader(xApiKeyHeader);
            }
        } catch (XApiKeyValidationException e) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for platform");
        }

        // authority access point validation
        if (path.startsWith("/v1/control")) {
            if (validApiKey.equals(xApiKeyHeader)) {
                chain.doFilter(req, res);
            } else {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key for authority");
            }
        }

        // skip public endpoints
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/ws")
                || path.startsWith("/api/platform/v0")) {
            chain.doFilter(req, res);
        }
    }

    private void validatePlatformXApiKeyHeader(String header) throws XApiKeyValidationException {
        String[] parts = header.split("_", 2);
        if (parts.length != 2) {
            log.warn("Platform validation failed: invalid header format");
            throw new XApiKeyValidationException("Invalid header format");
        }

        String platformId = parts[0];
        String secret = parts[1];

        if (platformId.isEmpty() || secret.isEmpty()) {
            log.warn("Platform validation failed: platformId or secret is empty");
            throw new XApiKeyValidationException("platformId or secret is empty");
        }

        validatePlatform(platformId, secret);
    }

    private void validatePlatform(String platformId, String secret) {
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
