package org.example.zenvybackend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.dto.request.AddressRequest;
import org.example.zenvybackend.user.dto.request.UpdateAddressRequest;
import org.example.zenvybackend.user.dto.response.AddressResponse;
import org.example.zenvybackend.user.service.AddressService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customer/address")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class AddressController {

    private final AddressService addressService;
    private final MessageResolver messageResolver;

    @PostMapping
    public ApiResponse<String> addAddress(@Valid @RequestBody AddressRequest request){

        addressService.addAddress(request);

        return ApiResponse.success(messageResolver.get("response.address.added", "Address added successfully"));
    }

    @GetMapping
    public ApiResponse<List<AddressResponse>> getAddresses(){

        return ApiResponse.success(
                messageResolver.get("response.address.list", "Addresses fetched"),
                addressService.getAddresses()
        );
    }

    @PatchMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAddressRequest request){

        return ApiResponse.success(
                messageResolver.get("response.address.updated", "Address updated"),
                addressService.updateAddress(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteAddress(@PathVariable UUID id){

        addressService.deleteAddress(id);

        return ApiResponse.success(messageResolver.get("response.address.deleted", "Address deleted"));
    }
}
