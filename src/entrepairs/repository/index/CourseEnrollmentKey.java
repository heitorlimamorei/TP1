package entrepairs.repository.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.RegistroArvoreBMais;

public class CourseEnrollmentKey implements RegistroArvoreBMais<CourseEnrollmentKey> {

    private static final short RECORD_SIZE = 8;

    private int courseId;
    private int courseUserId;

    public CourseEnrollmentKey() {
        this(0, -1);
    }

    public CourseEnrollmentKey(int courseId, int courseUserId) {
        this.courseId = courseId;
        this.courseUserId = courseUserId;
    }

    public int getCourseUserId() {
        return courseUserId;
    }

    @Override
    public short size() {
        return RECORD_SIZE;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(output);
        data.writeInt(courseId);
        data.writeInt(courseUserId);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        courseId = data.readInt();
        courseUserId = data.readInt();
    }

    @Override
    public int compareTo(CourseEnrollmentKey other) {
        int courseComparison = Integer.compare(courseId, other.courseId);
        if (courseComparison != 0) {
            return courseComparison;
        }
        if (courseUserId < 0 || other.courseUserId < 0) {
            return 0;
        }
        return Integer.compare(courseUserId, other.courseUserId);
    }

    @Override
    public CourseEnrollmentKey clone() {
        return new CourseEnrollmentKey(courseId, courseUserId);
    }
}
