package org.example.zenvybackend.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAddressRequest {

    @Pattern(regexp = "^(?!\\s*$).+", message = "City must not be blank")
    // PATCH semantics: all fields optional; if provided, they overwrite existing values
    private String city;

    @Pattern(regexp = "^(?!\\s*$).+", message = "State must not be blank")
    private String state;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Country must not be blank")
    private String country;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Address line must not be blank")
    private String addressLine;

    @Pattern(regexp = "^[0-9]{6}$", message = "Zip code must be a 6 digit number")
    private String zipCode;

    private String label;
}

