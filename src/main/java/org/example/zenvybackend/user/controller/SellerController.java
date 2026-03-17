package org.example.zenvybackend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateAddressRequest;
import org.example.zenvybackend.user.dto.request.UpdateSellerProfileRequest;
import org.example.zenvybackend.user.dto.response.AddressResponse;
import org.example.zenvybackend.user.dto.response.SellerProfileResponse;
import org.example.zenvybackend.user.service.AddressService;
import org.example.zenvybackend.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/profile")
    public ApiResponse<SellerProfileResponse> getProfile() {

        return ApiResponse.success(
                "Profile fetched successfully",
                userService.getSellerProfile()
        );
    }

    @PatchMapping("/profile")
    public ApiResponse<SellerProfileResponse> updateProfile(
            @Valid @RequestBody UpdateSellerProfileRequest request
    ) {

        return ApiResponse.success(
                "Profile updated successfully",
                userService.updateSellerProfile(request)
        );
    }

    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        userService.changePassword(request);

        return ApiResponse.success("Password updated successfully");
    }

    @PatchMapping("/address")
    public ApiResponse<AddressResponse> updateAddress(
            @Valid @RequestBody UpdateAddressRequest request
    ) {

        return ApiResponse.success(
                "Address updated",
                addressService.updateSellerAddress(request)
        );
    }
}

