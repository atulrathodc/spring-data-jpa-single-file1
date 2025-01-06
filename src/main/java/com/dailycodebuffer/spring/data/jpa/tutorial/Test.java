package com.dailycodebuffer.spring.data.jpa.tutorial;

import java.security.SecureRandom;
import java.util.Base64;

public class Test {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);

        // Convert to Hex String
        int randomIntInRange = random.nextInt(10);
        System.out.println("Random Integer (0 to " + (10 - 1) + "): " + randomIntInRange);

//        System.out.println("Random Base64: " + base64String);
    }
}
