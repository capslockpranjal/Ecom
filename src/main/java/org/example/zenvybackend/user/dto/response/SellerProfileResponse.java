package org.example.zenvybackend.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SellerProfileResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String companyContact;
    private String gst;

    // Addresses from address table for this seller (user)
    private List<AddressResponse> addresses;
}

