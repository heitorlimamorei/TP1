package entrepairs.model;

public class CourseSearchResult {

    private final Course course;
    private final double score;

    public CourseSearchResult(Course course, double score) {
        this.course = course;
        this.score = score;
    }

    public Course getCourse() {
        return course;
    }

    public double getScore() {
        return score;
    }
}
