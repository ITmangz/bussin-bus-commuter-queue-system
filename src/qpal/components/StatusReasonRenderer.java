package qpal.components;

import java.awt.Component;
import javax.swing.*;

/** Show stored staff text literally, with a wrapped tooltip for longer reasons. */
public final class StatusReasonRenderer extends ModernTableCellRenderer {
    public StatusReasonRenderer() {
        putClientProperty("html.disable", Boolean.TRUE);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean selected, boolean focused, int row, int column) {
        super.getTableCellRendererComponent(table, value, selected, focused, row, column);
        setHorizontalAlignment(SwingConstants.LEFT);
        String text = value == null ? "" : value.toString();
        setToolTipText("<html><body style='width:300px'>" + text.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                + "</body></html>");
        return this;
    }
}
