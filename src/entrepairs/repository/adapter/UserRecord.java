package entrepairs.repository.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.Registro;

public class UserRecord implements Registro {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String secretQuestion;
    private String secretAnswerHash;

    public UserRecord() {
        this(0, "", "", "", "", "");
    }

    public UserRecord(int id, String name, String email, String passwordHash, String secretQuestion, String secretAnswerHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.secretQuestion = secretQuestion;
        this.secretAnswerHash = secretAnswerHash;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSecretQuestion() {
        return secretQuestion;
    }

    public String getSecretAnswerHash() {
        return secretAnswerHash;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(output);
        data.writeInt(id);
        data.writeUTF(name);
        data.writeUTF(email);
        data.writeUTF(passwordHash);
        data.writeUTF(secretQuestion);
        data.writeUTF(secretAnswerHash);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        id = data.readInt();
        name = data.readUTF();
        email = data.readUTF();
        passwordHash = data.readUTF();
        secretQuestion = data.readUTF();
        secretAnswerHash = data.readUTF();
    }
}
