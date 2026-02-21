package com.games.util;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generateKey() {
        byte[] bytes = new byte[24]; // 192 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static void main(String[] args) {
        String apiKey = generateKey();
        System.out.println("API KEY: " + apiKey);
    }
}
