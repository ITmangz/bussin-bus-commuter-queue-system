package qpal.view.Admin;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import qpal.dao.EmployeeSummaryDao;
import qpal.model.Account;
import qpal.util.UiTask;

/** Date-specific report from durable work records; includes employees with no work. */
final class DailyEmployeeSummaryDialog extends JDialog {
    private final JTextField date = new JTextField(LocalDate.now(ZoneId.of("Asia/Manila")).toString(), 10);
    private final JLabel status = new JLabel(" ");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Employee", "Email", "Payments", "Collected (PHP)", "Served", "Boarded"},0) {
        @Override public boolean isCellEditable(int row,int column) { return false; }
    };
    private long request;
    DailyEmployeeSummaryDialog(Component parent, Account account) {
        super(SwingUtilities.getWindowAncestor(parent),"Daily Employee Summary",Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(12,12));
        content.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Work date (YYYY-MM-DD, Manila):")); top.add(date);
        JButton load = new JButton("View / Refresh"); top.add(load);
        content.add(top,BorderLayout.NORTH);
        JTable table = new JTable(model); table.setRowHeight(32); table.setAutoCreateRowSorter(true);
        content.add(new JScrollPane(table),BorderLayout.CENTER);
        content.add(status,BorderLayout.SOUTH);
        Runnable refresh = () -> {
            final LocalDate day;
            try { day = LocalDate.parse(date.getText().trim()); }
            catch (java.time.format.DateTimeParseException ex) { status.setText("Enter a valid date as YYYY-MM-DD."); return; }
            long version = ++request;
            model.setRowCount(0); status.setText("Loading " + day + "...");
            UiTask.run(() -> new EmployeeSummaryDao().dailyEmployees(account,day), rows -> {
                if (version != request || !isDisplayable()) return;
                java.math.BigDecimal total = java.math.BigDecimal.ZERO;
                for (var row : rows) {
                    model.addRow(new Object[]{row.name(),row.email(),row.payments(),row.collected(),row.served(),row.boarded()});
                    total = total.add(row.collected());
                }
                status.setText(day + " | " + rows.size() + " employees | Total collected: PHP "
                        + total.toPlainString() + " | Includes employees with no recorded work.");
            }, ex -> { if (version == request) status.setText("Unable to load summary. Please retry."); });
        };
        load.addActionListener(e -> refresh.run()); date.addActionListener(e -> refresh.run());
        setContentPane(content); setSize(950,500); setLocationRelativeTo(parent);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) { refresh.run(); }
        });
    }
}
