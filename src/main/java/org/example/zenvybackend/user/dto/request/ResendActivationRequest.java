package org.example.zenvybackend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendActivationRequest {

    @NotBlank
    @Email
    private String email;
}

