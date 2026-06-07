package org.creati.sicloReservationsApi.auth.service;

import org.creati.sicloReservationsApi.auth.dto.LoginRequest;
import org.creati.sicloReservationsApi.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

}
