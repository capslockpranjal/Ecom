package org.example.zenvybackend.user.mapper;

import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.User;

public class UserMapper {

    public static CustomerProfileResponse toProfileResponse(Customer customer) {

        User user = customer.getUser();
        return CustomerProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contact(customer.getContact())
                .build();
    }
}
