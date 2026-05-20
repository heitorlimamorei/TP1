package entrepairs.model;

import java.time.LocalDate;

public class CourseUser {

    private int id;
    private int courseId;
    private int userId;
    private LocalDate enrollmentDate;

    public CourseUser() {
        this(0, 0, 0, LocalDate.now());
    }

    public CourseUser(int id, int courseId, int userId, LocalDate enrollmentDate) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.enrollmentDate = enrollmentDate;
    }

    public CourseUser copy() {
        return new CourseUser(id, courseId, userId, enrollmentDate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}
