package entrepairs.repository.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.RegistroArvoreBMais;

public class UserCourseNameKey implements RegistroArvoreBMais<UserCourseNameKey> {

    private static final int NAME_SIZE = 120;
    private static final short RECORD_SIZE = (short) (4 + NAME_SIZE + 4);

    private int ownerUserId;
    private String normalizedCourseName;
    private int courseId;

    public UserCourseNameKey() {
        this(0, "", -1);
    }

    public UserCourseNameKey(int ownerUserId, String normalizedCourseName, int courseId) {
        this.ownerUserId = ownerUserId;
        this.normalizedCourseName = normalizedCourseName;
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
        data.write(FixedStringField.toBytes(normalizedCourseName, NAME_SIZE));
        data.writeInt(courseId);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        ownerUserId = data.readInt();
        byte[] nameBytes = new byte[NAME_SIZE];
        data.readFully(nameBytes);
        normalizedCourseName = FixedStringField.fromBytes(nameBytes);
        courseId = data.readInt();
    }

    @Override
    public int compareTo(UserCourseNameKey other) {
        int ownerComparison = Integer.compare(ownerUserId, other.ownerUserId);
        if (ownerComparison != 0) {
            return ownerComparison;
        }
        if (courseId < 0 || other.courseId < 0) {
            return 0;
        }
        int nameComparison = normalizedCourseName.compareTo(other.normalizedCourseName);
        if (nameComparison != 0) {
            return nameComparison;
        }
        return Integer.compare(courseId, other.courseId);
    }

    @Override
    public UserCourseNameKey clone() {
        return new UserCourseNameKey(ownerUserId, normalizedCourseName, courseId);
    }
}
