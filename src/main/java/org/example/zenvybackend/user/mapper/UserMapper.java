package org.example.zenvybackend.user.mapper;

import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.dto.response.SellerProfileResponse;
import org.example.zenvybackend.user.entity.Address;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;

public class  UserMapper {

    public static CustomerProfileResponse toProfileResponse(Customer customer, String profileImage) {

        User user = customer.getUser();
        return CustomerProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contact(customer.getContact())
                .profileImage(profileImage)
                .build();
    }

    public static SellerProfileResponse toSellerProfileResponse(Seller seller, Address address, String profileImage) {

        User user = seller.getUser();

        return SellerProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .companyName(seller.getCompanyName())
                .companyContact(seller.getCompanyContact())
                .gst(seller.getGst())
                .profileImage(profileImage)
                .address(address != null ? AddressMapper.toResponse(address) : null)
                .build();
    }
}
