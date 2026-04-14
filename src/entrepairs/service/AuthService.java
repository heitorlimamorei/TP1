package entrepairs.service;

import java.util.Optional;

import entrepairs.model.User;
import entrepairs.repository.adapter.Aed3UserRepository;
import entrepairs.util.TextNormalizer;

public class AuthService {

    private final Aed3UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthService(Aed3UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User register(String name, String email, String password, String secretQuestion, String secretAnswer) throws Exception {
        validateCommonFields(name, email, password, secretQuestion, secretAnswer);
        User user = new User();
        user.setName(name.trim());
        user.setEmail(TextNormalizer.normalizeEmail(email));
        user.setPasswordHash(passwordHasher.hash(password));
        user.setSecretQuestion(secretQuestion.trim());
        user.setSecretAnswerHash(passwordHasher.hash(secretAnswer));
        userRepository.create(user);
        return user;
    }

    public Optional<User> authenticate(String email, String password) throws Exception {
        TextNormalizer.requireFilled(email, "Email");
        TextNormalizer.requireFilled(password, "Password");
        Optional<User> storedUser = userRepository.findByEmail(email);
        if (storedUser.isEmpty()) {
            return Optional.empty();
        }
        if (!passwordHasher.matches(password, storedUser.get().getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(storedUser.get());
    }

    public Optional<String> findSecretQuestion(String email) throws Exception {
        Optional<User> storedUser = userRepository.findByEmail(email);
        return storedUser.map(User::getSecretQuestion);
    }

    public boolean resetPassword(String email, String secretAnswer, String newPassword) throws Exception {
        Optional<User> storedUser = userRepository.findByEmail(email);
        if (storedUser.isEmpty()) {
            return false;
        }
        if (!passwordHasher.matches(secretAnswer, storedUser.get().getSecretAnswerHash())) {
            return false;
        }
        TextNormalizer.requireFilled(newPassword, "New password");
        User user = storedUser.get().copy();
        user.setPasswordHash(passwordHasher.hash(newPassword));
        return userRepository.update(user);
    }

    private void validateCommonFields(String name, String email, String password, String secretQuestion, String secretAnswer) {
        TextNormalizer.requireFilled(name, "Name");
        TextNormalizer.requireFilled(email, "Email");
        TextNormalizer.requireFilled(password, "Password");
        TextNormalizer.requireFilled(secretQuestion, "Secret question");
        TextNormalizer.requireFilled(secretAnswer, "Secret answer");
    }
}
