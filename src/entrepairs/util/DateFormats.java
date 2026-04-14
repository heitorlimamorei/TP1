package entrepairs.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateFormats {

    public static final DateTimeFormatter BRAZILIAN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateFormats() {
    }

    public static LocalDate parse(String value) {
        try {
            return LocalDate.parse(TextNormalizer.requireFilled(value, "Start date"), BRAZILIAN_DATE);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Date must use the format dd/MM/yyyy.");
        }
    }

    public static String format(LocalDate date) {
        return date.format(BRAZILIAN_DATE);
    }
}
