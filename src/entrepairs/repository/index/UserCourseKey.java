package entrepairs.repository.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.RegistroArvoreBMais;

public class UserCourseKey implements RegistroArvoreBMais<UserCourseKey> {

    private static final short RECORD_SIZE = 8;

    private int ownerUserId;
    private int courseId;

    public UserCourseKey() {
        this(0, -1);
    }

    public UserCourseKey(int ownerUserId, int courseId) {
        this.ownerUserId = ownerUserId;
        this.courseId = courseId;
    }

    public int getCourseId() {
        return courseId;
    }

    @Override
    public short size() {
        return RECORD_SIZE;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(output);
        data.writeInt(ownerUserId);
        data.writeInt(courseId);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        ownerUserId = data.readInt();
        courseId = data.readInt();
    }

    @Override
    public int compareTo(UserCourseKey other) {
        int ownerComparison = Integer.compare(ownerUserId, other.ownerUserId);
        if (ownerComparison != 0) {
            return ownerComparison;
        }
        if (courseId < 0 || other.courseId < 0) {
            return 0;
        }
        return Integer.compare(courseId, other.courseId);
    }

    @Override
    public UserCourseKey clone() {
        return new UserCourseKey(ownerUserId, courseId);
    }
}
