package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGeneratorTest {

    @Test
    void generarPasswordEncriptada() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String adminPassword = encoder.encode("admin123");
        String operadorPassword = encoder.encode("operador123");

        System.out.println("=== CONTRASEÑAS ENCRIPTADAS ===");
        System.out.println("admin123    -> " + adminPassword);
        System.out.println("operador123 -> " + operadorPassword);
        System.out.println("================================");
    }
}
