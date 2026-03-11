package org.example.zenvybackend.security.util;

import org.example.zenvybackend.security.service.CustomUserDetails;
import org.example.zenvybackend.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static User getCurrentUser(){

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if(principal instanceof CustomUserDetails userDetails){
            return userDetails.getUser();
        }

        throw new RuntimeException("Invalid authentication principal");
    }
}