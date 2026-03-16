package org.example.zenvybackend.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.zenvybackend.user.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class AdminSellerResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;
    private String companyName;
    private String companyContact;
    private String gst;

    // Addresses from address table associated with this seller (user)
    private List<AddressResponse> addresses;
}

