package org.example.zenvybackend.user.service;

import org.example.zenvybackend.user.dto.request.LoginRequest;
import org.example.zenvybackend.user.dto.request.RegisterCustomerRequest;
import org.example.zenvybackend.user.dto.request.RegisterSellerRequest;
import org.example.zenvybackend.user.dto.response.AuthResponse;

public interface AuthService {

    void registerCustomer(RegisterCustomerRequest request);

    void registerSeller(RegisterSellerRequest request);

    void activateAccount(String token);

    AuthResponse login(LoginRequest request);

    void forgotPassword(String email);

    void resetPassword(String token, String password, String confirmPassword);

    AuthResponse refreshToken(String refreshTokenValue);

    void resendActivation(String email);

    void logout(String token);
}
