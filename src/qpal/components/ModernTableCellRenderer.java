package qpal.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

public class ModernTableCellRenderer extends DefaultTableCellRenderer {

    public ModernTableCellRenderer() {
        setOpaque(true);
        setBorder(new EmptyBorder(0, 12, 0, 12));
        setHorizontalAlignment(LEFT);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setFont(new Font("SansSerif", Font.PLAIN, 13));

        if (isSelected) {

            setBackground(new Color(235, 245, 255));
            setForeground(Color.BLACK);

        } else {

            // Zebra stripes
            if (row % 2 == 0) {
                setBackground(Color.WHITE);
            } else {
                setBackground(new Color(249, 249, 249));
            }

            setForeground(new Color(70, 70, 70));
        }

        // Center ID, Role, Status
        if (column == 0 || column == 4 || column == 5) {
            setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        return this;
    }
}
