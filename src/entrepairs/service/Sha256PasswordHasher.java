package entrepairs.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import entrepairs.util.TextNormalizer;

public class Sha256PasswordHasher implements PasswordHasher {

    @Override
    public String hash(String value) {
        TextNormalizer.requireFilled(value, "Value");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte currentByte : bytes) {
                builder.append(String.format("%02x", currentByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    @Override
    public boolean matches(String plainText, String hashedValue) {
        return hash(plainText).equals(hashedValue);
    }
}
