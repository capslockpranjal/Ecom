package org.example.zenvybackend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterSellerRequest {

    @NotBlank @Email
    private String email;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must contain uppercase, lowercase, number and minimum 8 characters"
    )
    private String password;

    @NotBlank
    private String confirmPassword;

    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GST number"
    ) @NotBlank
    private String gst;

    @NotBlank
    private String companyName;

    @NotBlank
    private String companyContact;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String companyAddress;
}