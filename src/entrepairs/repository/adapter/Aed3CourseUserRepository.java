package entrepairs.repository.adapter;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import aed3.Arquivo;
import aed3.ArvoreBMais;
import entrepairs.model.CourseUser;
import entrepairs.repository.index.CourseEnrollmentKey;
import entrepairs.repository.index.UserEnrollmentKey;

public class Aed3CourseUserRepository implements AutoCloseable {

    private final Arquivo<CourseUserRecord> file;
    private final ArvoreBMais<CourseEnrollmentKey> courseIndex;
    private final ArvoreBMais<UserEnrollmentKey> userIndex;

    public Aed3CourseUserRepository() throws Exception {
        Constructor<CourseUserRecord> constructor = CourseUserRecord.class.getConstructor();
        this.file = new Arquivo<>("course-users", constructor);
        this.courseIndex = new ArvoreBMais<>(
            CourseEnrollmentKey.class.getConstructor(),
            5,
            Path.of("data", "indexes", "course-users", "course.idx").toString()
        );
        this.userIndex = new ArvoreBMais<>(
            UserEnrollmentKey.class.getConstructor(),
            5,
            Path.of("data", "indexes", "course-users", "user.idx").toString()
        );
    }

    public int create(CourseUser courseUser) throws Exception {
        validate(courseUser);
        int id = file.create(toRecord(courseUser));
        courseUser.setId(id);
        indexCourseUser(courseUser);
        return id;
    }

    public Optional<CourseUser> findById(int id) throws Exception {
        CourseUserRecord record = file.read(id);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(toModel(record));
    }

    public List<CourseUser> findByCourse(int courseId) throws Exception {
        List<CourseUser> courseUsers = new ArrayList<>();
        ArrayList<CourseEnrollmentKey> matches = courseIndex.read(new CourseEnrollmentKey(courseId, -1));
        for (CourseEnrollmentKey match : matches) {
            findById(match.getCourseUserId()).ifPresent(courseUsers::add);
        }
        return courseUsers;
    }

    public List<CourseUser> findByUser(int userId) throws Exception {
        List<CourseUser> courseUsers = new ArrayList<>();
        ArrayList<UserEnrollmentKey> matches = userIndex.read(new UserEnrollmentKey(userId, -1));
        for (UserEnrollmentKey match : matches) {
            findById(match.getCourseUserId()).ifPresent(courseUsers::add);
        }
        return courseUsers;
    }

    public boolean update(CourseUser courseUser) throws Exception {
        CourseUser existingCourseUser = findById(courseUser.getId()).orElse(null);
        if (existingCourseUser == null) {
            return false;
        }

        validate(courseUser);
        boolean updated = file.update(toRecord(courseUser));
        if (updated) {
            unindexCourseUser(existingCourseUser);
            indexCourseUser(courseUser);
        }
        return updated;
    }

    public boolean delete(int id) throws Exception {
        CourseUser existingCourseUser = findById(id).orElse(null);
        if (existingCourseUser == null) {
            return false;
        }
        boolean deleted = file.delete(id);
        if (deleted) {
            unindexCourseUser(existingCourseUser);
        }
        return deleted;
    }

    public void deleteByCourse(int courseId) throws Exception {
        for (CourseUser courseUser : findByCourse(courseId)) {
            delete(courseUser.getId());
        }
    }

    public void deleteByUser(int userId) throws Exception {
        for (CourseUser courseUser : findByUser(userId)) {
            delete(courseUser.getId());
        }
    }

    @Override
    public void close() throws Exception {
        file.close();
        userIndex.close();
        courseIndex.close();
    }

    private void validate(CourseUser courseUser) {
        if (courseUser.getCourseId() <= 0) {
            throw new IllegalArgumentException("Course is required.");
        }
        if (courseUser.getUserId() <= 0) {
            throw new IllegalArgumentException("User is required.");
        }
        if (courseUser.getEnrollmentDate() == null) {
            throw new IllegalArgumentException("Enrollment date is required.");
        }
    }

    private void indexCourseUser(CourseUser courseUser) throws Exception {
        courseIndex.create(new CourseEnrollmentKey(courseUser.getCourseId(), courseUser.getId()));
        userIndex.create(new UserEnrollmentKey(courseUser.getUserId(), courseUser.getId()));
    }

    private void unindexCourseUser(CourseUser courseUser) throws Exception {
        courseIndex.delete(new CourseEnrollmentKey(courseUser.getCourseId(), courseUser.getId()));
        userIndex.delete(new UserEnrollmentKey(courseUser.getUserId(), courseUser.getId()));
    }

    private CourseUserRecord toRecord(CourseUser courseUser) {
        return new CourseUserRecord(
            courseUser.getId(),
            courseUser.getCourseId(),
            courseUser.getUserId(),
            courseUser.getEnrollmentDate().toEpochDay()
        );
    }

    private CourseUser toModel(CourseUserRecord record) {
        return new CourseUser(
            record.getId(),
            record.getCourseId(),
            record.getUserId(),
            LocalDate.ofEpochDay(record.getEnrollmentDateEpochDay())
        );
    }
}
