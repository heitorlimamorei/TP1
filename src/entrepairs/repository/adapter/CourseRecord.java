package entrepairs.repository.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import aed3.Registro;
import entrepairs.model.CourseStatus;

public class CourseRecord implements Registro {

    private int id;
    private int ownerUserId;
    private String name;
    private long startDateEpochDay;
    private String description;
    private String shareCode;
    private int statusCode;

    public CourseRecord() {
        this(0, 0, "", LocalDate.now().toEpochDay(), "", "", CourseStatus.OPEN_FOR_ENROLLMENT.getCode());
    }

    public CourseRecord(int id, int ownerUserId, String name, long startDateEpochDay, String description, String shareCode, int statusCode) {
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.startDateEpochDay = startDateEpochDay;
        this.description = description;
        this.shareCode = shareCode;
        this.statusCode = statusCode;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public String getName() {
        return name;
    }

    public long getStartDateEpochDay() {
        return startDateEpochDay;
    }

    public String getDescription() {
        return description;
    }

    public String getShareCode() {
        return shareCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(output);
        data.writeInt(id);
        data.writeInt(ownerUserId);
        data.writeUTF(name);
        data.writeLong(startDateEpochDay);
        data.writeUTF(description);
        data.writeUTF(shareCode);
        data.writeInt(statusCode);
        return output.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] bytes) throws IOException {
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        id = data.readInt();
        ownerUserId = data.readInt();
        name = data.readUTF();
        startDateEpochDay = data.readLong();
        description = data.readUTF();
        shareCode = data.readUTF();
        statusCode = data.readInt();
    }
}
