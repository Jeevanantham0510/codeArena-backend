package com.codearena.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "8526903021";

        System.out.println(encoder.encode(password));
    }
}