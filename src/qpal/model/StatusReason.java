package qpal.model;

/** Shared validation for forms and persistence. */
public final class StatusReason {
    private StatusReason() {}

    public static boolean required(String status) {
        return "Maintenance".equals(status) || "Inactive".equals(status);
    }

    public static String normalize(String status, String reason) {
        if (!required(status)) return null;
        String value = reason == null ? "" : reason.strip();
        if (value.isBlank())
            throw new IllegalArgumentException("Please provide a reason for the selected status.");
        if (value.length() > 500)
            throw new IllegalArgumentException("The reason must not exceed 500 characters.");
        return value;
    }

    public static String display(String status, String reason) {
        if (!required(status)) return "Not applicable";
        return reason == null || reason.isBlank() ? "Not recorded" : reason;
    }
}
