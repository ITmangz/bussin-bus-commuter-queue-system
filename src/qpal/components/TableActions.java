package qpal.components;

import java.awt.*;
import java.util.function.IntConsumer;

import javax.swing.*;
import javax.swing.table.*;

/** Real keyboard-accessible buttons; callbacks receive the selected model row. */
public final class TableActions extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor {
    private final JPanel display = panel();
    private final JPanel editor = panel();
    private int modelRow = -1;

    private static JPanel panel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 5));
        return panel;
    }

    private TableActions(JTable table, IntConsumer edit, IntConsumer delete) {
        addButtons(display, edit == null ? null : () -> {}, () -> {});
        addButtons(editor, edit == null ? null : () -> invoke(edit), () -> invoke(delete));
        table.getModel()
                .addTableModelListener(
                        e -> {
                            if (table.isEditing()) table.getCellEditor().cancelCellEditing();
                        });
    }

    private void invoke(IntConsumer action) {
        int row = modelRow;
        modelRow = -1;
        fireEditingCanceled();
        if (row >= 0) action.accept(row);
    }

    @Override
    public void cancelCellEditing() {
        modelRow = -1;
        super.cancelCellEditing();
    }

    private void addButtons(JPanel panel, Runnable edit, Runnable delete) {
        JButton editButton =
                TableStyle.button(
                        "Edit",
                        false,
                        () -> {
                            if (edit != null) edit.run();
                        });
        JButton deleteButton =
                TableStyle.button(
                        "Delete",
                        false,
                        () -> {
                            if (delete != null) delete.run();
                        });
        for (JButton button : new JButton[] {editButton, deleteButton}) {
            button.setPreferredSize(new Dimension(64, 30));
            button.setToolTipText(button.getText() + " this record");
            if (button != editButton || edit != null) panel.add(button);
        }
    }

    public static void install(JTable table, IntConsumer edit, IntConsumer delete) {
        TableColumn column = table.getColumn("Actions");
        TableActions actions = new TableActions(table, edit, delete);
        column.setCellRenderer(actions);
        column.setCellEditor(actions);
        int width = edit == null ? 88 : 150;
        column.setMinWidth(width);
        column.setMaxWidth(width);
        column.setPreferredWidth(width);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean selected, boolean focus, int row, int column) {
        display.setBackground(
                selected
                        ? table.getSelectionBackground()
                        : row % 2 == 0 ? Color.WHITE : new Color(249, 249, 249));
        return display;
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean selected, int row, int column) {
        modelRow = table.convertRowIndexToModel(row);
        editor.setBackground(table.getSelectionBackground());
        return editor;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }
}
