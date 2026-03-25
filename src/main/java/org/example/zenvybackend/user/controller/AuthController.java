package org.example.zenvybackend.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.dto.request.*;
import org.example.zenvybackend.user.dto.response.AuthResponse;
import org.example.zenvybackend.user.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessageResolver messageResolver;



    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<String>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request){

        authService.registerCustomer(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        messageResolver.get(
                                "response.auth.customer.registered",
                                "Customer registered successfully. Activation email sent."
                        )
                )
        );
    }



    @PostMapping("/register/seller")
    public ResponseEntity<ApiResponse<String>> registerSeller(
            @Valid @RequestBody RegisterSellerRequest request){

        authService.registerSeller(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        messageResolver.get(
                                "response.auth.seller.registered",
                                "Seller registration submitted. Await admin approval."
                        )
                )
        );
    }



    @PutMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activateAccount(
            @RequestParam String token){

        authService.activateAccount(token);

        return ResponseEntity.ok(
                ApiResponse.success(messageResolver.get("response.auth.account.activated", "Account activated successfully"))
        );
    }



    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
           @Valid @RequestBody LoginRequest request){

        AuthResponse token = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(messageResolver.get("response.auth.login.success", "Login successful"), token)
        );
    }



    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());

        return ApiResponse.success(messageResolver.get("response.auth.reset_email.sent", "Reset email sent"));
    }


    @PutMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request.getToken(), request.getPassword(),request.getConfirmPassword());

        return ApiResponse.success(messageResolver.get("response.auth.password.reset", "Password reset successful"));
    }


    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@RequestParam String refreshToken){

        AuthResponse response = authService.refreshToken(refreshToken);

        return ApiResponse.success(messageResolver.get("response.auth.tokens.refreshed", "New tokens generated"), response);
    }

    @PostMapping("/resend-activation")
    public ApiResponse<String> resendActivation(
            @Valid @RequestBody ResendActivationRequest request
    ){

        authService.resendActivation(request.getEmail());

        return ApiResponse.success(messageResolver.get("response.auth.activation.resent", "Activation email resent"));
    }
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Token missing");
        }

        String token = authHeader.substring(7);

        authService.logout(token);

        return ApiResponse.success(messageResolver.get("response.auth.logout.success", "Logout successful"), null);
    }

}
