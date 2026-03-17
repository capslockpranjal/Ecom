package org.example.zenvybackend.user.dto.response;

import lombok.Builder;
import lombok.Getter;

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


    private AddressResponse address;
}

