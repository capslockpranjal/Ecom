package org.example.zenvybackend.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CustomerProfileResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String contact;
}