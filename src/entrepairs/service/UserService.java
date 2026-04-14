package entrepairs.service;

import java.util.List;

import entrepairs.model.Course;
import entrepairs.model.User;
import entrepairs.repository.adapter.Aed3CourseRepository;
import entrepairs.repository.adapter.Aed3UserRepository;
import entrepairs.util.TextNormalizer;

public class UserService {

    public enum DeleteUserResult {
        DELETED,
        ACTIVE_COURSES_FOUND
    }

    private final Aed3UserRepository userRepository;
    private final Aed3CourseRepository courseRepository;
    private final PasswordHasher passwordHasher;

    public UserService(Aed3UserRepository userRepository, Aed3CourseRepository courseRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.passwordHasher = passwordHasher;
    }

    public User updateProfile(User currentUser, String name, String email, String secretQuestion, String secretAnswer) throws Exception {
        User updatedUser = currentUser.copy();
        updatedUser.setName(TextNormalizer.requireFilled(name, "Name").trim());
        updatedUser.setEmail(TextNormalizer.normalizeEmail(email));
        updatedUser.setSecretQuestion(TextNormalizer.requireFilled(secretQuestion, "Secret question").trim());
        if (secretAnswer != null && !secretAnswer.trim().isEmpty()) {
            updatedUser.setSecretAnswerHash(passwordHasher.hash(secretAnswer));
        }
        if (!userRepository.update(updatedUser)) {
            throw new IllegalStateException("User could not be updated.");
        }
        return updatedUser;
    }

    public boolean changePassword(User currentUser, String currentPassword, String newPassword) throws Exception {
        TextNormalizer.requireFilled(currentPassword, "Current password");
        TextNormalizer.requireFilled(newPassword, "New password");
        if (!passwordHasher.matches(currentPassword, currentUser.getPasswordHash())) {
            return false;
        }

        User updatedUser = currentUser.copy();
        updatedUser.setPasswordHash(passwordHasher.hash(newPassword));
        if (!userRepository.update(updatedUser)) {
            throw new IllegalStateException("Password could not be updated.");
        }

        currentUser.setPasswordHash(updatedUser.getPasswordHash());
        return true;
    }

    public DeleteUserResult deleteUser(User currentUser) throws Exception {
        List<Course> courses = courseRepository.findByOwner(currentUser.getId());
        for (Course course : courses) {
            if (course.getStatus().isActive()) {
                return DeleteUserResult.ACTIVE_COURSES_FOUND;
            }
        }

        for (Integer courseId : courseRepository.findIdsByOwner(currentUser.getId())) {
            courseRepository.delete(courseId);
        }

        if (!userRepository.delete(currentUser.getId())) {
            throw new IllegalStateException("User could not be deleted.");
        }
        return DeleteUserResult.DELETED;
    }
}
