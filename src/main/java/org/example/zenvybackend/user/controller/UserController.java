package org.example.zenvybackend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateCustomerProfileRequest;
import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<CustomerProfileResponse> getProfile(){

        return ApiResponse.success(
                "Profile fetched successfully",
                userService.getCustomerProfile()
        );
    }

    @PatchMapping("/profile")
    public ApiResponse<CustomerProfileResponse> updateProfile(
            @Valid @RequestBody UpdateCustomerProfileRequest request){

        return ApiResponse.success(
                "Profile updated successfully",
                userService.updateCustomerProfile(request)
        );
    }

    @PatchMapping("/password")
    public ApiResponse<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request){

        userService.changePassword(request);

        return ApiResponse.success("Password updated successfully");
    }
}
