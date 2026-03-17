package org.example.zenvybackend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.dto.request.UpdateAddressRequest;
import org.example.zenvybackend.user.dto.response.AddressResponse;
import org.example.zenvybackend.user.service.AddressService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/seller/address")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerAddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<AddressResponse> getAddress() {

        return ApiResponse.success(
                "Address fetched",
                addressService.getSellerAddress()
        );
    }

    @PatchMapping
    public ApiResponse<AddressResponse> updateAddress(
            @Valid @RequestBody UpdateAddressRequest request
    ) {

        return ApiResponse.success(
                "Address updated",
                addressService.updateSellerAddress(request)
        );
    }

    @DeleteMapping
    public ApiResponse<String> deleteAddress() {

        addressService.deleteSellerAddress();

        return ApiResponse.success("Address deleted");
    }
}

