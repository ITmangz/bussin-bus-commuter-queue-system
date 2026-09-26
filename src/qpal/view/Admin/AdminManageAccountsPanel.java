package qpal.view.Admin;

import java.awt.*;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import qpal.components.ModernTableCellRenderer;
import qpal.components.StatusRenderer;
import qpal.dao.AccountDao;
import qpal.model.Account;

public class AdminManageAccountsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private String appliedSearch = "";
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
        setBackground(new Color(245,245,245));
        setBorder(new EmptyBorder(20,25,20,25));
        
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadAccounts();
    }


    private JPanel createHeader() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Manage Accounts");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(228,0,70));

        JLabel subtitle = new JLabel("Manage admin accounts and access permissions.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120,120,120));

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(18));

        return panel;
    }


    private JPanel createContent() {

        JPanel panel = new AdminCard(20);
        panel.setLayout(new BorderLayout());

        panel.add(createTopPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());

        center.setOpaque(false);
        center.setBorder(new EmptyBorder(18,0,0,0));

        center.add(createTable(), BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());

        south.setOpaque(false);

        south.add(createBottomPanel(), BorderLayout.NORTH);
        south.add(createActionButtons(), BorderLayout.SOUTH);

        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createTopPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel("Account Details");

        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(40,40,40));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,6));

        left.setOpaque(false);
        left.add(lblTitle);

        panel.add(left, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4));

        controls.setOpaque(false);

        searchField = new JTextField();

        searchField.setPreferredSize(new Dimension(280,34));
        searchField.setFont(new Font("SansSerif",Font.PLAIN,13));
        searchField.setMargin(new Insets(0,10,0,10));

        cmbRole = new JComboBox<>(new String[]{
                "All Roles", "Admin", "Employee"
        });

        cmbRole.setPreferredSize(new Dimension(140,34));
        cmbRole.setFont(new Font("SansSerif",Font.PLAIN,13));
        cmbRole.setFocusable(false);
        cmbRole.setBackground(Color.WHITE);
        cmbRole.addActionListener(e -> {

            currentPage = 1;
            loadPage();
        });

        JButton btnSearch = new JButton("Search");

        btnSearch.setPreferredSize(new Dimension(100,34));
        btnSearch.setFont(new Font("SansSerif",Font.BOLD,13));
        btnSearch.setBackground(new Color(225,29,72));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setBorder(BorderFactory.createEmptyBorder());

        btnSearch.addActionListener(e -> {
            searchAccounts();
        });

    controls.add(searchField);
        controls.add(cmbRole);
        controls.add(btnSearch);

        panel.add(controls, BorderLayout.EAST);

        return panel;
    }


    private JScrollPane createTable() {

        String[] columns = {
                "ID", "Name", "Email", "Password", "Role", "Status"
        };

        model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(236,236,236));
        table.setIntercellSpacing(new Dimension(0,0));
        table.setRowMargin(0);
        table.setSelectionBackground(new Color(240,247,255));
        table.setSelectionForeground(Color.BLACK);
        table.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0,34));
        header.setFont(new Font("SansSerif",Font.BOLD,12));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(90,90,90));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        return scroll;
    }


    public void loadAccounts() {

        accounts.clear();

        List<Account> accountList = accountDao.getAllAccounts();

        for(Account account : accountList) {

            String password = "********************************";

            accounts.add(new Object[]{
                    account.getID(),
                    account.getName(),
                    account.getEmail(),
                    password,
                    account.getRole(),
                    account.getStatus()
            });
        }

        currentPage = 1;

        if(model != null) {
            loadPage();
        }
    }


    private void searchAccounts() {

        appliedSearch = searchField.getText().trim().toLowerCase();
        currentPage = 1;
        loadPage();
    }

    private List<Object[]> getVisibleRows() {

        List<Object[]> result = new ArrayList<>();
        String search = appliedSearch;

        for(Object[] row : accounts) {

            if((row[0].toString().toLowerCase().contains(search) || row[1].toString().toLowerCase().contains(search) || row[2].toString().toLowerCase().contains(search)) && (cmbRole.getSelectedItem().toString().equals("All Roles") || row[4].toString().equalsIgnoreCase(cmbRole.getSelectedItem().toString()))) {

                result.add(row);
            }
        }

        return result;
    }

    private void loadPage() {

        List<Object[]> visibleRows = getVisibleRows();

        model.setRowCount(0);

        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage, visibleRows.size());

        for(int i = start; i < end; i++){
            model.addRow(visibleRows.get(i));
        }

        if(lblInfo != null){

            lblInfo.setText("Showing " + (visibleRows.size() == 0 ? 0 : start + 1) + " to " + end + " of " + visibleRows.size() + " accounts");
        }

        updatePaginationButtons();
    }


    private void updatePaginationButtons(){

        int totalPages =
                (int)Math.ceil(getVisibleRows().size() / (double)rowsPerPage);

        if(btnPrev != null)
            btnPrev.setEnabled(currentPage > 1);

        if(btnNext != null)
            btnNext.setEnabled(currentPage < totalPages);

        if(btnOne != null){

            btnOne.setBackground(
                    currentPage == 1 ?
                    new Color(225,29,72) :
                    Color.WHITE);

            btnOne.setForeground(
                    currentPage == 1 ?
                    Color.WHITE :
                    new Color(80,80,80));
        }

        if(btnTwo != null){

            btnTwo.setVisible(totalPages >= 2);

            btnTwo.setBackground(
                    currentPage == 2 ?
                    new Color(225,29,72) :
                    Color.WHITE);

            btnTwo.setForeground(
                    currentPage == 2 ?
                    Color.WHITE :
                    new Color(80,80,80));
        }
    }


    private JPanel createBottomPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18,0,0,0));
        lblInfo = new JLabel();
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(130,130,130));
        panel.add(lblInfo, BorderLayout.WEST);

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));
        pagination.setOpaque(false);

        btnPrev = new JButton("<");
        btnOne = new JButton("1");
        btnTwo = new JButton("2");
        btnNext = new JButton(">");

        JButton[] buttons = {
                btnPrev, btnOne, btnTwo, btnNext
        };

        for (JButton b : buttons) {

            b.setPreferredSize(new Dimension(34,30));
            b.setFont(new Font("SansSerif", Font.BOLD,14));
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setBackground(Color.WHITE);
            b.setForeground(new Color(80,80,80));
            b.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
            pagination.add(b);
        }

        btnPrev.addActionListener(e -> {

            if(currentPage > 1){

                currentPage--;
                loadPage();
            }
        });

        btnNext.addActionListener(e -> {

            int totalPages = (int)Math.ceil(getVisibleRows().size() / (double)rowsPerPage);

            if(currentPage < totalPages){

                currentPage++;
                loadPage();
            }
        });

        btnOne.addActionListener(e -> {

            currentPage = 1;
            loadPage();
        });

        btnTwo.addActionListener(e -> {

            currentPage = 2;
            loadPage();
        });

        panel.add(pagination, BorderLayout.EAST);

        SwingUtilities.invokeLater(this::loadPage);

        return panel;
    }


    private JPanel createActionButtons() {

        JPanel panel = new JPanel(new GridLayout(1,4,12,0));

        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20,0,0,0));

        JButton btnAdd = createButton("Add", new Color(34,197,94));
        JButton btnEdit = createButton("Edit", new Color(245,158,11));
        JButton btnPrint = createButton("Print", new Color(59,130,246));
        JButton btnDelete = createButton("Delete", new Color(225,29,72));

        btnAdd.addActionListener(e -> {
            addAccount();
        });

        btnEdit.addActionListener(e -> {
            editAccount();
        });

        btnPrint.addActionListener(e -> {
            printAccounts();
        });

        btnDelete.addActionListener(e -> {
            deleteAccount();
        });

        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnPrint);
        panel.add(btnDelete);

        return panel;
    }


    private void addAccount() {

        AddAccountPanel panel =new AddAccountPanel(accountDao, this);
        panel.showDialog();
    }


    private void editAccount() {

        int selectedRow = table.getSelectedRow();

        if(selectedRow == -1) {

            qpal.components.AppDialogs.showMessageDialog(this,"No selected account to edit.","Warning",JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id =
                (int) model.getValueAt(selectedRow,0);

        EditAccountPanel panel = new EditAccountPanel(accountDao, this, id);
        panel.showDialog();
    }


    private void deleteAccount() {

        int selectedRow = table.getSelectedRow();

        if(selectedRow == -1) {

            qpal.components.AppDialogs.showMessageDialog(this,"No selected account.","Warning",JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) model.getValueAt(selectedRow,0);

        String name = model.getValueAt(selectedRow,1).toString();

        int confirm = qpal.components.AppDialogs.showConfirmDialog(this, "Are you sure you want to delete " + name + "?","Delete Account", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if(confirm == JOptionPane.YES_OPTION) {

            boolean success = accountDao.deleteAccount(id);

            if(success) {

                qpal.components.AppDialogs.showMessageDialog(this,"Account deleted successfully.","Success",JOptionPane.INFORMATION_MESSAGE);
                loadAccounts();

            } else {

                qpal.components.AppDialogs.showMessageDialog(this,"Failed to delete account.","Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void printAccounts() {

        try {

            boolean complete = table.print();

            if(complete) {

                qpal.components.AppDialogs.showMessageDialog(this,"Table printed successfully.","Print", JOptionPane.INFORMATION_MESSAGE);

            } else {

                qpal.components.AppDialogs.showMessageDialog(this,"Printing was cancelled.","Print", JOptionPane.WARNING_MESSAGE);
            }

        } catch(PrinterException e) {

            qpal.components.AppDialogs.showMessageDialog(this,"Unable to print the table.","Print Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private JButton createButton(String text, Color color) {

        JButton button =new JButton(text);
        button.setPreferredSize(new Dimension(145,40));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif",Font.BOLD,13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}