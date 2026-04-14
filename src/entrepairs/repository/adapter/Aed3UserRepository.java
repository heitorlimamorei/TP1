package entrepairs.repository.adapter;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

import aed3.Arquivo;
import aed3.ArvoreBMais;
import entrepairs.model.User;
import entrepairs.repository.index.UserEmailKey;
import entrepairs.util.IndexKeys;
import entrepairs.util.TextNormalizer;

public class Aed3UserRepository implements AutoCloseable {

    private final Arquivo<UserRecord> file;
    private final ArvoreBMais<UserEmailKey> emailIndex;

    public Aed3UserRepository() throws Exception {
        Constructor<UserRecord> constructor = UserRecord.class.getConstructor();
        this.file = new Arquivo<>("users", constructor);
        this.emailIndex = new ArvoreBMais<>(
            UserEmailKey.class.getConstructor(),
            5,
            Path.of("data", "indexes", "users", "email.idx").toString()
        );
    }

    public int create(User user) throws Exception {
        user.setName(TextNormalizer.requireFilled(user.getName(), "Name").trim());
        user.setEmail(IndexKeys.email(user.getEmail()));
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("There is already a user with this email.");
        }
        int id = file.create(toRecord(user));
        user.setId(id);
        emailIndex.create(new UserEmailKey(IndexKeys.email(user.getEmail()), id));
        return id;
    }

    public Optional<User> findById(int id) throws Exception {
        UserRecord record = file.read(id);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(toModel(record));
    }

    public Optional<User> findByEmail(String email) throws Exception {
        ArrayList<UserEmailKey> matches = emailIndex.read(new UserEmailKey(IndexKeys.email(email), -1));
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        return findById(matches.get(0).getUserId());
    }

    public boolean update(User user) throws Exception {
        User existingUser = findById(user.getId()).orElse(null);
        if (existingUser == null) {
            return false;
        }

        user.setName(TextNormalizer.requireFilled(user.getName(), "Name").trim());
        user.setEmail(IndexKeys.email(user.getEmail()));

        Optional<User> otherUser = findByEmail(user.getEmail());
        if (otherUser.isPresent() && otherUser.get().getId() != user.getId()) {
            throw new IllegalArgumentException("There is already a user with this email.");
        }

        boolean updated = file.update(toRecord(user));
        if (updated && !existingUser.getEmail().equals(user.getEmail())) {
            emailIndex.delete(new UserEmailKey(IndexKeys.email(existingUser.getEmail()), existingUser.getId()));
            emailIndex.create(new UserEmailKey(IndexKeys.email(user.getEmail()), user.getId()));
        }
        return updated;
    }

    public boolean delete(int id) throws Exception {
        User existingUser = findById(id).orElse(null);
        if (existingUser == null) {
            return false;
        }
        boolean deleted = file.delete(id);
        if (deleted) {
            emailIndex.delete(new UserEmailKey(IndexKeys.email(existingUser.getEmail()), id));
        }
        return deleted;
    }

    @Override
    public void close() throws Exception {
        file.close();
        emailIndex.close();
    }

    private UserRecord toRecord(User user) {
        return new UserRecord(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getSecretQuestion(),
            user.getSecretAnswerHash()
        );
    }

    private User toModel(UserRecord record) {
        return new User(
            record.getId(),
            record.getName(),
            record.getEmail(),
            record.getPasswordHash(),
            record.getSecretQuestion(),
            record.getSecretAnswerHash()
        );
    }
}
