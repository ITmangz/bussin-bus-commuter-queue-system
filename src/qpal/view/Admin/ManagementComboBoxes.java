package qpal.view.Admin;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;

final class ManagementComboBoxes {
    private ManagementComboBoxes() {}

    static void validateBusOnSelection(JComboBox<qpal.model.Bus> combo, Component parent) {
        combo.addActionListener(e -> validateBusSelection(combo, parent));
    }

    static boolean validateBusSelection(JComboBox<qpal.model.Bus> combo, Component parent) {
        qpal.model.Bus bus = (qpal.model.Bus)combo.getSelectedItem();
        if (bus == null) return true;
        String status = bus.getBusStatus();
        String message;
        if ("Maintenance".equalsIgnoreCase(status)) {
            message = "Bus is in maintenance.";
        } else if ("Inactive".equalsIgnoreCase(status)) {
            message = "Bus is not in the operations.";
        } else {
            return true;
        }
        combo.setSelectedIndex(-1);
        qpal.components.AppDialogs.showMessageDialog(parent, message,
                "Bus Unavailable", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    static JComboBox<Integer> seatCapacity(int selected) {
        JComboBox<Integer> combo = new JComboBox<>();
        for (int seats = 20; seats <= 50; seats += 5) combo.addItem(seats);
        combo.setSelectedIndex(selected >= 20 && selected <= 50 && selected % 5 == 0
                ? (selected - 20) / 5 : -1);
        style(combo);
        return combo;
    }

    static JComboBox<LocalTime> departureTime(String selected) {
        JComboBox<LocalTime> combo = new JComboBox<>();
        for (int minute = 0; minute < 1440; minute += 30)
            combo.addItem(LocalTime.MIDNIGHT.plusMinutes(minute));
        combo.setSelectedIndex(-1);
        if (selected != null) {
            try {
                LocalTime time = LocalTime.parse(selected);
                if (time.getMinute() % 30 != 0 || time.getSecond() != 0 || time.getNano() != 0)
                    combo.addItem(time);
                combo.setSelectedItem(time);
            } catch (java.time.format.DateTimeParseException ex) {
                // Invalid legacy values require a new selection.
            }
        }
        style(combo);
        return combo;
    }

    private static void style(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setForeground(new Color(20, 23, 28));
        combo.setMaximumRowCount(8);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension size = new Dimension(300, 38);
        combo.setMinimumSize(size);
        combo.setPreferredSize(size);
        combo.setMaximumSize(size);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof LocalTime) {
                    LocalTime time = (LocalTime)value;
                    setText(time.format(DateTimeFormatter.ofPattern(
                            time.getSecond() == 0 ? "h:mm a" : "h:mm:ss a", Locale.ENGLISH)));
                }
                setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 8));
                setBackground(selected && index >= 0 ? new Color(255, 235, 240) : Color.WHITE);
                setForeground(new Color(20, 23, 28));
                return this;
            }
        });
    }
}
