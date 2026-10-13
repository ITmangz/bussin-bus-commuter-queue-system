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

        setFont(table.getFont());
        setBorder(new EmptyBorder(0, 12, 0, 12));
        setToolTipText(value == null ? null : value.toString());

        if (isSelected) {

            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());

        } else {

            // Zebra stripes
            if (row % 2 == 0) {
                setBackground(Color.WHITE);
            } else {
                setBackground(new Color(249, 249, 249));
            }

            setForeground(new Color(70, 70, 70));
        }

        String heading = table.getColumnName(column);
        if (heading.equals("ID")
                || heading.endsWith(" ID")
                || heading.equals("#")
                || heading.equals("Role")
                || heading.contains("Status")) {
            setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        return this;
    }
}
