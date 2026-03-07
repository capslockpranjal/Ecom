package org.example.zenvybackend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.dto.request.ForgotPasswordRequest;
import org.example.zenvybackend.user.dto.request.LoginRequest;
import org.example.zenvybackend.user.dto.request.RegisterCustomerRequest;
import org.example.zenvybackend.user.dto.request.ResetPasswordRequest;
import org.example.zenvybackend.user.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /* ------------------------------------------------ */
    /* REGISTER CUSTOMER */
    /* ------------------------------------------------ */

    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<String>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request){

        authService.registerCustomer(request);

        return ResponseEntity.ok(
                ApiResponse.success("Customer registered successfully. Activation email sent.")
        );
    }

    /* ------------------------------------------------ */
    /* ACTIVATE ACCOUNT */
    /* ------------------------------------------------ */

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activateAccount(
            @RequestParam String token){

        authService.activateAccount(token);

        return ResponseEntity.ok(
                ApiResponse.success("Account activated successfully")
        );
    }

    /* ------------------------------------------------ */
    /* LOGIN */
    /* ------------------------------------------------ */

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody LoginRequest request){

        String token = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(token)
        );
    }

    /* ------------------------------------------------ */
    /* FORGOT PASSWORD */
    /* ------------------------------------------------ */

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());

        return ApiResponse.success("Reset email sent");
    }
    /* ------------------------------------------------ */
    /* RESET PASSWORD */
    /* ------------------------------------------------ */

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request.getToken(), request.getPassword());

        return ApiResponse.success("Password reset successful");
    }
    @GetMapping("/test")
    public String test(){
        return "JWT works";
    }

}