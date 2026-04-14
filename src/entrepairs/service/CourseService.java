package entrepairs.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import entrepairs.model.Course;
import entrepairs.model.CourseStatus;
import entrepairs.model.User;
import entrepairs.repository.adapter.Aed3CourseRepository;
import entrepairs.util.TextNormalizer;

public class CourseService {

    public enum CancelCourseResult {
        DELETED,
        NOT_FOUND
    }

    private final Aed3CourseRepository courseRepository;
    private final ShareCodeGenerator shareCodeGenerator;

    public CourseService(Aed3CourseRepository courseRepository, ShareCodeGenerator shareCodeGenerator) {
        this.courseRepository = courseRepository;
        this.shareCodeGenerator = shareCodeGenerator;
    }

    public Course createCourse(User owner, String name, LocalDate startDate, String description) throws Exception {
        Course course = new Course();
        course.setOwnerUserId(owner.getId());
        course.setName(TextNormalizer.requireFilled(name, "Course name").trim());
        course.setStartDate(startDate);
        course.setDescription(TextNormalizer.requireFilled(description, "Course description").trim());
        course.setStatus(CourseStatus.OPEN_FOR_ENROLLMENT);
        course.setShareCode(generateUniqueShareCode());
        courseRepository.create(course);
        return course;
    }

    public List<Course> listCourses(User owner) throws Exception {
        return courseRepository.findByOwner(owner.getId());
    }

    public Optional<Course> findById(int id) throws Exception {
        return courseRepository.findById(id);
    }

    public Course updateCourse(Course currentCourse, String name, LocalDate startDate, String description) throws Exception {
        Course updatedCourse = currentCourse.copy();
        updatedCourse.setName(TextNormalizer.requireFilled(name, "Course name").trim());
        updatedCourse.setStartDate(startDate);
        updatedCourse.setDescription(TextNormalizer.requireFilled(description, "Course description").trim());
        if (!courseRepository.update(updatedCourse)) {
            throw new IllegalStateException("Course could not be updated.");
        }
        return updatedCourse;
    }

    public Course closeEnrollments(Course currentCourse) throws Exception {
        Course updatedCourse = currentCourse.copy();
        updatedCourse.setStatus(CourseStatus.ENROLLMENT_CLOSED);
        if (!courseRepository.update(updatedCourse)) {
            throw new IllegalStateException("Course could not be updated.");
        }
        return updatedCourse;
    }

    public Course concludeCourse(Course currentCourse) throws Exception {
        Course updatedCourse = currentCourse.copy();
        updatedCourse.setStatus(CourseStatus.COMPLETED);
        if (!courseRepository.update(updatedCourse)) {
            throw new IllegalStateException("Course could not be updated.");
        }
        return updatedCourse;
    }

    public CancelCourseResult cancelCourse(Course currentCourse) throws Exception {
        if (courseRepository.delete(currentCourse.getId())) {
            return CancelCourseResult.DELETED;
        }
        return CancelCourseResult.NOT_FOUND;
    }

    private String generateUniqueShareCode() throws Exception {
        String shareCode;
        do {
            shareCode = shareCodeGenerator.generate();
        } while (courseRepository.findByShareCode(shareCode).isPresent());
        return shareCode;
    }
}
