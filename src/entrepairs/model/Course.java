package entrepairs.model;

import java.time.LocalDate;

public class Course {

    private int id;
    private int ownerUserId;
    private String name;
    private LocalDate startDate;
    private String description;
    private String shareCode;
    private CourseStatus status;

    public Course() {
        this(0, 0, "", LocalDate.now(), "", "", CourseStatus.OPEN_FOR_ENROLLMENT);
    }

    public Course(int id, int ownerUserId, String name, LocalDate startDate, String description, String shareCode, CourseStatus status) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.startDate = startDate;
        this.description = description;
        this.shareCode = shareCode;
        this.status = status;
    }

    public Course copy() {
        return new Course(id, ownerUserId, name, startDate, description, shareCode, status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }
}
