package org.example.zenvybackend.user.dto.request;



import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    private String token;
    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;


}