package qpal.components;

import java.awt.*;
import java.awt.print.PrinterException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

/** Common table, toolbar and window sizing for the staff application. */
public final class TableStyle {
    public static final Color PRIMARY = new Color(225, 29, 72);

    private TableStyle() {}

    public static void apply(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(42);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(237, 239, 243));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(255, 235, 241));
        table.setSelectionForeground(new Color(40, 45, 55));
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(247, 248, 250));
        header.setForeground(new Color(85, 93, 107));
        header.setReorderingAllowed(false);
        DefaultTableCellRenderer heading =
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable owner,
                            Object value,
                            boolean selected,
                            boolean focus,
                            int row,
                            int column) {
                        super.getTableCellRendererComponent(
                                owner, value, false, false, row, column);
                        setFont(owner.getTableHeader().getFont());
                        String title = String.valueOf(value);
                        setHorizontalAlignment(
                                title.equals("ID")
                                                || title.endsWith(" ID")
                                                || title.equals("#")
                                                || title.equals("Role")
                                                || title.contains("Status")
                                                || title.equals("Actions")
                                        ? SwingConstants.CENTER
                                        : SwingConstants.LEFT);
                        setBorder(
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(
                                                0, 0, 1, 0, new Color(226, 230, 237)),
                                        new EmptyBorder(0, 12, 0, 12)));
                        return this;
                    }
                };
        heading.setHorizontalAlignment(SwingConstants.LEFT);
        heading.setFont(header.getFont());
        heading.setBackground(header.getBackground());
        heading.setForeground(header.getForeground());
        header.setDefaultRenderer(heading);
    }

    public static JButton button(String title, boolean primary, Runnable action) {
        JButton button = new JButton(title);
        styleButton(button, primary);
        button.addActionListener(e -> action.run());
        return button;
    }

    public static void styleButton(JButton button, boolean primary) {
        button.setUI(
                new javax.swing.plaf.basic.BasicButtonUI() {
                    @Override
                    public void paint(Graphics graphics, JComponent component) {
                        AbstractButton b = (AbstractButton) component;
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color color = b.getBackground();
                        if (!b.isEnabled()) color = new Color(232, 235, 240);
                        else if (b.getModel().isPressed()) color = color.darker();
                        else if (b.getModel().isRollover()) color = color.darker();
                        g.setColor(color);
                        g.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 4, 4);
                        if (b.hasFocus()) {
                            g.setColor(b.getForeground());
                            g.drawRoundRect(3, 3, b.getWidth() - 7, b.getHeight() - 7, 3, 3);
                        }
                        g.dispose();
                        super.paint(graphics, component);
                    }

                    @Override
                    protected void paintButtonPressed(Graphics g, AbstractButton b) {}
                });
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(true);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setPreferredSize(
                new Dimension(
                        Math.max(
                                72,
                                button.getFontMetrics(button.getFont())
                                                .stringWidth(button.getText())
                                        + 24),
                        36));
        Color background = primary ? PRIMARY : new Color(237, 240, 245);
        Color foreground = primary ? Color.WHITE : new Color(65, 73, 87);
        switch (button.getText()) {
            case "Add", "Add Passenger" -> {
                background = new Color(22, 163, 74);
                foreground = Color.WHITE;
            }
            case "Print" -> {
                background = new Color(59, 130, 246);
                foreground = Color.WHITE;
            }
            case "Delete" -> {
                background = PRIMARY;
                foreground = Color.WHITE;
            }
            case "Edit" -> {
                background = new Color(250, 204, 21);
                foreground = Color.WHITE;
            }
        }
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 10, 6, 10));
    }

    /** One aligned title/control row with a compact, bounded search field. */
    public static void toolbar(JPanel panel, Component title, JPanel controls) {
        panel.removeAll();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.gridx = 0;
        titleConstraints.weightx = 1;
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        titleConstraints.insets = new Insets(0, 0, 0, 12);
        panel.add(title, titleConstraints);
        Component[] items = controls.getComponents();
        controls.removeAll();
        controls.setLayout(new GridBagLayout());
        for (int i = 0; i < items.length; i++) {
            Component item = items[i];
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = item instanceof JTextField ? 1 : 0;
            c.insets = new Insets(0, i == 0 ? 0 : 8, 0, 0);
            if (item instanceof JTextField field) {
                field.setPreferredSize(new Dimension(150, 36));
                field.setMinimumSize(new Dimension(90, 36));
                field.getAccessibleContext().setAccessibleName("Search table");
                field.setToolTipText("Enter a search term, then press Enter or Search");
            } else if (item instanceof JButton button) {
                styleButton(
                        button,
                        "Search".equals(button.getText()) || "Add".equals(button.getText()));
            } else if (item instanceof JComboBox<?>) {
                item.setPreferredSize(new Dimension(130, 36));
                item.setMinimumSize(new Dimension(130, 36));
            }
            controls.add(item, c);
        }
        GridBagConstraints controlConstraints = new GridBagConstraints();
        controlConstraints.gridx = 1;
        controlConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(controls, controlConstraints);
    }

    /** Keep columns readable; use horizontal scrolling only when the viewport is too narrow. */
    public static void responsiveScroll(JScrollPane scroll) {
        JTable table = (JTable) scroll.getViewport().getView();
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            if ("Actions".equals(col.getHeaderValue())) continue;
            int labelWidth =
                    table.getTableHeader()
                                    .getFontMetrics(table.getTableHeader().getFont())
                                    .stringWidth(String.valueOf(col.getHeaderValue()))
                            + 28;
            col.setMinWidth(Math.max(64, Math.min(160, labelWidth)));
        }
        Runnable resize =
                () -> {
                    int minimum = 0;
                    for (int i = 0; i < table.getColumnCount(); i++)
                        minimum += table.getColumnModel().getColumn(i).getMinWidth();
                    int mode =
                            scroll.getViewport().getWidth() < minimum
                                    ? JTable.AUTO_RESIZE_OFF
                                    : JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS;
                    if (table.getAutoResizeMode() != mode) table.setAutoResizeMode(mode);
                };
        scroll.getViewport()
                .addComponentListener(
                        new java.awt.event.ComponentAdapter() {
                            @Override
                            public void componentResized(java.awt.event.ComponentEvent e) {
                                resize.run();
                            }
                        });
        resize.run();
    }

    /** Copy displayed data so printing never includes interactive action cells. */
    public static JTable printSnapshot(JTable source) {
        DefaultTableModel data = new DefaultTableModel();
        for (int col = 0; col < source.getColumnCount(); col++)
            if (!"Actions".equals(source.getColumnName(col)))
                data.addColumn(source.getColumnName(col));
        for (int row = 0; row < source.getRowCount(); row++) {
            Object[] values = new Object[data.getColumnCount()];
            int target = 0;
            for (int col = 0; col < source.getColumnCount(); col++)
                if (!"Actions".equals(source.getColumnName(col)))
                    values[target++] = source.getValueAt(row, col);
            data.addRow(values);
        }
        JTable print = new JTable(data);
        apply(print);
        print.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        int target = 0;
        for (int col = 0; col < source.getColumnCount(); col++) {
            if ("Actions".equals(source.getColumnName(col))) continue;
            print.getColumnModel()
                    .getColumn(target++)
                    .setPreferredWidth(source.getColumnModel().getColumn(col).getWidth());
        }
        int width = 0;
        for (int col = 0; col < print.getColumnCount(); col++)
            width += print.getColumnModel().getColumn(col).getPreferredWidth();
        print.setSize(
                Math.max(1, width),
                Math.max(print.getRowHeight(), print.getRowCount() * print.getRowHeight()));
        print.doLayout();
        print.getTableHeader().setSize(print.getWidth(), 40);
        return print;
    }

    public static boolean print(JTable table) throws PrinterException {
        return printSnapshot(table).print(JTable.PrintMode.FIT_WIDTH);
    }

    public static void sizeWindow(JFrame frame) {
        Rectangle screen =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        frame.setResizable(true);
        frame.setMinimumSize(
                new Dimension(Math.min(1080, screen.width), Math.min(680, screen.height)));
        frame.setSize(Math.min(1440, screen.width), Math.min(940, screen.height));
        frame.setLocationRelativeTo(null);
    }
}
