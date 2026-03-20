package org.example.zenvybackend.user.service;

import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateCustomerProfileRequest;
import org.example.zenvybackend.user.dto.request.UpdateSellerProfileRequest;
import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.dto.response.SellerProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    CustomerProfileResponse getCustomerProfile();

    CustomerProfileResponse updateCustomerProfile(UpdateCustomerProfileRequest request);

    CustomerProfileResponse updateCustomerProfile(UpdateCustomerProfileRequest request, MultipartFile profileImage);

    SellerProfileResponse getSellerProfile();

    SellerProfileResponse updateSellerProfile(UpdateSellerProfileRequest request);

    SellerProfileResponse updateSellerProfile(UpdateSellerProfileRequest request, MultipartFile profileImage);

    void changePassword(ChangePasswordRequest request);
}
