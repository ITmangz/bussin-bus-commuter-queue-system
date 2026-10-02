package qpal.view.Admin;

import java.awt.*;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import qpal.components.ModernTableCellRenderer;

public class AdminRevenuePanel extends JPanel {
    private final List<JLabel> summaryValues = new ArrayList<>();
    private JLabel connectionStatus;
    private boolean loading;
    private boolean editing;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> cmbStatus;
    private JLabel lblInfo;
    private JButton btnOne;
    private JButton btnTwo;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnPrint;

    private JLabel lblPaidImage = new JLabel();
    private JLabel lblPendingImage = new JLabel();
    private JLabel lblRevenueImage = new JLabel();

    private final List<Object[]> revenues = new ArrayList<>();
    private final List<Object[]> filteredRevenues = new ArrayList<>();
    private int currentPage = 1;
    private final int rowsPerPage = 10;

    public AdminRevenuePanel() {

        setLayout(new BorderLayout(0,16));
        setBackground(new Color(245,245,245));
        setBorder(new EmptyBorder(20,25,20,25));

        add(createHeader(),BorderLayout.NORTH);
        add(createContent(),BorderLayout.CENTER);
        searchRevenue();
        Timer timer = new Timer(5000, e -> refreshData());
        addHierarchyListener(e -> {
            if (isShowing()) { timer.start(); refreshData(); }
            else timer.stop();
        });
    }

    public void refreshData() {
        if (loading || editing) return;
        loading = true;
        qpal.util.UiTask.run(() -> new qpal.dao.QueueDao().revenue(), data -> {
            Integer selectedId = selectedPaymentId();
            revenues.clear();
            revenues.addAll(data.rows());
            summaryValues.get(0).setText(String.valueOf(data.paidPassengers()));
            summaryValues.get(1).setText(String.valueOf(data.pendingPayments()));
            summaryValues.get(2).setText("PHP " + data.todayRevenue());
            int page = currentPage;
            searchRevenue();
            currentPage = Math.max(1, Math.min(page, (filteredRevenues.size() + rowsPerPage - 1) / rowsPerPage));
            loadPage();
            if (selectedId != null) for (int row=0; row<model.getRowCount(); row++) {
                if (selectedId.equals(model.getValueAt(row,0))) table.setRowSelectionInterval(row,row);
            }
            connectionStatus.setText(revenues.isEmpty() ? "No payment records yet." : "");
            connectionStatus.setVisible(revenues.isEmpty());
            loading = false;
        }, ex -> { loading = false; connectionStatus.setText("Unable to refresh payments. Retrying in 5 seconds."); connectionStatus.setVisible(true); });
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout(0,16));
        panel.setOpaque(false);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading,BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Revenue Management");
        lblTitle.setFont(new Font("SansSerif",Font.BOLD,30));
        lblTitle.setForeground(new Color(228,0,70));
        heading.add(lblTitle);
        heading.add(Box.createVerticalStrut(4));

        JLabel lblSubtitle = new JLabel("Manage fare collections and total revenue.");
        lblSubtitle.setFont(new Font("SansSerif",Font.PLAIN,13));
        lblSubtitle.setForeground(new Color(120,120,120));
        heading.add(lblSubtitle);
        panel.add(heading,BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1,3,14,0));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(0,110));
        cards.add(createSummaryCard("TOTAL NUMBER OF PAID COMMUTERS",lblPaidImage));
        cards.add(createSummaryCard("PENDING PAYMENT",lblPendingImage));
        cards.add(createSummaryCard("TODAY'S REVENUE",lblRevenueImage));
        panel.add(cards,BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryCard(String text, JLabel lblImage) {

        JPanel panel = AdminDashboardPanel.card();
        panel.setLayout(new GridBagLayout());
        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setOpaque(false);

        // Assign an ImageIcon here when the card images are ready.
        Dimension imageSize = new Dimension(40,40);
        lblImage.setMinimumSize(imageSize);
        lblImage.setPreferredSize(imageSize);
        lblImage.setMaximumSize(imageSize);
        lblImage.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(text);
        lblTitle.setFont(new Font("SansSerif",Font.PLAIN,9));
        lblTitle.setForeground(new Color(100,100,100));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel("—");
        summaryValues.add(lblValue);
        lblValue.setFont(new Font("SansSerif",Font.BOLD,30));
        lblValue.setForeground(new Color(170,0,45));
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblValue, BorderLayout.CENTER);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        panel.add(content, constraints);

        return panel;
    }

    private JPanel createContent() {

        JPanel panel = AdminDashboardPanel.card();
        panel.setLayout(new BorderLayout(0,12));
        panel.add(createTopPanel(),BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(createTable(),BorderLayout.CENTER);

        JLabel lblEmpty = new JLabel("Loading payments...");
        connectionStatus = lblEmpty;
        lblEmpty.setFont(new Font("SansSerif",Font.PLAIN,12));
        lblEmpty.setForeground(new Color(120,120,120));
        lblEmpty.setBorder(new EmptyBorder(12,0,8,0));
        center.add(lblEmpty,BorderLayout.SOUTH);
        panel.add(center,BorderLayout.CENTER);
        panel.add(createBottomPanel(),BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTopPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel("Revenue List");
        lblTitle.setFont(new Font("SansSerif",Font.BOLD,22));
        panel.add(lblTitle,BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        controls.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(190,34));
        searchField.setFont(new Font("SansSerif",Font.PLAIN,13));
        searchField.setMargin(new Insets(0,10,0,10));
        searchField.setToolTipText("Search payment ID, queue number, seat, bus or route");

        cmbStatus = new JComboBox<>(new String[]{"All Statuses","Paid","Pending","Cancelled"});
        AdminFormStyle.tableFilter(cmbStatus);
        cmbStatus.setFont(new Font("SansSerif",Font.PLAIN,13));
        cmbStatus.setBackground(Color.WHITE);
        cmbStatus.setFocusable(false);
        cmbStatus.addActionListener(e -> {
            searchRevenue();
        });

        JButton btnSearch = createButton("Search",new Color(225,29,72));
        btnSearch.addActionListener(e -> {
            if (AdminFormStyle.validateSearch(searchField)) searchRevenue();
        });
        AdminFormStyle.searchOnEnter(searchField, btnSearch);

        controls.add(searchField);
        controls.add(cmbStatus);
        controls.add(btnSearch);
        btnPrint = createButton("Print",new Color(59,130,246));
        btnPrint.setToolTipText("Print the current page of revenue records");
        btnPrint.addActionListener(e -> printRevenue());
        controls.add(btnPrint);
        panel.add(controls,BorderLayout.EAST);

        return panel;
    }

    private JScrollPane createTable() {

        String[] columns = {"Payment ID","Queue No.","Bus","Route","Date / Time","Amount","Status","Seat"};
        model = new DefaultTableModel(columns,0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("SansSerif",Font.PLAIN,12));
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(236,236,236));
        table.setSelectionBackground(new Color(240,247,255));
        table.setDefaultRenderer(Object.class,new ModernTableCellRenderer());
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif",Font.BOLD,12));
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0,34));
        header.setReorderingAllowed(false);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(50);
        table.moveColumn(7,2);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        AdminCard.styleScrollBar(scroll,Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
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

            if(currentPage * rowsPerPage < filteredRevenues.size()) {

                currentPage++;
                loadPage();
            }
        });
        panel.add(pagination,BorderLayout.EAST);

        return panel;
    }

    private void searchRevenue() {

        filteredRevenues.clear();
        String search = searchField.getText().trim().toLowerCase();
        String status = cmbStatus.getSelectedItem().toString();

        for(Object[] revenue : revenues) {

            String details = (revenue[0] + " " + revenue[1] + " " + revenue[7] + " " + revenue[2] + " " + revenue[3]).toLowerCase();

            if(details.contains(search) && (status.equals("All Statuses") || status.equalsIgnoreCase(revenue[6].toString()))) {

                filteredRevenues.add(revenue);
            }
        }

        if (status.equals("All Statuses")) {
            filteredRevenues.sort(java.util.Comparator.comparingInt(row ->
                    "Pending".equals(row[6]) ? 0 : "Paid".equals(row[6]) ? 2 : 1));
        }
        currentPage = 1;
        loadPage();
    }

    private Integer selectedPaymentId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : ((Number)model.getValueAt(table.convertRowIndexToModel(row),0)).intValue();
    }

    private void loadPage() {

        model.setRowCount(0);
        int totalPages = Math.max(1, (filteredRevenues.size() + rowsPerPage - 1) / rowsPerPage);
        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        int start = (currentPage - 1) * rowsPerPage;
        int end = Math.min(start + rowsPerPage,filteredRevenues.size());

        for(int i = start; i < end; i++) {

            model.addRow(filteredRevenues.get(i));
        }

        int first = 0;

        if(!filteredRevenues.isEmpty()) {

            first = start + 1;
        }

        lblInfo.setText("Showing " + first + " to " + end + " of " + filteredRevenues.size() + " commuters");
        table.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        updatePaginationButtons();
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(end < filteredRevenues.size());
        btnPrint.setEnabled(model.getRowCount() > 0);
    }

    private void updatePaginationButtons() {

        int totalPages = (int)Math.ceil(filteredRevenues.size() / (double)rowsPerPage);

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
    private void printRevenue() {
        if (loading || editing) return;
        editing = true;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Print Revenue", Dialog.ModalityType.APPLICATION_MODAL);
        DefaultTableModel snapshot = new DefaultTableModel(new String[]{"Payment ID","Queue No.","Seat","Bus","Route","Date / Time","Amount","Status"},0) {
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
        content.add(new JLabel("Print current page of revenue records"),BorderLayout.NORTH);
        content.add(new JScrollPane(preview),BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton print = new JButton("Print"), close = new JButton("Close");
        buttons.add(print); buttons.add(close); content.add(buttons,BorderLayout.SOUTH);
        close.addActionListener(e -> dialog.dispose());
        print.addActionListener(e -> {
            try {
                if (preview.print(JTable.PrintMode.FIT_WIDTH,new MessageFormat("Revenue Management"),new MessageFormat("Page {0}"))) {
                    qpal.dao.ActivityLogDao.recordActivity("Revenue", "Print", "Printed the revenue report.");
                }
            }
            catch (PrinterException ex) { qpal.components.AppDialogs.showMessageDialog(dialog,"Unable to print revenue.","Print Error",JOptionPane.ERROR_MESSAGE); }
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
