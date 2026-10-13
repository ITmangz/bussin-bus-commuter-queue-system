import qpal.model.StatusReason;
import qpal.components.StatusReasonField;
import qpal.components.StatusReasonRenderer;
import javax.swing.*;

/** Offline validation and Swing behavior checks; no database changes. */
public class StatusReasonTest {
    public static void main(String[] args) throws Exception {
        for (String status : new String[] {"Maintenance", "Inactive"}) {
            rejects(status, null);
            rejects(status, " \n\t ");
            rejects(status, "x".repeat(501));
            check(StatusReason.normalize(status, "  Scheduled inspection.  ")
                    .equals("Scheduled inspection."), "Trim reason");
            check(StatusReason.normalize(status, "x".repeat(500)).length() == 500,
                    "Allow maximum length");
            check(StatusReason.display(status, null).equals("Not recorded"), "Legacy reason");
        }
        for (String status : new String[] {"Active", "Available"})
            check(StatusReason.normalize(status, "Previous reason") == null, "Clear old reason");
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<String> status = new JComboBox<>(new String[] {"Maintenance", "Available"});
            StatusReasonField field = new StatusReasonField(status, "Scheduled brake inspection.");
            check(field.reason().equals("Scheduled brake inspection."), "Prepopulate reason");
            status.setSelectedItem("Available");
            check(field.reason() == null, "Reactivation clears persisted reason");
            status.setSelectedItem("Maintenance");
            check(field.reason().equals("Scheduled brake inspection."), "Retain draft while toggling");
            StatusReasonRenderer renderer = new StatusReasonRenderer();
            JTable table = new JTable(new Object[][] {{""}}, new Object[] {"Reason for Status Change"});
            renderer.getTableCellRendererComponent(table, "<html>Inspection & repair", false, false, 0, 0);
            check(renderer.getToolTipText().contains("&lt;html&gt;Inspection &amp; repair"),
                    "Render staff input literally");
        });
        System.out.println("StatusReasonTest passed");
    }

    private static void rejects(String status, String reason) {
        try {
            StatusReason.normalize(status, reason);
            throw new AssertionError("Invalid reason accepted");
        } catch (IllegalArgumentException expected) {}
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
