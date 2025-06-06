package eu.efti.authorityapp.service;

import eu.efti.authorityapp.dto.AuthorityUserDto;
import eu.efti.authorityapp.dto.JwtDto;
import eu.efti.authorityapp.entity.AuthorityUserEntity;
import eu.efti.authorityapp.repository.AuthorityUserRepository;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
}
