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

        JLabel label = new JLabel(value.toString());

        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setOpaque(true);

        if ("Active".equals(value.toString())) {

            label.setBackground(new Color(220,252,231));
            label.setForeground(new Color(22,101,52));

        } else {

            label.setBackground(new Color(254,226,226));
            label.setForeground(new Color(185,28,28));

        }

        label.setBorder(BorderFactory.createEmptyBorder(4,12,4,12));

        if (row % 2 == 0)
            panel.setBackground(Color.WHITE);
        else
            panel.setBackground(new Color(249,249,249));

        panel.add(label);

        return panel;
    }

}