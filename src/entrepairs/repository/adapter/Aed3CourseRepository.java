package entrepairs.repository.adapter;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import aed3.Arquivo;
import aed3.ArvoreBMais;
import entrepairs.model.Course;
import entrepairs.model.CourseStatus;
import entrepairs.repository.index.CourseShareCodeKey;
import entrepairs.repository.index.UserCourseKey;
import entrepairs.repository.index.UserCourseNameKey;
import entrepairs.util.IndexKeys;
import entrepairs.util.TextNormalizer;

public class Aed3CourseRepository implements AutoCloseable {

    private final Arquivo<CourseRecord> file;
    private final ArvoreBMais<CourseShareCodeKey> shareCodeIndex;
    private final ArvoreBMais<UserCourseNameKey> ownerCourseNameIndex;
    private final ArvoreBMais<UserCourseKey> ownerRelationIndex;

    public Aed3CourseRepository() throws Exception {
        Constructor<CourseRecord> constructor = CourseRecord.class.getConstructor();
        this.file = new Arquivo<>("courses", constructor);
        this.shareCodeIndex = new ArvoreBMais<>(
            CourseShareCodeKey.class.getConstructor(),
            5,
            Path.of("data", "indexes", "courses", "share-code.idx").toString()
        );
        this.ownerCourseNameIndex = new ArvoreBMais<>(
            UserCourseNameKey.class.getConstructor(),
            5,
            Path.of("data", "indexes", "courses", "owner-name.idx").toString()
        );
        this.ownerRelationIndex = new ArvoreBMais<>(
            UserCourseKey.class.getConstructor(),
            5,
            Path.of("data", "indexes", "courses", "owner-relation.idx").toString()
        );
    }

    public int create(Course course) throws Exception {
        validateCourse(course);
        if (findByShareCode(course.getShareCode()).isPresent()) {
            throw new IllegalArgumentException("This share code is already in use.");
        }
        int id = file.create(toRecord(course));
        course.setId(id);
        indexCourse(course);
        return id;
    }

    public Optional<Course> findById(int id) throws Exception {
        CourseRecord record = file.read(id);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(toModel(record));
    }

    public Optional<Course> findByShareCode(String shareCode) throws Exception {
        ArrayList<CourseShareCodeKey> matches = shareCodeIndex.read(new CourseShareCodeKey(IndexKeys.shareCode(shareCode), -1));
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        return findById(matches.get(0).getCourseId());
    }

    public List<Course> findByOwner(int ownerUserId) throws Exception {
        List<Course> courses = new ArrayList<>();
        ArrayList<UserCourseNameKey> matches = ownerCourseNameIndex.read(new UserCourseNameKey(ownerUserId, "", -1));
        for (UserCourseNameKey match : matches) {
            findById(match.getCourseId()).ifPresent(courses::add);
        }
        return courses;
    }

    public List<Course> findAllOrderByStartDate() throws Exception {
        List<Course> courses = new ArrayList<>();
        for (CourseRecord record : file.readAll()) {
            courses.add(toModel(record));
        }
        courses.sort(Comparator
            .comparing(Course::getStartDate)
            .thenComparing(course -> TextNormalizer.normalizeForIndex(course.getName()))
            .thenComparingInt(Course::getId));
        return courses;
    }

    public List<Integer> findIdsByOwner(int ownerUserId) throws Exception {
        List<Integer> courseIds = new ArrayList<>();
        ArrayList<UserCourseKey> matches = ownerRelationIndex.read(new UserCourseKey(ownerUserId, -1));
        for (UserCourseKey match : matches) {
            courseIds.add(match.getCourseId());
        }
        return courseIds;
    }

    public boolean update(Course course) throws Exception {
        Course existingCourse = findById(course.getId()).orElse(null);
        if (existingCourse == null) {
            return false;
        }

        validateCourse(course);

        Optional<Course> otherCourse = findByShareCode(course.getShareCode());
        if (otherCourse.isPresent() && otherCourse.get().getId() != course.getId()) {
            throw new IllegalArgumentException("This share code is already in use.");
        }

        boolean updated = file.update(toRecord(course));
        if (updated) {
            unindexCourse(existingCourse);
            indexCourse(course);
        }
        return updated;
    }

    public boolean delete(int id) throws Exception {
        Course existingCourse = findById(id).orElse(null);
        if (existingCourse == null) {
            return false;
        }
        boolean deleted = file.delete(id);
        if (deleted) {
            unindexCourse(existingCourse);
        }
        return deleted;
    }

    @Override
    public void close() throws Exception {
        file.close();
        ownerRelationIndex.close();
        ownerCourseNameIndex.close();
        shareCodeIndex.close();
    }

    private void validateCourse(Course course) {
        course.setName(TextNormalizer.requireFilled(course.getName(), "Course name").trim());
        course.setDescription(TextNormalizer.requireFilled(course.getDescription(), "Course description").trim());
        course.setShareCode(IndexKeys.shareCode(course.getShareCode()));
        if (course.getStartDate() == null) {
            throw new IllegalArgumentException("Course start date is required.");
        }
        if (course.getOwnerUserId() <= 0) {
            throw new IllegalArgumentException("Course owner is required.");
        }
    }

    private void indexCourse(Course course) throws Exception {
        shareCodeIndex.create(new CourseShareCodeKey(IndexKeys.shareCode(course.getShareCode()), course.getId()));
        ownerCourseNameIndex.create(new UserCourseNameKey(course.getOwnerUserId(), TextNormalizer.normalizeForIndex(course.getName()), course.getId()));
        ownerRelationIndex.create(new UserCourseKey(course.getOwnerUserId(), course.getId()));
    }

    private void unindexCourse(Course course) throws Exception {
        shareCodeIndex.delete(new CourseShareCodeKey(IndexKeys.shareCode(course.getShareCode()), course.getId()));
        ownerCourseNameIndex.delete(new UserCourseNameKey(course.getOwnerUserId(), TextNormalizer.normalizeForIndex(course.getName()), course.getId()));
        ownerRelationIndex.delete(new UserCourseKey(course.getOwnerUserId(), course.getId()));
    }

    private CourseRecord toRecord(Course course) {
        return new CourseRecord(
            course.getId(),
            course.getOwnerUserId(),
            course.getName(),
            course.getStartDate().toEpochDay(),
            course.getDescription(),
            course.getShareCode(),
            course.getStatus().getCode()
        );
    }

    private Course toModel(CourseRecord record) {
        return new Course(
            record.getId(),
            record.getOwnerUserId(),
            record.getName(),
            LocalDate.ofEpochDay(record.getStartDateEpochDay()),
            record.getDescription(),
            record.getShareCode(),
            CourseStatus.fromCode(record.getStatusCode())
        );
    }
}
