package qpal.view.Admin;

import qpal.components.ModernTableCellRenderer;
import qpal.components.StatusRenderer;
import qpal.components.TableActions;
import qpal.components.TableStyle;
import qpal.dao.AccountDao;
import qpal.model.Account;

import java.awt.*;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class AdminManageAccountsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private String appliedSearch = "";
    private final JLabel[] accountCounts = new JLabel[3];
    private JComboBox<String> cmbRole;
    private final List<Object[]> accounts = new ArrayList<>();
    private int currentPage = 1;
    private final int rowsPerPage = 10;
    private JLabel lblInfo;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnOne;
    private JButton btnTwo;
    private AccountDao accountDao = new AccountDao();

    public AdminManageAccountsPanel() {

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        add(createHeader(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(createAccountSummary(), BorderLayout.NORTH);
        body.add(createContent(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        loadAccounts();
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Accounts");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(228, 0, 70));

        JLabel subtitle =
                new JLabel("Manage administrator and employee accounts and access permissions.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 120, 120));

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(18));

        return panel;
    }

    private JPanel createAccountSummary() {
        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0));
        summary.setOpaque(false);
        summary.setPreferredSize(new Dimension(0, 112));
        String[] titles = {"TOTAL ACCOUNTS", "ACTIVE ACCOUNTS", "INACTIVE ACCOUNTS"};
        for (int i = 0; i < titles.length; i++) {
            JPanel card = new AdminCard(18);
            card.setLayout(new GridBagLayout());

            JLabel title = new JLabel(titles[i]);
            title.setFont(new Font("SansSerif", Font.PLAIN, 10));
            title.setForeground(new Color(100, 100, 100));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            JPanel content = new JPanel(new BorderLayout(0, 8));
            content.setOpaque(false);
            content.add(title, BorderLayout.NORTH);

            accountCounts[i] = new JLabel("—");
            accountCounts[i].setFont(new Font("SansSerif", Font.BOLD, 32));
            accountCounts[i].setForeground(new Color(170, 0, 45));
            accountCounts[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(accountCounts[i], BorderLayout.CENTER);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1;
            card.add(content, constraints);
            summary.add(card);
        }
        return summary;
    }

    private void updateAccountSummary() {
        int[] counts = {accounts.size(), 0, 0};
        for (Object[] account : accounts) {
            String status = String.valueOf(account[4]);
            if ("Active".equalsIgnoreCase(status)) counts[1]++;
            else if ("Inactive".equalsIgnoreCase(status)) counts[2]++;
        }
        for (int i = 0; i < counts.length; i++) {
            accountCounts[i].setText(String.valueOf(counts[i]));
        }
    }

    private JPanel createContent() {

        JPanel panel = new AdminCard(20);
        panel.setLayout(new BorderLayout());

        panel.add(createTopPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());

        center.setOpaque(false);
        center.setBorder(new EmptyBorder(18, 0, 0, 0));

        center.add(createTable(), BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());

        south.setOpaque(false);

        south.add(createBottomPanel(), BorderLayout.NORTH);

        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTopPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel("Account Details");

        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40, 40, 40));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));

        left.setOpaque(false);
        left.add(lblTitle);

        panel.add(left, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));

        controls.setOpaque(false);

        searchField = new JTextField();

        searchField.setPreferredSize(new Dimension(280, 34));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setMargin(new Insets(0, 10, 0, 10));

        cmbRole = new JComboBox<>(new String[] {"All Roles", "Admin", "Employee"});

        AdminFormStyle.tableFilter(cmbRole);
        cmbRole.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cmbRole.setFocusable(false);
        cmbRole.setBackground(Color.WHITE);
        cmbRole.addActionListener(
                e -> {
                    currentPage = 1;
                    loadPage();
                });

        JButton btnSearch = new JButton("Search");

        btnSearch.setPreferredSize(new Dimension(100, 34));
        btnSearch.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSearch.setBackground(new Color(225, 29, 72));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setBorder(BorderFactory.createEmptyBorder());

        btnSearch.addActionListener(
                e -> {
                    searchAccounts();
                });

        AdminFormStyle.searchOnEnter(searchField, btnSearch);

        controls.add(searchField);
        controls.add(cmbRole);
        controls.add(btnSearch);

        controls.add(TableStyle.button("Add", true, this::addAccount));
        controls.add(TableStyle.button("Print", false, this::printAccounts));

        TableStyle.toolbar(panel, left, controls);

        return panel;
    }

    private JScrollPane createTable() {

        String[] columns = {"ID", "Name", "Email", "Role", "Status", "Reason for Status Change"};

        model =
                new DefaultTableModel(columns, 0) {

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return column == 6;
                    }
                };

        model.addColumn("Actions");
        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(236, 236, 236));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowMargin(0);
        table.setSelectionBackground(new Color(240, 247, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer(qpal.components.StatusDescriptions::account));

        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 34));
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(90, 90, 90));
        header.setReorderingAllowed(false);

        table.getColumn("Reason for Status Change").setPreferredWidth(240);
        table.getColumn("Reason for Status Change").setCellRenderer(new qpal.components.StatusReasonRenderer());
        TableStyle.apply(table);
        TableActions.install(table, this::editAccount, this::deleteAccount);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        scroll.getViewport().setBackground(Color.WHITE);
        AdminCard.styleScrollBar(scroll, Color.WHITE);
        qpal.components.TableStyle.responsiveScroll(scroll);
        return scroll;
    }

    public void loadAccounts() {

        accounts.clear();

        List<Account> accountList = accountDao.getAllAccounts();

        for (Account account : accountList) {

            accounts.add(
                    new Object[] {
                        account.getID(),
                        account.getName(),
                        account.getEmail(),
                        account.getRole(),
                        account.getStatus(),
                        qpal.model.StatusReason.display(account.getStatus(), account.getStatusReason())
                    });
        }

        // The DAO returns ascending auto-increment IDs; reverse to show newest accounts first.
        java.util.Collections.reverse(accounts);

        updateAccountSummary();
        currentPage = 1;

        if (model != null) {
            loadPage();
        }
    }

    private void searchAccounts() {
        if (!AdminFormStyle.validateSearch(searchField)) return;

        appliedSearch = searchField.getText().trim().toLowerCase();
        currentPage = 1;
        loadPage();
    }

    private List<Object[]> getVisibleRows() {

        List<Object[]> result = new ArrayList<>();
        String search = appliedSearch;

        for (Object[] row : accounts) {

            if ((row[0].toString().toLowerCase().contains(search)
                            || row[1].toString().toLowerCase().contains(search)
                            || row[2].toString().toLowerCase().contains(search))
                    && (cmbRole.getSelectedItem().toString().equals("All Roles")
                            || row[3].toString()
                                    .equalsIgnoreCase(cmbRole.getSelectedItem().toString()))) {

                result.add(row);
            }
        }

        return result;
    }

    private void loadPage() {

        List<Object[]> visibleRows = getVisibleRows();
        currentPage =
                Math.max(
                        1,
                        Math.min(
                                currentPage,
                                Math.max(1, (visibleRows.size() + rowsPerPage - 1) / rowsPerPage)));

        model.setRowCount(0);

        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, visibleRows.size());

        for (int i = start; i < end; i++) {
            model.addRow(visibleRows.get(i));
        }

        if (lblInfo != null) {

            lblInfo.setText(
                    "Showing "
                            + (visibleRows.size() == 0 ? 0 : start + 1)
                            + " to "
                            + end
                            + " of "
                            + visibleRows.size()
                            + " accounts");
        }

        updatePaginationButtons();
    }

    private void updatePaginationButtons() {

        int totalPages = (int) Math.ceil(getVisibleRows().size() / (double) rowsPerPage);
        int firstPage = Math.max(1, Math.min(currentPage, totalPages - 1));
        btnOne.setText(String.valueOf(firstPage));
        btnTwo.setText(String.valueOf(firstPage + 1));

        if (btnPrev != null) btnPrev.setEnabled(currentPage > 1);

        if (btnNext != null) btnNext.setEnabled(currentPage < totalPages);

        if (btnOne != null) {

            btnOne.setBackground(currentPage == firstPage ? new Color(225, 29, 72) : Color.WHITE);

            btnOne.setForeground(currentPage == firstPage ? Color.WHITE : new Color(80, 80, 80));
        }

        if (btnTwo != null) {

            btnTwo.setVisible(totalPages >= 2);

            btnTwo.setBackground(
                    currentPage == firstPage + 1 ? new Color(225, 29, 72) : Color.WHITE);

            btnTwo.setForeground(
                    currentPage == firstPage + 1 ? Color.WHITE : new Color(80, 80, 80));
        }
    }

    private JPanel createBottomPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18, 0, 0, 0));
        lblInfo = new JLabel();
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(130, 130, 130));
        panel.add(lblInfo, BorderLayout.WEST);

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pagination.setOpaque(false);

        btnPrev = new JButton("<");
        btnOne = new JButton("1");
        btnTwo = new JButton("2");
        btnNext = new JButton(">");

        JButton[] buttons = {btnPrev, btnOne, btnTwo, btnNext};

        for (JButton b : buttons) {

            b.setPreferredSize(new Dimension(34, 30));
            b.setFont(new Font("SansSerif", Font.BOLD, 14));
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setBackground(Color.WHITE);
            b.setForeground(new Color(80, 80, 80));
            b.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            pagination.add(b);
        }

        btnPrev.addActionListener(
                e -> {
                    if (currentPage > 1) {

                        currentPage--;
                        loadPage();
                    }
                });

        btnNext.addActionListener(
                e -> {
                    int totalPages =
                            (int) Math.ceil(getVisibleRows().size() / (double) rowsPerPage);

                    if (currentPage < totalPages) {

                        currentPage++;
                        loadPage();
                    }
                });

        btnOne.addActionListener(
                e -> {
                    currentPage = Integer.parseInt(btnOne.getText());
                    loadPage();
                });

        btnTwo.addActionListener(
                e -> {
                    currentPage = Integer.parseInt(btnTwo.getText());
                    loadPage();
                });

        pagination.add(
                qpal.components.PagePicker.create(
                        () -> currentPage,
                        () -> (getVisibleRows().size() + rowsPerPage - 1) / rowsPerPage,
                        page -> {
                            currentPage = page;
                            loadPage();
                        }),
                pagination.getComponentCount() - 1);
        panel.add(pagination, BorderLayout.EAST);

        SwingUtilities.invokeLater(this::loadPage);

        return panel;
    }

    private void addAccount() {

        AddAccountPanel panel = new AddAccountPanel(accountDao, this);
        panel.showDialog();
    }

    private void editAccount(int selectedRow) {

        int id = (int) model.getValueAt(selectedRow, 0);

        EditAccountPanel panel = new EditAccountPanel(accountDao, this, id);
        panel.showDialog();
    }

    private void deleteAccount(int selectedRow) {

        int id = (int) model.getValueAt(selectedRow, 0);

        String name = model.getValueAt(selectedRow, 1).toString();

        int confirm =
                qpal.components.AppDialogs.showConfirmDialog(
                        this,
                        "Are you sure you want to delete " + name + "?",
                        "Delete Account",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {

            boolean success = accountDao.deleteAccount(id);

            if (success) {

                qpal.components.AppDialogs.showMessageDialog(
                        this,
                        "Account deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAccounts();

            } else {

                qpal.components.AppDialogs.showMessageDialog(
                        this, "Failed to delete account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printAccounts() {

        try {

            boolean complete = TableStyle.print(table);

            if (complete) {
                qpal.dao.ActivityLogDao.recordActivity(
                        "Manage Accounts", "Print", "Printed the manage accounts table.");

                qpal.components.AppDialogs.showMessageDialog(
                        this,
                        "Table printed successfully.",
                        "Print",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {

                qpal.components.AppDialogs.showMessageDialog(
                        this, "Printing was cancelled.", "Print", JOptionPane.WARNING_MESSAGE);
            }

        } catch (PrinterException e) {

            qpal.components.AppDialogs.showMessageDialog(
                    this, "Unable to print the table.", "Print Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
