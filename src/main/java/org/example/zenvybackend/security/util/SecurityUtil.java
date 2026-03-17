package org.example.zenvybackend.security.util;

import org.example.zenvybackend.security.service.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtil {

    public static UUID getCurrentUserId(){

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if(principal instanceof CustomUserDetails userDetails){
            return userDetails.getId();
        }

        throw new RuntimeException("Invalid authentication principal");
    }
}