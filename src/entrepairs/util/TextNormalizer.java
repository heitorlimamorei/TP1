package entrepairs.util;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalizeEmail(String email) {
        return requireFilled(email, "Email").trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeForIndex(String value) {
        String normalized = Normalizer.normalize(requireFilled(value, "Text"), Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");
        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    public static String requireFilled(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }
}
