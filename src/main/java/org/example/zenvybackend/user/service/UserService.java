package org.example.zenvybackend.user.service;

import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateCustomerProfileRequest;
import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;

public interface UserService {

    CustomerProfileResponse getCustomerProfile();

    CustomerProfileResponse updateCustomerProfile(UpdateCustomerProfileRequest request);

    void changePassword(ChangePasswordRequest request);
}
