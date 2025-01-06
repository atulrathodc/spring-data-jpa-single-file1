package com.dailycodebuffer.spring.data.jpa.tutorial;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


import java.security.SecureRandom;

public class Test {
    static Logger log = Logger.getLogger(Test.class.getName());
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);

        // Convert to Hex String
        int randomIntInRange = random.nextInt(10);
        if (log.isLoggable(Level.INFO)) {
            log.info("Random " + randomIntInRange);
        }

    }
}
