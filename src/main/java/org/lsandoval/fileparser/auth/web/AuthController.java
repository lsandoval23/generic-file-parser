package org.lsandoval.fileparser.auth.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lsandoval.fileparser.auth.dto.LoginRequest;
import org.lsandoval.fileparser.auth.dto.LoginResponse;
import org.lsandoval.fileparser.auth.service.AuthService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@ConditionalOnProperty(name = "auth.jwt.enabled", havingValue = "true", matchIfMissing = true)
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

}
