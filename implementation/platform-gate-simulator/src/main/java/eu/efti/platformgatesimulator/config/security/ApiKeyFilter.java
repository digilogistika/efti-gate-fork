// 2) a One-liner filter bean
package eu.efti.platformgatesimulator.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${gate.gateSuperApiKey}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        // skip public endpoints
        String path = req.getRequestURI();
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/gate-api")) {
            chain.doFilter(req, res);
            return;
        }

        String provided = req.getHeader("X-API-Key");
        if (validApiKey.equals(provided)) {
            chain.doFilter(req, res);
        } else {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
        }
    }
}
