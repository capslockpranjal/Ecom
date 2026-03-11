package org.example.zenvybackend.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.dto.request.AddressRequest;
import org.example.zenvybackend.user.dto.response.AddressResponse;
import org.example.zenvybackend.user.entity.Address;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.mapper.AddressMapper;
import org.example.zenvybackend.user.repository.AddressRepository;
import org.example.zenvybackend.user.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    public void addAddress(AddressRequest request) {

        User user = SecurityUtil.getCurrentUser();

        Address address = AddressMapper.toEntity(request);
        address.setUser(user);

        addressRepository.save(address);
    }

    @Override
    public List<AddressResponse> getAddresses() {

        User user = SecurityUtil.getCurrentUser();

        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(AddressMapper::toResponse)
                .toList();
    }

    @Override
    public AddressResponse updateAddress(UUID id, AddressRequest request) {

        User user = SecurityUtil.getCurrentUser();

        Address address = addressRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address not found"));

        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setAddressLine(request.getAddressLine());
        address.setZipCode(request.getZipCode());
        address.setLabel(request.getLabel());

        addressRepository.save(address);

        return AddressMapper.toResponse(address);
    }

    @Override
    public void deleteAddress(UUID id) {

        User user = SecurityUtil.getCurrentUser();

        Address address = addressRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
    }
}