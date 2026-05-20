package entrepairs.repository.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.RegistroArvoreBMais;

public class UserEnrollmentKey implements RegistroArvoreBMais<UserEnrollmentKey> {

    private static final short RECORD_SIZE = 8;

    private int userId;
    private int courseUserId;

    public UserEnrollmentKey() {
        this(0, -1);
    }

    public UserEnrollmentKey(int userId, int courseUserId) {
        this.userId = userId;
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
        data.writeInt(userId);
        data.writeInt(courseUserId);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        userId = data.readInt();
        courseUserId = data.readInt();
    }

    @Override
    public int compareTo(UserEnrollmentKey other) {
        int userComparison = Integer.compare(userId, other.userId);
        if (userComparison != 0) {
            return userComparison;
        }
        if (courseUserId < 0 || other.courseUserId < 0) {
            return 0;
        }
        return Integer.compare(courseUserId, other.courseUserId);
    }

    @Override
    public UserEnrollmentKey clone() {
        return new UserEnrollmentKey(userId, courseUserId);
    }
}
