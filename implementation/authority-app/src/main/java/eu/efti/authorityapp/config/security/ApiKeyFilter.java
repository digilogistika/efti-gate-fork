package eu.efti.authorityapp.config.security;

import eu.efti.authorityapp.config.GateProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final GateProperties gateProperties;
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String ADMIN_AUTHORITY_USER_ENDPOINT = "/v1/admin/authority-user";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        if ("POST".equals(method) && ADMIN_AUTHORITY_USER_ENDPOINT.equals(requestUri)) {
            Optional<String> apiKey = getApiKey(request);
            
            if (apiKey.isEmpty() || !isValidApiKey(apiKey.get())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
                response.setContentType("application/json");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private Optional<String> getApiKey(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(API_KEY_HEADER));
    }

    private boolean isValidApiKey(String apiKey) {
        return gateProperties.getSuperApiKey() != null && 
               gateProperties.getSuperApiKey().equals(apiKey);
    }
}