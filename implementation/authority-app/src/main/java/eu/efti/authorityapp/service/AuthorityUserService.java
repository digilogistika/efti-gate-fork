package eu.efti.authorityapp.service;

import eu.efti.authorityapp.dto.AuthorityUserDto;
import eu.efti.authorityapp.dto.JwtDto;
import eu.efti.authorityapp.entity.AuthorityUserEntity;
import eu.efti.authorityapp.repository.AuthorityUserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Slf4j
public class AuthorityUserService {
    private final AuthorityUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private final SecretKey jwtKey;

    private String generateToken(AuthorityUserEntity user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claims(Map.of(
                        "userId", user.getId()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(jwtKey)
                .compact();
    }

    public ResponseEntity<Void> createAuthorityUser(AuthorityUserDto authorityUserDto) {
        if (repository.existsByEmail(authorityUserDto.getEmail())) {
            log.info("User with email {} already exists", authorityUserDto.getEmail());
            return ResponseEntity.badRequest().build();
        }
        log.info("Creating new user with email {}", authorityUserDto.getEmail());

        AuthorityUserEntity authorityUser = AuthorityUserEntity.builder()
                .email(authorityUserDto.getEmail())
                .build();

        authorityUser.setPassword(passwordEncoder.encode(authorityUserDto.getPassword()));

        repository.save(authorityUser);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<JwtDto> verifyAuthorityUser(AuthorityUserDto authorityUserDto) {
        log.info("Attempting login for user: {}", authorityUserDto.getEmail());

        AuthorityUserEntity authorityUser = repository.findByEmail(authorityUserDto.getEmail())
                .orElseThrow(() -> new InvalidParameterException("Invalid email or password"));

        if (!passwordEncoder.matches(authorityUserDto.getPassword(), authorityUser.getPassword())) {
            throw new InvalidParameterException("Invalid username or password");
        }

        log.info("Successful login for user: {}", authorityUserDto.getEmail());

        String token = generateToken(authorityUser);
        return ResponseEntity.ok(new JwtDto(token));
    }

    public ResponseEntity<Void> validateAuthorityUser(String jwt) {
        log.info("Validating JWT token");

        try {
            Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload(); // If no exception, validation passed
            return ResponseEntity.ok().build(); // Or return something indicating success
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
        } catch (Exception e) { // Catch any other unexpected exceptions
            log.error("An unexpected error occurred during JWT validation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
}
