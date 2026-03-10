package org.example.zenvybackend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterCustomerRequest {

    @Email
    private String email;

    @NotBlank
    private String password;

    private String confirmPassword;

    private String firstName;
    private String lastName;
}