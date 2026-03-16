package org.example.zenvybackend.user.service;

import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateCustomerProfileRequest;
import org.example.zenvybackend.user.dto.request.UpdateSellerProfileRequest;
import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.dto.response.SellerProfileResponse;

public interface UserService {

    CustomerProfileResponse getCustomerProfile();

    CustomerProfileResponse updateCustomerProfile(UpdateCustomerProfileRequest request);

    SellerProfileResponse getSellerProfile();

    SellerProfileResponse updateSellerProfile(UpdateSellerProfileRequest request);

    void changePassword(ChangePasswordRequest request);
}
