package entrepairs.util;

public final class IndexKeys {

    private IndexKeys() {
    }

    public static String email(String email) {
        return TextNormalizer.normalizeEmail(email);
    }

    public static String shareCode(String shareCode) {
        return TextNormalizer.requireFilled(shareCode, "Share code").trim().toLowerCase();
    }

    public static String ownerPrefix(int ownerId) {
        return String.format("%010d|", ownerId);
    }

    public static String ownerCourseRelation(int ownerId, int courseId) {
        return ownerPrefix(ownerId) + String.format("%010d", courseId);
    }

    public static String ownerCourseName(int ownerId, String courseName, int courseId) {
        return ownerPrefix(ownerId) + TextNormalizer.normalizeForIndex(courseName) + "|" + String.format("%010d", courseId);
    }
}
