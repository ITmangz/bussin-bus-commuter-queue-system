package qpal.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class StatusRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);

        JLabel label = new JLabel(value == null ? "" : value.toString());

        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setOpaque(true);

        String status = value == null ? "" : value.toString();
        Color background;
        Color foreground;
        switch (status) {
            case "Active": case "Available": case "Scheduled": case "Paid":
                background = new Color(220,252,231); foreground = new Color(22,101,52); break;
            case "Boarding":
                background = new Color(219,234,254); foreground = new Color(30,64,175); break;
            case "Maintenance": case "Awaiting Gate": case "Pending":
                background = new Color(254,243,199); foreground = new Color(146,64,14); break;
            case "Cancelled": case "Inactive": case "Expired": case "No-show":
                background = new Color(254,226,226); foreground = new Color(185,28,28); break;
            case "Departed":
                background = new Color(243,244,246); foreground = new Color(75,85,99); break;
            default:
                background = new Color(243,244,246); foreground = new Color(75,85,99);
        }
        label.setBackground(background);
        label.setForeground(foreground);
        label.setBorder(BorderFactory.createEmptyBorder(4,12,4,12));

        if (row % 2 == 0)
            panel.setBackground(Color.WHITE);
        else
            panel.setBackground(new Color(249,249,249));

        panel.add(label);

        return panel;
    }

}
