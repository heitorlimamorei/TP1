package entrepairs.service;

public interface PasswordHasher {

    String hash(String value);

    boolean matches(String plainText, String hashedValue);
}
