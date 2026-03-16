package org.example.zenvybackend.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AdminSellerResponse {

    private UUID id;
    private String fullName;
    private String email;
    private Boolean isActive;
    private String companyName;
    private String companyAddress;
    private String companyContact;
}