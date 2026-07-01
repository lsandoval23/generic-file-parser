package org.lsandoval.fileparser.auth.service;

import org.lsandoval.fileparser.auth.dto.LoginRequest;
import org.lsandoval.fileparser.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

}
