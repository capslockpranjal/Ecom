package org.example.zenvybackend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.dto.request.ForgotPasswordRequest;
import org.example.zenvybackend.user.dto.request.LoginRequest;
import org.example.zenvybackend.user.dto.request.RegisterCustomerRequest;
import org.example.zenvybackend.user.dto.request.ResetPasswordRequest;
import org.example.zenvybackend.user.dto.response.AuthResponse;
import org.example.zenvybackend.user.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;



    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<String>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request){

        authService.registerCustomer(request);

        return ResponseEntity.ok(
                ApiResponse.success("Customer registered successfully. Activation email sent.")
        );
    }



    @PostMapping("/register/seller")
    public ResponseEntity<ApiResponse<String>> registerSeller(
            @Valid @RequestBody RegisterCustomerRequest request){

        authService.registerSeller(request);

        return ResponseEntity.ok(
                ApiResponse.success("Seller registration submitted. Await admin approval.")
        );
    }



    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activateAccount(
            @RequestParam String token){

        authService.activateAccount(token);

        return ResponseEntity.ok(
                ApiResponse.success("Account activated successfully")
        );
    }



    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request){

        AuthResponse token = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", token)
        );
    }



    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());

        return ApiResponse.success("Reset email sent");
    }


    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request.getToken(), request.getPassword(),request.getConfirmPassword());

        return ApiResponse.success("Password reset successful");
    }
    @GetMapping("/test")
    public String test(){
        return "JWT works";
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@RequestParam String refreshToken){

        AuthResponse response = authService.refreshToken(refreshToken);

        return ApiResponse.success("New tokens generated", response);
    }

    @PostMapping("/resend-activation")
    public ApiResponse<String> resendActivation(@RequestParam String email){

        authService.resendActivation(email);

        return ApiResponse.success("Activation email resent");
    }
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String header){

        String token = header.substring(7);

        authService.logout(token);

        return ApiResponse.success("Logged out successfully");
    }

}