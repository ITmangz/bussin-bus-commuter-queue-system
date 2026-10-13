package qpal.components;

import java.awt.*;
import java.util.function.Function;
import javax.swing.*;

/** Context-specific help for bus and account status controls. */
public final class StatusDescriptions {
    private StatusDescriptions() {}

    public static String bus(String status) {
        return switch (status) {
            case "Available" -> "Bus is in service and available for trips.";
            case "Maintenance" -> "Bus is unavailable while undergoing repairs or servicing.";
            case "Inactive" -> "Bus is out of service until reactivated.";
            default -> "Select a status to see its description.";
        };
    }

    public static String account(String status) {
        return switch (status) {
            case "Active" -> "Account is enabled and can sign in.";
            case "Inactive" -> "Account is disabled and cannot sign in until reactivated.";
            default -> "Select a status to see its description.";
        };
    }

    public static JLabel help(JComboBox<String> selection, Function<String, String> description) {
        JLabel help = new JLabel();
        help.setFont(new Font("SansSerif", Font.PLAIN, 12));
        help.setForeground(new Color(90, 100, 115));
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        help.setPreferredSize(new Dimension(300, 48));
        help.setMaximumSize(new Dimension(300, 48));
        Runnable update = () -> {
            String text = description.apply(selection.getSelectedItem() == null
                    ? "" : selection.getSelectedItem().toString());
            help.setText("<html><body style='width:220px'>" + text + "</body></html>");
            selection.setToolTipText(text);
        };
        selection.addActionListener(event -> update.run());
        update.run();
        return help;
    }
}
