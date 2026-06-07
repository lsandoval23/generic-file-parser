package org.creati.sicloReservationsApi.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.dao.UserRepository;
import org.creati.sicloReservationsApi.auth.dto.LoginRequest;
import org.creati.sicloReservationsApi.auth.dto.LoginResponse;
import org.creati.sicloReservationsApi.auth.dto.UserDto;
import org.creati.sicloReservationsApi.auth.exception.InvalidCredentialsException;
import org.creati.sicloReservationsApi.auth.model.User;
import org.creati.sicloReservationsApi.auth.service.AuthService;
import org.creati.sicloReservationsApi.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@ConditionalOnProperty(name = "auth.jwt.enabled", havingValue = "true", matchIfMissing = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final Long jwtExpiration;


    public AuthServiceImpl(
            final UserRepository userRepository,
            final JwtService jwtService,
            final AuthenticationManager authenticationManager,
            @Value("${jwt.expiration}") Long jwtExpiration) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwtExpiration = jwtExpiration;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() ->  new InvalidCredentialsException("User not found after authentication"));
            String jwtToken = jwtService.generateToken(user);
            UserDto userInfo = new UserDto(user);

            return new LoginResponse(jwtToken, userInfo, jwtExpiration);

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
            throw new InvalidCredentialsException("Error in authentication", e);
        }
    }
}
