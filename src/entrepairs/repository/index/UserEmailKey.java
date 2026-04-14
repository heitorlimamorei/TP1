package entrepairs.repository.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.RegistroArvoreBMais;

public class UserEmailKey implements RegistroArvoreBMais<UserEmailKey> {

    private static final int EMAIL_SIZE = 120;
    private static final short RECORD_SIZE = (short) (EMAIL_SIZE + 4);

    private String email;
    private int userId;

    public UserEmailKey() {
        this("", -1);
    }

    public UserEmailKey(String email, int userId) {
        this.email = email;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public short size() {
        return RECORD_SIZE;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(output);
        data.write(FixedStringField.toBytes(email, EMAIL_SIZE));
        data.writeInt(userId);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        byte[] emailBytes = new byte[EMAIL_SIZE];
        data.readFully(emailBytes);
        email = FixedStringField.fromBytes(emailBytes);
        userId = data.readInt();
    }

    @Override
    public int compareTo(UserEmailKey other) {
        int emailComparison = email.compareTo(other.email);
        if (emailComparison != 0) {
            return emailComparison;
        }
        if (userId < 0 || other.userId < 0) {
            return 0;
        }
        return Integer.compare(userId, other.userId);
    }

    @Override
    public UserEmailKey clone() {
        return new UserEmailKey(email, userId);
    }
}
