package org.example.zenvybackend.user.mapper;

import lombok.NonNull;
import org.example.zenvybackend.user.dto.request.AddressRequest;
import org.example.zenvybackend.user.dto.response.AddressResponse;
import org.example.zenvybackend.user.entity.Address;

public class AddressMapper {

    public static  Address toEntity(@NonNull AddressRequest request) {

        Address address = new Address();
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setAddressLine(request.getAddressLine());
        address.setZipCode(request.getZipCode());
        address.setLabel(request.getLabel());

        return address;
    }

    public static AddressResponse toResponse(@NonNull Address address){

        return AddressResponse.builder()
                .id(address.getId())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .addressLine(address.getAddressLine())
                .zipCode(address.getZipCode())
                .label(address.getLabel())
                .build();
    }
}
