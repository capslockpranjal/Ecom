package org.example.zenvybackend.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerProfileRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank
    private String lastName;


    @Pattern(regexp = "^[0-9]{10}$")
    private String contact;
}
