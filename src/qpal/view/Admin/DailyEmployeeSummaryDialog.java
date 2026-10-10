package qpal.view.Admin;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import qpal.components.ModernTableCellRenderer;
import qpal.dao.EmployeeSummaryDao;
import qpal.model.Account;
import qpal.util.UiTask;

/** Date-specific report from durable work records; includes employees with no work. */
final class DailyEmployeeSummaryDialog extends JDialog {
    private final JDateChooser date = new JDateChooser();
    private final DefaultTableModel model =
            new DefaultTableModel(
                    new String[] {
                        "Staff Member",
                        "Email",
                        "Role",
                        "Payments",
                        "Amount Collected (PHP)",
                        "Counter Transactions",
                        "Passengers Boarded"
                    },
                    0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int column) {
                    return column < 3
                            ? String.class
                            : column == 4 ? java.math.BigDecimal.class : Integer.class;
                }
            };
    private long request;
    private final java.util.List<EmployeeSummaryDao.DailyEmployee> staff =
            new java.util.ArrayList<>();
    private final java.util.List<EmployeeSummaryDao.DailyEmployee> filtered =
            new java.util.ArrayList<>();
    private final JLabel info = new JLabel("Showing 0 to 0 of 0 staff members");
    private final JButton previous = pageButton("<");
    private final JButton first = pageButton("1");
    private final JButton second = pageButton("2");
    private final JButton next = pageButton(">");
    private final JTable table = new JTable(model);
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;

    private int pageCount() {
        return Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private void filter(String role) {
        filtered.clear();
        for (var row : staff)
            if ("All Roles".equals(role) || row.role().equals(role)) filtered.add(row);
        currentPage = 1;
        showPage();
    }

    private void showPage() {
        currentPage = Math.max(1, Math.min(currentPage, pageCount()));
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filtered.size());
        model.setRowCount(0);
        for (int i = start; i < end; i++) {
            var row = filtered.get(i);
            model.addRow(
                    new Object[] {
                        row.name(),
                        row.email(),
                        row.role(),
                        row.payments(),
                        row.collected(),
                        row.transactionsMade(),
                        row.boarded()
                    });
        }
        info.setText(
                "Showing "
                        + (filtered.isEmpty() ? 0 : start + 1)
                        + " to "
                        + end
                        + " of "
                        + filtered.size()
                        + " staff members");
        int pair = ((currentPage - 1) / 2) * 2 + 1;
        first.setText(String.valueOf(pair));
        second.setText(String.valueOf(pair + 1));
        second.setVisible(pair + 1 <= pageCount());
        previous.setEnabled(currentPage > 1);
        next.setEnabled(currentPage < pageCount());
        for (JButton button : new JButton[] {first, second}) {
            boolean selected = Integer.parseInt(button.getText()) == currentPage;
            button.setBackground(selected ? new Color(225, 29, 72) : Color.WHITE);
            button.setForeground(selected ? Color.WHITE : new Color(80, 80, 80));
        }
        table.clearSelection();
        table.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
    }

    private static JButton pageButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(34, 30));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(80, 80, 80));
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    DailyEmployeeSummaryDialog(Component parent, Account account) {
        super(
                SwingUtilities.getWindowAncestor(parent),
                "Daily Staff Summary",
                Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Daily Staff Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(228, 0, 70));
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        JLabel listTitle = new JLabel("Staff Activity");
        listTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        listTitle.setForeground(new Color(55, 55, 55));
        toolbar.add(listTitle, BorderLayout.WEST);
        JLabel subtitle =
                new JLabel(
                        "Completed counter transactions, collections, and passengers boarded by"
                            + " employees and admins.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 100, 100));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        date.setDateFormatString("yyyy-MM-dd");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Manila"));
        Calendar initial = Calendar.getInstance();
        initial.clear();
        initial.set(today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
        date.setCalendar(initial);
        // Selection-only input prevents invalid text from silently retaining a previous date.
        if (date.getDateEditor().getUiComponent() instanceof JTextField editor)
            editor.setEditable(false);
        date.getCalendarButton().getAccessibleContext().setAccessibleName("Choose work date");
        controls.add(date);
        JComboBox<String> roles = new JComboBox<>(new String[] {"All Roles", "Admin", "Employee"});
        AdminFormStyle.tableFilter(roles);
        roles.getAccessibleContext().setAccessibleName("Staff role");
        controls.add(roles);
        JButton load = button("View / Refresh", new Color(228, 0, 70), Color.WHITE);
        controls.add(load);
        toolbar.add(controls, BorderLayout.EAST);
        JButton close = button("Close", new Color(245, 245, 245), new Color(90, 90, 90));
        close.setPreferredSize(new Dimension(72, 30));
        close.addActionListener(e -> dispose());
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(title, BorderLayout.WEST);
        heading.add(close, BorderLayout.EAST);
        top.add(heading);
        top.add(Box.createVerticalStrut(8));
        top.add(Box.createVerticalStrut(10));
        top.add(subtitle);
        top.add(Box.createVerticalStrut(16));
        top.add(toolbar);
        for (Component child : top.getComponents())
            if (child instanceof JComponent c) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(top, BorderLayout.NORTH);

        table.setRowHeight(42);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        roles.addActionListener(e -> filter((String) roles.getSelectedItem()));
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(236, 236, 236));
        ModernTableCellRenderer renderer =
                new ModernTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t,
                            Object value,
                            boolean selected,
                            boolean focus,
                            int row,
                            int column) {
                        super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                        setHorizontalAlignment(column < 2 ? LEFT : column == 4 ? RIGHT : CENTER);
                        return this;
                    }
                };
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        table.setDefaultRenderer(java.math.BigDecimal.class, renderer);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
        int[] widths = {160, 140, 85, 75, 165, 155, 150};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(232, 236, 242)));
        scroll.getViewport().setBackground(Color.WHITE);
        AdminCard.styleScrollBar(scroll, Color.WHITE);
        content.add(scroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(12, 12));
        footer.setOpaque(false);
        JPanel navigation = new JPanel(new BorderLayout(12, 0));
        navigation.setOpaque(false);
        info.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info.setForeground(new Color(120, 120, 120));
        navigation.add(info, BorderLayout.WEST);
        JPanel pages = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pages.setOpaque(false);
        previous.addActionListener(
                e -> {
                    currentPage--;
                    showPage();
                });
        next.addActionListener(
                e -> {
                    currentPage++;
                    showPage();
                });
        first.addActionListener(
                e -> {
                    currentPage = Integer.parseInt(first.getText());
                    showPage();
                });
        second.addActionListener(
                e -> {
                    currentPage = Integer.parseInt(second.getText());
                    showPage();
                });
        pages.add(previous);
        pages.add(first);
        pages.add(second);
        pages.add(
                qpal.components.PagePicker.create(
                        () -> currentPage,
                        this::pageCount,
                        page -> {
                            currentPage = page;
                            showPage();
                        }));
        pages.add(next);
        navigation.add(pages, BorderLayout.EAST);
        footer.add(navigation, BorderLayout.NORTH);
        content.add(footer, BorderLayout.SOUTH);
        Runnable refresh =
                () -> {
                    Calendar selected = date.getCalendar();
                    if (selected == null) {
                        date.getCalendarButton().doClick();
                        return;
                    }
                    LocalDate day =
                            LocalDate.of(
                                    selected.get(Calendar.YEAR),
                                    selected.get(Calendar.MONTH) + 1,
                                    selected.get(Calendar.DAY_OF_MONTH));
                    long version = ++request;
                    staff.clear();
                    filter((String) roles.getSelectedItem());
                    load.setEnabled(false);
                    load.setText("Loading...");
                    UiTask.run(
                            () -> new EmployeeSummaryDao().dailyEmployees(account, day),
                            rows -> {
                                if (version != request || !isDisplayable()) return;
                                load.setEnabled(true);
                                load.setText("View / Refresh");
                                staff.addAll(rows);
                                filter((String) roles.getSelectedItem());
                            },
                            ex -> {
                                if (version != request || !isDisplayable()) return;
                                load.setEnabled(true);
                                load.setText("View / Refresh");
                                qpal.components.AppDialogs.showMessageDialog(
                                        this,
                                        "Unable to load summary. Please retry.",
                                        "Staff Summary",
                                        JOptionPane.ERROR_MESSAGE);
                            });
                };
        load.addActionListener(e -> refresh.run());
        content.setPreferredSize(new Dimension(1000, 540));
        setContentPane(AdminFormStyle.frame(content));
        // Form styling removes action borders; restore the compact pagination outlines.
        for (Component component : pages.getComponents())
            if (component instanceof JButton button) {
                button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                button.setBorderPainted(true);
            }
        // Fit the viewport when possible; preserve a horizontal scrollbar on smaller screens.
        scroll.getViewport()
                .addComponentListener(
                        new java.awt.event.ComponentAdapter() {
                            @Override
                            public void componentResized(java.awt.event.ComponentEvent e) {
                                int available = scroll.getViewport().getWidth();
                                int minimum = java.util.Arrays.stream(widths).sum();
                                int extra = Math.max(0, available - minimum);
                                for (int i = 0; i < widths.length; i++) {
                                    var column = table.getColumnModel().getColumn(i);
                                    column.setPreferredWidth(
                                            widths[i]
                                                    + extra / widths.length
                                                    + (i < extra % widths.length ? 1 : 0));
                                }
                            }
                        });
        // Keep the full date and calendar button visible in this compact filter row.
        Dimension dateSize = new Dimension(190, 38);
        date.setPreferredSize(dateSize);
        date.setMinimumSize(dateSize);
        date.setMaximumSize(dateSize);
        Dimension roleSize = new Dimension(140, 38);
        roles.setPreferredSize(roleSize);
        roles.setMinimumSize(roleSize);
        roles.setMaximumSize(roleSize);
        if (date.getDateEditor().getUiComponent() instanceof JTextField editor) {
            editor.setForeground(new Color(70, 70, 70));
            editor.setCaretPosition(0);
        }
        getRootPane()
                .registerKeyboardAction(
                        e -> dispose(),
                        KeyStroke.getKeyStroke("ESCAPE"),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        showPage();
        pack();
        Rectangle screen =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(getWidth(), screen.width - 32), Math.min(getHeight(), screen.height - 32));
        setLocationRelativeTo(null);
        addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowOpened(java.awt.event.WindowEvent e) {
                        refresh.run();
                    }
                });
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(144, 38));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
