package org.lsandoval.fileparser.auth.service;

import org.lsandoval.fileparser.auth.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(User user);

    boolean isTokenValid(String token, UserDetails user);

    <T> T extractClaim(String token, String claimName, Class<T> type);

}
