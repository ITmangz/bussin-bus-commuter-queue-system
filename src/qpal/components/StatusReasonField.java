package qpal.components;

import java.awt.*;
import javax.swing.*;

/** A required operational reason for a restricted status. */
public final class StatusReasonField extends JPanel {
    private final JTextArea input = new JTextArea(3, 24);
    private final JComboBox<String> status;

    public StatusReasonField(JComboBox<String> status, String existingReason) {
        this.status = status;
        setOpaque(false);
        setLayout(new BorderLayout(0, 5));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(300, 108));
        setMinimumSize(new Dimension(300, 108));
        setMaximumSize(new Dimension(300, 108));
        JLabel label = new JLabel("Reason for Status Change");
        label.setLabelFor(input);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        input.setLineWrap(true);
        input.setWrapStyleWord(true);
        input.setText(existingReason == null ? "" : existingReason);
        input.getAccessibleContext().setAccessibleName("Reason for Status Change");
        add(label, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(input,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Color fieldBackground = new Color(248, 249, 251);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(fieldBackground);
        ScrollBarStyle.apply(scroll, fieldBackground);
        add(scroll, BorderLayout.CENTER);
        JLabel hint = new JLabel("Required for Maintenance or Inactive. Maximum 500 characters.");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 10));
        add(hint, BorderLayout.SOUTH);
        Runnable update = () -> {
            boolean required = qpal.model.StatusReason.required((String) status.getSelectedItem());
            input.setEnabled(required);
            hint.setText(required ? "Required. Maximum 500 characters."
                    : "No reason is required for this status.");
        };
        status.addActionListener(event -> update.run());
        update.run();
    }

    public String reason() {
        return qpal.model.StatusReason.normalize((String) status.getSelectedItem(), input.getText());
    }

    public boolean validateInput(Component parent) {
        try {
            reason();
            return true;
        } catch (IllegalArgumentException ex) {
            AppDialogs.showMessageDialog(parent, ex.getMessage(), "Status Change Reason",
                    JOptionPane.WARNING_MESSAGE);
            input.requestFocusInWindow();
            return false;
        }
    }
}
