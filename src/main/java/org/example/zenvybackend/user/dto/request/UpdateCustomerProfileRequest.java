package org.example.zenvybackend.user.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerProfileRequest {

    // PATCH semantics: optional, but if provided must not be blank
    @Pattern(regexp = "^(?!\\s*$).+", message = "First name must not be blank")
    private String firstName;

    // PATCH semantics: optional, but if provided must not be blank
    @Pattern(regexp = "^(?!\\s*$).+", message = "Middle name must not be blank")
    private String middleName;

    // PATCH semantics: optional, but if provided must not be blank
    @Pattern(regexp = "^(?!\\s*$).+", message = "Last name must not be blank")
    private String lastName;


    @Pattern(regexp = "^[0-9]{10}$") 
    @JsonAlias({"companyContact"})
    private String contact;
}
