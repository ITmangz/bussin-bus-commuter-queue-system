package qpal.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class ActivityActionRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean selected, boolean focus, int row, int column) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(selected ? table.getSelectionBackground() : row % 2 == 0 ? Color.WHITE : new Color(249,249,249));
        String action = String.valueOf(value);
        JLabel label = new JLabel(action);
        label.setOpaque(true);
        label.setFont(new Font("SansSerif",Font.BOLD,11));
        label.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
        Color background = new Color(243,244,246);
        Color foreground = new Color(75,85,99);
        if (action.equals("Login") || action.equals("Update")) {
            background = new Color(219,234,254);
            foreground = new Color(30,90,175);
        } else if (action.equals("Create")) {
            background = new Color(220,245,233);
            foreground = new Color(20,125,85);
        } else if (action.equals("Delete")) {
            background = new Color(254,226,226);
            foreground = new Color(185,28,28);
        } else if (action.equals("Print") || action.equals("Export")) {
            background = new Color(237,225,255);
            foreground = new Color(110,55,175);
        }
        label.setBackground(background);
        label.setForeground(foreground);
        panel.add(label);
        return panel;
    }
}
