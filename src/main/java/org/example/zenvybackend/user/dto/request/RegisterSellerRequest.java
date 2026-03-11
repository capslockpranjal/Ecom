package org.example.zenvybackend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterSellerRequest {

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String gst;

    @NotBlank
    private String companyName;

    @NotBlank
    private String companyContact;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}