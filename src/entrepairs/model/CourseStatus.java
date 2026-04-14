package entrepairs.model;

public enum CourseStatus {

    OPEN_FOR_ENROLLMENT(0, "Este curso está aberto para inscrições!"),
    ENROLLMENT_CLOSED(1, "Este curso está ativo, mas não recebe novas inscrições."),
    COMPLETED(2, "Este curso foi concluído."),
    CANCELED(3, "Este curso foi cancelado.");

    private final int code;
    private final String description;

    CourseStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == OPEN_FOR_ENROLLMENT || this == ENROLLMENT_CLOSED;
    }

    public static CourseStatus fromCode(int code) {
        for (CourseStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown course status code: " + code);
    }
}
