package entrepairs.repository.index;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class FixedStringField {

    private FixedStringField() {
    }

    static byte[] toBytes(String value, int size) {
        byte[] result = new byte[size];
        Arrays.fill(result, (byte) ' ');
        if (value == null) {
            return result;
        }

        byte[] source = value.getBytes(StandardCharsets.UTF_8);
        int length = Math.min(source.length, size);
        System.arraycopy(source, 0, result, 0, length);
        return result;
    }

    static String fromBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }
}
