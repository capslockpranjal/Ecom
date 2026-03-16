package org.example.zenvybackend.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.dto.request.AddressRequest;
import org.example.zenvybackend.user.dto.response.AddressResponse;
import org.example.zenvybackend.user.dto.request.UpdateAddressRequest;
import org.example.zenvybackend.user.entity.Address;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.mapper.AddressMapper;
import org.example.zenvybackend.user.repository.AddressRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.example.zenvybackend.user.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public void addAddress(AddressRequest request) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = AddressMapper.toEntity(request);
        address.setUser(user);

        addressRepository.save(address);
    }

    @Override
    public List<AddressResponse> getAddresses() {

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        return addressRepository.findByUserId(currentUserId)
                .stream()
                .map(AddressMapper::toResponse)
                .toList();
    }

    @Override
    public AddressResponse updateAddress(UUID id, UpdateAddressRequest request) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        Address address = addressRepository
                .findByIdAndUserId(id, currentUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address not found"));

        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        if (request.getAddressLine() != null) {
            address.setAddressLine(request.getAddressLine());
        }
        if (request.getZipCode() != null) {
            address.setZipCode(request.getZipCode());
        }
        if (request.getLabel() != null) {
            address.setLabel(request.getLabel());
        }

        addressRepository.save(address);

        return AddressMapper.toResponse(address);
    }

    @Override
    public void deleteAddress(UUID id) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        Address address = addressRepository
                .findByIdAndUserId(id, currentUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address not found"));

        addressRepository.delete(address);
    }
}