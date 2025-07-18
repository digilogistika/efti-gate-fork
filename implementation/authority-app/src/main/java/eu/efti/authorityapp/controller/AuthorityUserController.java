package eu.efti.authorityapp.controller;

import eu.efti.authorityapp.controller.api.AuthorityUserControllerApi;
import eu.efti.authorityapp.dto.AuthorityUserDto;
import eu.efti.authorityapp.dto.JwtDto;
import eu.efti.authorityapp.service.AuthorityUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AuthorityUserController implements AuthorityUserControllerApi {

    private final AuthorityUserService authorityUserService;

    @PostMapping("/admin/authority-user/create")
    public ResponseEntity<Void> createAuthorityUser(final @RequestBody AuthorityUserDto authorityUserDto) {
        return authorityUserService.createAuthorityUser(authorityUserDto);
    }

    @PostMapping("/public/authority-user/verify")
    public ResponseEntity<JwtDto> verifyAuthorityUser(final @RequestBody AuthorityUserDto authorityUserDto) {
        return authorityUserService.verifyAuthorityUser(authorityUserDto);
    }

    @PostMapping("/public/authority-user/validate")
    public ResponseEntity<Void> validateAuthorityUser(final @RequestBody String jwt) {
        return authorityUserService.validateAuthorityUser(jwt);
    }
}
