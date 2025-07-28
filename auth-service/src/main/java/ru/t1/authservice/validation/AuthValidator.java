package ru.t1.authservice.validation;

public class AuthValidator {

    public static boolean authValidation(String username, String password) {
        return usernameValidation(username) && passwordValidation(password);
    }

    public static boolean usernameValidation(String username) {
        return username.length() >= 4 &&
                username.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
    public static boolean passwordValidation(String password) {
        return password.length() >= 6;
    }
}
