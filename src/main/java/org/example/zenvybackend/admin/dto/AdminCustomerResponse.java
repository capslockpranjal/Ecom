package org.example.zenvybackend.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AdminCustomerResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;
    private String contact;
}

