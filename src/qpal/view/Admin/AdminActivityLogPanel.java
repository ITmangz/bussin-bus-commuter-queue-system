package qpal.view.Admin;

import java.awt.*;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import qpal.components.ModernTableCellRenderer;
import qpal.components.ActivityActionRenderer;
import qpal.dao.ActivityLogDao;
import qpal.model.Account;
import qpal.model.ActivityLog;

public class AdminActivityLogPanel extends JPanel {
    private Account account;
    private ActivityLogDao activityLogDao = new ActivityLogDao();
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> cmbAction;
    private JLabel lblInfo;
    private JLabel connectionStatus;
    private JButton btnOne;
    private JButton btnTwo;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnPrint;
    private boolean loading;
    private boolean editing;
    private String appliedSearch = "";
    private final List<Object[]> activities = new ArrayList<>();
    private final List<Object[]> filteredActivities = new ArrayList<>();
    private int currentPage = 1;
    private final int rowsPerPage = 10;

    public AdminActivityLogPanel(Account account) {
        this.account = account;
        setLayout(new BorderLayout(0,16));
        setBackground(new Color(245,245,245));
        setBorder(new EmptyBorder(20,25,20,25));
        add(createHeader(),BorderLayout.NORTH);
        add(createContent(),BorderLayout.CENTER);
        searchActivities();
        Timer timer = new Timer(5000,e -> refreshData());
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) { timer.start(); refreshData(); }
                else timer.stop();
            }
        });
    }

    public void refreshData() {
        if (loading || editing) return;
        loading = true;
        qpal.util.UiTask.run(() -> activityLogDao.getAllActivities(account), data -> {
            activities.clear();
            java.time.format.DateTimeFormatter format = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (ActivityLog activity : data) {
                activities.add(new Object[]{activity.getId(), activity.getTimestamp().toLocalDateTime().format(format),
                        activity.getEmail(), activity.getRole(), activity.getModule(), activity.getAction(), activity.getDescription()});
            }
            int page = currentPage;
            searchActivities();
            currentPage = page;
            loadPage();
            connectionStatus.setText(activities.isEmpty() ? "No activity records yet." : "");
            if (ActivityLogDao.hasSaveFailed()) connectionStatus.setText("An activity could not be saved. Check the database connection.");
            connectionStatus.setVisible(!connectionStatus.getText().isEmpty());
            loading = false;
        }, ex -> {
            loading = false;
            connectionStatus.setText("Unable to refresh activity logs. Check the database connection. Retrying in 5 seconds.");
            connectionStatus.setVisible(true);
        });
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Activity Log");
        title.setFont(new Font("SansSerif",Font.BOLD,30));
        title.setForeground(new Color(228,0,70));
        JLabel subtitle = new JLabel("View recorded user actions and system activity.");
        subtitle.setFont(new Font("SansSerif",Font.PLAIN,13));
        subtitle.setForeground(new Color(120,120,120));
        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        return panel;
    }

    private JPanel createContent() {
        JPanel panel = AdminDashboardPanel.card();
        panel.setLayout(new BorderLayout(0,12));
        panel.add(createTopPanel(),BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(createTable(),BorderLayout.CENTER);
        connectionStatus = new JLabel("Loading activity logs...");
        connectionStatus.setFont(new Font("SansSerif",Font.PLAIN,12));
        connectionStatus.setForeground(new Color(120,120,120));
        connectionStatus.setBorder(new EmptyBorder(12,0,8,0));
        center.add(connectionStatus,BorderLayout.SOUTH);
        panel.add(center,BorderLayout.CENTER);
        panel.add(createBottomPanel(),BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel("Activity List");
        title.setFont(new Font("SansSerif",Font.BOLD,22));
        panel.add(title,BorderLayout.WEST);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        controls.setOpaque(false);
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(190,34));
        searchField.setFont(new Font("SansSerif",Font.PLAIN,13));
        searchField.setMargin(new Insets(0,10,0,10));
        searchField.setToolTipText("Search timestamp, email, role, module or description");
        cmbAction = new JComboBox<>(new String[]{"All Actions","Login","Logout","Create","Update","Delete","Print","Export"});
        AdminFormStyle.tableFilter(cmbAction);
        cmbAction.setFont(new Font("SansSerif",Font.PLAIN,13));
        cmbAction.setBackground(Color.WHITE);
        cmbAction.setFocusable(false);
        cmbAction.addActionListener(e -> searchActivities());
        JButton btnSearch = createButton("Search",new Color(225,29,72));
        btnSearch.addActionListener(e -> {
            if (AdminFormStyle.validateSearch(searchField)) {
                appliedSearch = searchField.getText().trim();
                searchActivities();
            }
        });
        AdminFormStyle.searchOnEnter(searchField,btnSearch);
        btnPrint = createButton("Print",new Color(59,130,246));
        btnPrint.setToolTipText("Print the current page of activity records");
        btnPrint.addActionListener(e -> printActivities());
        controls.add(searchField);
        controls.add(cmbAction);
        controls.add(btnSearch);
        controls.add(btnPrint);
        panel.add(controls,BorderLayout.EAST);
        return panel;
    }

    private JScrollPane createTable() {
        String[] columns = {"#","Timestamp","Email","Role","Module","Action","Description"};
        model = new DefaultTableModel(columns,0) {
            public boolean isCellEditable(int row,int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("SansSerif",Font.PLAIN,12));
        table.setRowHeight(42);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(236,236,236));
        table.setSelectionBackground(new Color(240,247,255));
        table.setDefaultRenderer(Object.class,new ModernTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int column) {
                super.getTableCellRendererComponent(table,value,selected,focus,row,column);
                setHorizontalAlignment(column == 0 ? CENTER : LEFT);
                setFont(new Font("SansSerif",Font.PLAIN,12));
                setToolTipText(value == null ? null : value.toString());
                return this;
            }
        });
        table.getColumnModel().getColumn(5).setCellRenderer(new ActivityActionRenderer());
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif",Font.BOLD,12));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0,34));
        header.setReorderingAllowed(false);
        int[] widths = {35,150,155,75,140,90,245};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(header);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        AdminCard.styleScrollBar(scroll,Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private void searchActivities() {
        filteredActivities.clear();
        String search = appliedSearch.toLowerCase(Locale.ROOT);
        String action = cmbAction.getSelectedItem().toString();
        for (Object[] activity : activities) {
            String details = "";
            for (Object value : activity) details += value + " ";
            if (details.toLowerCase(Locale.ROOT).contains(search)
                    && (action.equals("All Actions") || action.equalsIgnoreCase(activity[5].toString()))) {
                filteredActivities.add(activity);
            }
        }
        currentPage = 1;
        loadPage();
    }
    private JPanel createBottomPanel() {

        JPanel panel = new JPanel(new BorderLayout(12,0));
        panel.setOpaque(false);

        lblInfo = new JLabel();
        lblInfo.setFont(new Font("SansSerif",Font.PLAIN,12));
        lblInfo.setForeground(Color.GRAY);
        panel.add(lblInfo,BorderLayout.CENTER);

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));
        pagination.setOpaque(false);
        btnPrev = new JButton("<");
        btnNext = new JButton(">");
        btnOne = new JButton("1");
        btnTwo = new JButton("2");

        JButton[] buttons = {btnPrev,btnOne,btnTwo,btnNext};

        for(JButton b : buttons) {

            b.setPreferredSize(new Dimension(34,30));
            b.setFont(new Font("SansSerif",Font.BOLD,14));
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setBackground(Color.WHITE);
            b.setForeground(new Color(80,80,80));
            b.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
            pagination.add(b);
        }

        btnOne.addActionListener(e -> {

            currentPage = Integer.parseInt(btnOne.getText());
            loadPage();
        });

        btnTwo.addActionListener(e -> {

            currentPage = Integer.parseInt(btnTwo.getText());
            loadPage();
        });
        btnPrev.addActionListener(e -> {

            if(currentPage > 1) {

                currentPage--;
                loadPage();
            }
        });
        btnNext.addActionListener(e -> {

            if(currentPage * rowsPerPage < filteredActivities.size()) {

                currentPage++;
                loadPage();
            }
        });
        panel.add(pagination,BorderLayout.EAST);

        return panel;
    }


    private void loadPage() {

        model.setRowCount(0);
        int totalPages = Math.max(1, (filteredActivities.size() + rowsPerPage - 1) / rowsPerPage);
        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage,filteredActivities.size());

        for(int i = start; i < end; i++) {

            model.addRow(filteredActivities.get(i));
        }

        int first = 0;

        if(!filteredActivities.isEmpty()) {

            first = start + 1;
        }

        lblInfo.setText("Showing " + first + " to " + end + " of " + filteredActivities.size() + " activities");
        table.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        updatePaginationButtons();
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(end < filteredActivities.size());
        btnPrint.setEnabled(model.getRowCount() > 0);
    }

    private void updatePaginationButtons() {

        int totalPages = (int)Math.ceil(filteredActivities.size() / (double)rowsPerPage);

        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        int pairStart = ((currentPage - 1) / 2) * 2 + 1;
        btnOne.setText(String.valueOf(pairStart));
        btnTwo.setText(String.valueOf(pairStart + 1));
        btnTwo.setVisible(pairStart + 1 <= totalPages);

        btnOne.setBackground(Color.WHITE);
        btnOne.setForeground(new Color(80,80,80));
        btnTwo.setBackground(Color.WHITE);
        btnTwo.setForeground(new Color(80,80,80));

        if(currentPage == pairStart) {

            btnOne.setBackground(new Color(225,29,72));
            btnOne.setForeground(Color.WHITE);

        } else if(currentPage == pairStart + 1) {

            btnTwo.setBackground(new Color(225,29,72));
            btnTwo.setForeground(Color.WHITE);
        }
    }
    private void printActivities() {
        if (loading || editing) return;
        editing = true;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Print Activity Log", Dialog.ModalityType.APPLICATION_MODAL);
        DefaultTableModel snapshot = new DefaultTableModel(new String[]{"#","Timestamp","Email","Role","Module","Action","Description"},0) {
            @Override public boolean isCellEditable(int row,int column) { return false; }
        };
        for (int row=0;row<model.getRowCount();row++) {
            Object[] values=new Object[model.getColumnCount()];
            for (int col=0;col<values.length;col++) values[col]=table.getValueAt(row,col);
            snapshot.addRow(values);
        }
        JTable preview = new JTable(snapshot); preview.setRowHeight(30);
        JPanel content = new JPanel(new BorderLayout(12,12));
        content.setBorder(new EmptyBorder(20,20,20,20));
        content.add(new JLabel("Print current page of activity records"),BorderLayout.NORTH);
        content.add(new JScrollPane(preview),BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton print = new JButton("Print"), close = new JButton("Close");
        buttons.add(print); buttons.add(close); content.add(buttons,BorderLayout.SOUTH);
        close.addActionListener(e -> dialog.dispose());
        print.addActionListener(e -> {
            try {
                if (preview.print(JTable.PrintMode.FIT_WIDTH,new MessageFormat("Activity Log"),new MessageFormat("Page {0}"))) {
                    ActivityLogDao.recordActivity("Activity Log","Print","Printed the current page of activity records.");
                }
            }
            catch (PrinterException ex) { qpal.components.AppDialogs.showMessageDialog(dialog,"Unable to print activity logs.","Print Error",JOptionPane.ERROR_MESSAGE); }
        });
        dialog.setContentPane(content); dialog.setSize(900,450); dialog.setLocationRelativeTo(this);
        try { dialog.setVisible(true); } finally { editing=false; }
    }
    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(95,34));
        button.setFont(new Font("SansSerif",Font.BOLD,13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}

