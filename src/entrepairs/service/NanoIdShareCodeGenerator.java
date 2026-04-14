package entrepairs.service;

import java.security.SecureRandom;

public class NanoIdShareCodeGenerator implements ShareCodeGenerator {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final int SIZE = 10;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder builder = new StringBuilder(SIZE);
        for (int index = 0; index < SIZE; index++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }
}
