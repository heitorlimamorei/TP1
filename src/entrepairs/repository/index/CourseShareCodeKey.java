package entrepairs.repository.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.RegistroArvoreBMais;

public class CourseShareCodeKey implements RegistroArvoreBMais<CourseShareCodeKey> {

    private static final int SHARE_CODE_SIZE = 16;
    private static final short RECORD_SIZE = (short) (SHARE_CODE_SIZE + 4);

    private String shareCode;
    private int courseId;

    public CourseShareCodeKey() {
        this("", -1);
    }

    public CourseShareCodeKey(String shareCode, int courseId) {
        this.shareCode = shareCode;
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
        data.write(FixedStringField.toBytes(shareCode, SHARE_CODE_SIZE));
        data.writeInt(courseId);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        byte[] shareCodeBytes = new byte[SHARE_CODE_SIZE];
        data.readFully(shareCodeBytes);
        shareCode = FixedStringField.fromBytes(shareCodeBytes);
        courseId = data.readInt();
    }

    @Override
    public int compareTo(CourseShareCodeKey other) {
        int shareCodeComparison = shareCode.compareTo(other.shareCode);
        if (shareCodeComparison != 0) {
            return shareCodeComparison;
        }
        if (courseId < 0 || other.courseId < 0) {
            return 0;
        }
        return Integer.compare(courseId, other.courseId);
    }

    @Override
    public CourseShareCodeKey clone() {
        return new CourseShareCodeKey(shareCode, courseId);
    }
}
