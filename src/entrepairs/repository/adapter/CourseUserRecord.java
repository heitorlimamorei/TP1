package entrepairs.repository.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import aed3.Registro;

public class CourseUserRecord implements Registro {

    private int id;
    private int courseId;
    private int userId;
    private long enrollmentDateEpochDay;

    public CourseUserRecord() {
        this(0, 0, 0, LocalDate.now().toEpochDay());
    }

    public CourseUserRecord(int id, int courseId, int userId, long enrollmentDateEpochDay) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.enrollmentDateEpochDay = enrollmentDateEpochDay;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getCourseId() {
        return courseId;
    }

    public int getUserId() {
        return userId;
    }

    public long getEnrollmentDateEpochDay() {
        return enrollmentDateEpochDay;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(output);
        data.writeInt(id);
        data.writeInt(courseId);
        data.writeInt(userId);
        data.writeLong(enrollmentDateEpochDay);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        id = data.readInt();
        courseId = data.readInt();
        userId = data.readInt();
        enrollmentDateEpochDay = data.readLong();
    }
}
