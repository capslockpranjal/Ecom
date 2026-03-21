package org.example.zenvybackend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateCustomerProfileRequest;
import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class UserController {

    private final UserService userService;
    private final MessageResolver messageResolver;

    @GetMapping("/profile")
    public ApiResponse<CustomerProfileResponse> getProfile(){

        return ApiResponse.success(
                messageResolver.get("response.profile.fetched", "Profile fetched successfully"),
                userService.getCustomerProfile()
        );
    }

    @PatchMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<CustomerProfileResponse> updateProfile(
            @Valid @RequestBody UpdateCustomerProfileRequest request){

        return ApiResponse.success(
                messageResolver.get("response.profile.updated", "Profile updated successfully"),
                userService.updateCustomerProfile(request)
        );
    }

    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CustomerProfileResponse> updateProfileMultipart(
            @Valid @RequestPart("data") UpdateCustomerProfileRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {

        return ApiResponse.success(
                messageResolver.get("response.profile.updated", "Profile updated successfully"),
                userService.updateCustomerProfile(request, profileImage)
        );
    }

    @PatchMapping("/password")
    public ApiResponse<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request){

        userService.changePassword(request);

        return ApiResponse.success(messageResolver.get("response.password.updated", "Password updated successfully"));
    }
}
