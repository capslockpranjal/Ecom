package org.example.zenvybackend.user.service;

import org.example.zenvybackend.user.dto.request.AddressRequest;
import org.example.zenvybackend.user.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    void addAddress(AddressRequest request);

    List<AddressResponse> getAddresses();

    AddressResponse updateAddress(UUID id, AddressRequest request);

    void deleteAddress(UUID id);

}