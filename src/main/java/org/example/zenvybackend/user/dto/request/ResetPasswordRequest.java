package org.example.zenvybackend.user.dto.request;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    private String token;
    private String password;

    private String confirmPassword;


}