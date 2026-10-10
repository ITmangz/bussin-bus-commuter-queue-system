package qpal.view.Admin;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Gate prompts using the same card, fields and actions as the management forms. */
final class GateActionDialog {
    private GateActionDialog() {}

    static int show(
            Component parent, String title, String message, JComboBox<?> trips, String... options) {
        JDialog dialog =
                new JDialog(
                        SwingUtilities.getWindowAncestor(parent),
                        title,
                        Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int[] result = {JOptionPane.CLOSED_OPTION};

        JPanel form = new JPanel(new BorderLayout(0, 25));
        form.setBorder(new EmptyBorder(25, 30, 25, 30));
        JPanel content = new JPanel(new BorderLayout(0, 7));
        content.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(new Color(225, 29, 72));
        content.add(heading, BorderLayout.NORTH);
        JTextArea description = new JTextArea(message);
        description.setFont(new Font("SansSerif", Font.PLAIN, 12));
        description.setForeground(new Color(100, 100, 100));
        description.setOpaque(false);
        description.setEditable(false);
        description.setFocusable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setSize(360, Short.MAX_VALUE);
        description.setPreferredSize(new Dimension(360, description.getPreferredSize().height));
        content.add(description, BorderLayout.CENTER);
        if (trips != null) {
            JPanel field = new JPanel(new BorderLayout(0, 7));
            field.setOpaque(false);
            field.setBorder(new EmptyBorder(13, 0, 0, 0));
            JLabel label = new JLabel("Trip");
            label.setFont(new Font("SansSerif", Font.BOLD, 13));
            field.add(label, BorderLayout.NORTH);
            trips.setPreferredSize(new Dimension(360, 38));
            trips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            field.add(trips, BorderLayout.CENTER);
            content.add(field, BorderLayout.SOUTH);
        }
        form.add(content, BorderLayout.CENTER);

        // Stack longer departure choices so every action remains readable.
        JPanel actions =
                new JPanel(
                        new GridLayout(
                                options.length > 2 ? options.length : 1,
                                options.length > 2 ? 1 : options.length,
                                12,
                                12));
        actions.setOpaque(false);
        actions.putClientProperty("fullWidthActions", Boolean.TRUE);
        int actionRows = options.length > 2 ? options.length : 1;
        actions.setPreferredSize(new Dimension(360, actionRows * 38 + (actionRows - 1) * 12));
        JButton cancel = null;
        for (int i = 0; i < options.length; i++) {
            final int choice = i;
            JButton button = new JButton(options[i]);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setBackground(
                    i == options.length - 1
                            ? new Color(240, 0, 55)
                            : i == 0 ? new Color(0, 190, 100) : new Color(245, 158, 0));
            button.setPreferredSize(new Dimension(360, 38));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(
                    e -> {
                        result[0] = choice;
                        dialog.dispose();
                    });
            actions.add(button);
            cancel = button;
        }
        form.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(AdminFormStyle.frame(form));
        dialog.getRootPane().setDefaultButton(cancel);
        dialog.getRootPane()
                .registerKeyboardAction(
                        e -> dialog.dispose(),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return result[0];
    }
}
