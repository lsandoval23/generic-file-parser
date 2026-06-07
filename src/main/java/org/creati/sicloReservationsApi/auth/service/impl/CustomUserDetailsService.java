package org.creati.sicloReservationsApi.auth.service.impl;

import org.creati.sicloReservationsApi.auth.dao.UserRepository;
import org.creati.sicloReservationsApi.auth.exception.InvalidCredentialsException;
import org.creati.sicloReservationsApi.auth.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("User not found for username: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        });

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);

    }
}
