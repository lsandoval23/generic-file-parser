package org.creati.sicloReservationsApi.auth.service;

import org.creati.sicloReservationsApi.auth.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(User user);

    boolean isTokenValid(String token, UserDetails user);

    <T> T extractClaim(String token, String claimName, Class<T> type);

}
