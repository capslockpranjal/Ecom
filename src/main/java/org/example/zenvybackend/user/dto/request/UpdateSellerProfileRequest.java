package org.example.zenvybackend.user.dto.request;


import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSellerProfileRequest {

   @Pattern(regexp = "^(?!\\s*$).+", message = "First name must not be blank")
    private String firstName;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Last name must not be blank")
    private String lastName;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Company name must not be blank")
    private String companyName;

    @Pattern(regexp = "^[0-9]{10}$")
    @JsonAlias({"contact"})
    private String companyContact;
}

