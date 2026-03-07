package org.example.zenvybackend.common.util;

public class PasswordValidator {

    public static boolean isValid(String password){

        if(password == null || password.length() < 8 || password.length() > 15){
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for(char c : password.toCharArray()){

            if(Character.isUpperCase(c)) hasUpper = true;
            else if(Character.isLowerCase(c)) hasLower = true;
            else if(Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}