package qpal.view.Admin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

public class AdminQueuePanel extends JPanel {
    private final DefaultTableModel boardingModel = new DefaultTableModel(
            new String[]{"Queue No.", "Destination", "Bus", "Time", "Payment", "Status", "Passengers"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTabbedPane queues = new JTabbedPane();
    private JTable boardingTable;
    private int boardingPage = 1;
    private String boardingSearch = "", boardingStatus = "All Statuses";
    private TableRowSorter<DefaultTableModel> boardingSorter;
    private final JLabel boardingPageInfo = new JLabel();
    private final JPanel boardingPagination = transparent(new FlowLayout(FlowLayout.RIGHT,6,0));
    private JPanel queueActions;
    private final java.util.List<JButton> sideActions = new java.util.ArrayList<>();
    private JButton completeButton, ticketButton;
    private final JComboBox<String> counter = new JComboBox<>(new String[]{"Select counter", "Counter 1", "Counter 2"});
    private final JComboBox<String> gate = new JComboBox<>(new String[]{"Select gate", "Gate 1", "Gate 2"});
    private final JPanel stationPanel = new JPanel(new CardLayout());
    private java.util.Map<Integer,Integer> paymentStations = java.util.Map.of(), boardingStations = java.util.Map.of();
    private java.util.List<qpal.model.BookingData.QueueRow> boardingRows = new java.util.ArrayList<>();
    private JTable table;
    private JLabel number;
    private final JLabel detailTitle = sectionTitle("Currently Serving");
    private final JLabel loadStatus = new JLabel("Loading queues...");
    private final java.util.List<JLabel> stats = new java.util.ArrayList<>();
    private final java.util.Map<String,JLabel> detailLabels = new java.util.HashMap<>();
    private final java.util.List<JButton> actionButtons = new java.util.ArrayList<>();
    private java.util.List<qpal.model.BookingData.QueueRow> rows = new java.util.ArrayList<>();
    private boolean loading;
    private boolean acting;
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private String queueSearch = "", queueStatus = "All Statuses";
    private TableRowSorter<DefaultTableModel> sorter;
    private final JLabel pageInfo = new JLabel();
    private final JPanel pagination = transparent(new FlowLayout(FlowLayout.RIGHT,6,0));

    private void loadQueuePage() {
        java.util.List<Integer> matches = new java.util.ArrayList<>();
        for (int i=0;i<rows.size();i++) {
            var row=rows.get(i);
            if ((String.valueOf(row.number()).contains(queueSearch)
                    || String.format(java.util.Locale.ROOT,"P%03d",row.number()).toLowerCase(java.util.Locale.ROOT).contains(queueSearch))
                    && (queueStatus.equals("All Statuses") || queueStatus.equals(row.status()))) matches.add(i);
        }
        int pages=Math.max(1,(matches.size()+PAGE_SIZE-1)/PAGE_SIZE);
        currentPage=Math.max(1,Math.min(currentPage,pages));
        int start=(currentPage-1)*PAGE_SIZE, end=Math.min(start+PAGE_SIZE,matches.size());
        java.util.Set<Integer> visible=new java.util.HashSet<>(matches.subList(start,end));
        sorter.setRowFilter(new RowFilter<DefaultTableModel,Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel,? extends Integer> entry) {
                return visible.contains(entry.getIdentifier());
            }
        });
        pageInfo.setText("Showing "+(matches.isEmpty()?0:start+1)+" to "+end+" of "+matches.size()+" queues");
        pagination.removeAll();
        pageButton("<",currentPage-1,currentPage>1,false);
        int first=Math.max(1,Math.min(currentPage-2,pages-4));
        for (int page=first;page<=Math.min(pages,first+4);page++) pageButton(String.valueOf(page),page,true,page==currentPage);
        pageButton(">",currentPage+1,currentPage<pages,false);
        pagination.revalidate(); pagination.repaint();
        if (!loading) showSelectedDetails();
    }

    private void pageButton(String text,int page,boolean enabled,boolean active) {
        JButton button=new JButton(text);
        button.setPreferredSize(new Dimension(34,30));
        button.setFont(new Font("SansSerif",Font.BOLD,14));
        button.setFocusPainted(false);
        button.setBackground(active ? new Color(225,29,72) : Color.WHITE);
        button.setForeground(active ? Color.WHITE : new Color(80,80,80));
        button.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setEnabled(enabled);
        button.addActionListener(e -> { currentPage=page; loadQueuePage(); });
        pagination.add(button);
    }
    private void loadBoardingPage() {
        java.util.List<Integer> matches = new java.util.ArrayList<>();
        for (int i=0;i<boardingRows.size();i++) {
            var row=boardingRows.get(i);
            if ((String.valueOf(row.number()).contains(boardingSearch)
                    || String.format(java.util.Locale.ROOT,"B%03d",row.number()).toLowerCase(java.util.Locale.ROOT).contains(boardingSearch))
                    && (boardingStatus.equals("All Statuses") || boardingStatus.equals(row.status()))) matches.add(i);
        }
        int pages=Math.max(1,(matches.size()+PAGE_SIZE-1)/PAGE_SIZE);
        boardingPage=Math.max(1,Math.min(boardingPage,pages));
        int start=(boardingPage-1)*PAGE_SIZE, end=Math.min(start+PAGE_SIZE,matches.size());
        java.util.Set<Integer> visible=new java.util.HashSet<>(matches.subList(start,end));
        boardingSorter.setRowFilter(new RowFilter<DefaultTableModel,Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel,? extends Integer> entry) {
                return visible.contains(entry.getIdentifier());
            }
        });
        boardingPageInfo.setText("Showing "+(matches.isEmpty()?0:start+1)+" to "+end+" of "+matches.size()+" queues");
        boardingPagination.removeAll();
        boardingPageButton("<",boardingPage-1,boardingPage>1,false);
        int first=Math.max(1,Math.min(boardingPage-2,pages-4));
        for (int page=first;page<=Math.min(pages,first+4);page++) boardingPageButton(String.valueOf(page),page,true,page==boardingPage);
        boardingPageButton(">",boardingPage+1,boardingPage<pages,false);
        boardingPagination.revalidate(); boardingPagination.repaint();
        if (!loading) showSelectedDetails();
    }

    private void boardingPageButton(String text,int page,boolean enabled,boolean active) {
        JButton button=new JButton(text);
        button.setPreferredSize(new Dimension(34,30));
        button.setFont(new Font("SansSerif",Font.BOLD,14));
        button.setFocusPainted(false);
        button.setBackground(active ? new Color(225,29,72) : Color.WHITE);
        button.setForeground(active ? Color.WHITE : new Color(80,80,80));
        button.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setEnabled(enabled);
        button.addActionListener(e -> { boardingPage=page; loadBoardingPage(); });
        boardingPagination.add(button);
    }
    private static final Color RED = new Color(225, 0, 45);
    private static final Color MUTED = new Color(100, 109, 124);
    private final JLabel lblCommutersImage = new JLabel();
    private final JLabel lblBusesImage = new JLabel();
    private final JLabel lblQueueImage = new JLabel();
    private final DefaultTableModel queueModel = new DefaultTableModel(
            new String[]{"Queue No.", "Destination", "Bus", "Time", "Payment", "Status", "Passengers"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };


    public AdminQueuePanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 25, 18, 25));

        JPanel heading = transparent(new GridLayout(2, 1, 0, 2));
        heading.add(label("Queue Management", 30, RED, true));
        heading.add(label("Manage and monitor commuter queues in real time.", 14, MUTED, false));
        add(heading, BorderLayout.NORTH);

        JPanel body = transparent(new BorderLayout(0, 14));
        JPanel upper = transparent(new BorderLayout(0, 14));
        JPanel summary = transparent(new GridLayout(1, 3, 16, 0));
        summary.setPreferredSize(new Dimension(0, 155));
        summary.add(stat("TOTAL NUMBER OF COMMUTERS", lblCommutersImage, new Color(255, 232, 238)));
        summary.add(stat("NUMBER OF AVAILABLE BUSES", lblBusesImage, new Color(255, 245, 210)));
        summary.add(stat("CURRENT QUEUE NUMBER", lblQueueImage, new Color(228, 249, 233)));
        upper.add(summary, BorderLayout.NORTH);

        JPanel servingRow = transparent(new BorderLayout(14, 0));
        servingRow.setPreferredSize(new Dimension(0, 220));
        JPanel serving = card();
        serving.setLayout(new BorderLayout(10, 10));
        serving.add(detailTitle, BorderLayout.NORTH);
        JPanel current = transparent(new BorderLayout(16, 0));
        JPanel ticket = new JPanel(new GridLayout(2, 1));
        ticket.setBackground(new Color(255, 238, 242));
        ticket.setPreferredSize(new Dimension(105, 70));
        JLabel queueCaption = label("Queue No.", 11, RED, false);
        queueCaption.setHorizontalAlignment(SwingConstants.CENTER);
        number = label("—", 32, RED, true);
        number.setHorizontalAlignment(SwingConstants.CENTER);
        ticket.add(queueCaption);
        ticket.add(number);
        current.add(ticket, BorderLayout.WEST);
        JPanel fields = transparent(new GridLayout(2, 3, 10, 8));
        fields.add(detail("Route", "—"));
        fields.add(detail("Bus", "—"));
        fields.add(detail("Departure Time", "—"));
        fields.add(detail("Passenger", "—"));
        fields.add(detail("Payment Status", "—"));
        fields.add(detail("Boarding Status", "—"));
        current.add(fields, BorderLayout.CENTER);
        serving.add(current, BorderLayout.CENTER);
        JPanel servingButtons = transparent(new GridLayout(1, 0, 8, 0));
        servingButtons.add(unavailableAction("View Details", false));
        servingButtons.add(unavailableAction("Recall", false));
        ticketButton = unavailableAction("Print Ticket", false);
        servingButtons.add(ticketButton);
        completeButton = unavailableAction("Complete", true);
        servingButtons.add(completeButton);
        serving.add(servingButtons, BorderLayout.SOUTH);
        servingRow.add(serving, BorderLayout.CENTER);

        JPanel actions = card();
        queueActions = actions;
        actions.setPreferredSize(new Dimension(205, 0));
        actions.setLayout(new BorderLayout(0, 8));
        actions.add(sectionTitle("Queue Actions"), BorderLayout.NORTH);
        JPanel actionButtons = transparent(new GridLayout(4, 1, 0, 6));
        stationPanel.add(counter, "Payment"); stationPanel.add(gate, "Boarding");
        AdminFormStyle.tableFilter(counter); AdminFormStyle.tableFilter(gate);
        counter.addActionListener(e -> showSelectedDetails()); gate.addActionListener(e -> showSelectedDetails());
        actionButtons.add(stationPanel);
        actionButtons.add(unavailableAction("Call Next Queue", true));
        actionButtons.add(unavailableAction("Skip Queue", false));
        actionButtons.add(unavailableAction("Payment", false));

        for (Component control : actionButtons.getComponents()) if (control instanceof JButton b) sideActions.add(b);
        actions.add(actionButtons, BorderLayout.CENTER);
        servingRow.add(actions, BorderLayout.EAST);
        upper.add(servingRow, BorderLayout.CENTER);
        body.add(upper, BorderLayout.NORTH);

        JPanel waiting = card();
        waiting.setLayout(new BorderLayout(0, 10));
        JPanel toolbar = transparent(new BorderLayout(16, 0));
        toolbar.add(sectionTitle("Payment Queue"), BorderLayout.WEST);
        JPanel filters = transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JTextField search = new JTextField();
        search.setPreferredSize(new Dimension(190, 34));
        search.setFont(new Font("SansSerif", Font.PLAIN, 13));
        search.setMargin(new Insets(0, 10, 0, 10));
        search.setToolTipText("Search queue number");
        search.getAccessibleContext().setAccessibleName("Search queue number");

        JComboBox<String> trips = new JComboBox<>(new String[]{"All Statuses", "Waiting", "Serving", "Skipped", "Completed", "Cancelled"});
        AdminFormStyle.tableFilter(trips);
        trips.setFont(new Font("SansSerif", Font.PLAIN, 13));
        trips.setFocusable(false);
        trips.setBackground(Color.WHITE);
        trips.setToolTipText("Filter queue status");

        JButton searchButton = button("Search", true);
        searchButton.setPreferredSize(new Dimension(100, 34));
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        searchButton.setBackground(new Color(225, 29, 72));
        searchButton.setBorder(BorderFactory.createEmptyBorder());
        filters.add(search);
        filters.add(trips);
        filters.add(searchButton);
        JButton printButton = button("View", true);
        printButton.setPreferredSize(new Dimension(95, 34));
        printButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        printButton.setBackground(new Color(59, 130, 246));
        printButton.setBorder(BorderFactory.createEmptyBorder());
        printButton.setToolTipText("Open the live commuter queue monitor");
        printButton.addActionListener(e -> QueueMonitor.open(this, isBoarding()));
        filters.add(printButton);
        toolbar.add(filters, BorderLayout.EAST);
        waiting.add(toolbar, BorderLayout.NORTH);

        table = queueTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !loading) showSelectedDetails();
        });
        sorter = new TableRowSorter<>(queueModel);
        for (int column=0;column<queueModel.getColumnCount();column++) sorter.setSortable(column,false);
        table.setRowSorter(sorter);
        Runnable applySearch = () -> {
            queueSearch = search.getText().trim().toLowerCase(java.util.Locale.ROOT);
            queueStatus = trips.getSelectedItem().toString();
            currentPage = 1;
            loadQueuePage();
        };
        searchButton.addActionListener(e -> {
            if (AdminFormStyle.validateSearch(search)) applySearch.run();
        });
        search.addActionListener(e -> {
            if (AdminFormStyle.validateSearch(search)) applySearch.run();
        });
        trips.addActionListener(e -> applySearch.run());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        AdminCard.styleScrollBar(scroll,Color.WHITE);
        waiting.add(scroll, BorderLayout.CENTER);
        JPanel footer = transparent(new BorderLayout());
        footer.setBorder(new EmptyBorder(18,0,0,0));
        JPanel pageRow = transparent(new BorderLayout());
        pageInfo.setFont(new Font("SansSerif",Font.PLAIN,12));
        pageInfo.setForeground(new Color(130,130,130));
        pageRow.add(pageInfo,BorderLayout.WEST);
        pageRow.add(pagination,BorderLayout.EAST);
        footer.add(pageRow,BorderLayout.CENTER);
        waiting.add(footer,BorderLayout.SOUTH);
        loadQueuePage();
        queues.addTab("Payment Queue", waiting);
        JPanel boarding = createBoardingPanel();
        queues.addTab("Boarding Queue", boarding);
        queues.addChangeListener(e -> {
            String[] labels = isBoarding()
                    ? new String[]{"Call Next Queue", "Skip Queue"}
                    : new String[]{"Call Next Queue", "Skip Queue", "Payment"};
            JPanel controls = (JPanel) sideActions.get(0).getParent();
            controls.removeAll();
            ((CardLayout)stationPanel.getLayout()).show(stationPanel,isBoarding() ? "Boarding" : "Payment");
            controls.add(stationPanel);
            ticketButton.setVisible(!isBoarding());
            // GridLayout reserves space even for hidden components.
            if (isBoarding()) servingButtons.remove(ticketButton);
            else servingButtons.add(ticketButton, 2);
            controls.setLayout(new GridLayout(4,1,0,6));
            for (int i=0;i<labels.length;i++) {
                sideActions.get(i).setText(labels[i]);
                controls.add(sideActions.get(i));
            }
            completeButton.setText(isBoarding() ? "Complete Boarding" : "Complete");
            showSelectedDetails();
            revalidate(); repaint();
        });
        body.add(queues, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        loadStatus.setFont(new Font("Segoe UI",Font.PLAIN,11));
        loadStatus.setForeground(MUTED);
        add(loadStatus, BorderLayout.SOUTH);
        Timer timer = new Timer(5000, e -> refreshData());
        addHierarchyListener(e -> {
            if (isShowing()) { timer.start(); refreshData(); }
            else timer.stop();
        });
    }

    private JPanel createBoardingPanel() {
        JPanel boarding = card();
        boarding.setLayout(new BorderLayout(0, 10));
        JPanel toolbar = transparent(new BorderLayout(16, 0));
        toolbar.add(sectionTitle("Boarding Queue"), BorderLayout.WEST);
        JPanel filters = transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JTextField search = new JTextField();
        search.setPreferredSize(new Dimension(190, 34));
        search.setFont(new Font("SansSerif", Font.PLAIN, 13));
        search.setMargin(new Insets(0, 10, 0, 10));
        search.setToolTipText("Search queue number");
        search.getAccessibleContext().setAccessibleName("Search queue number");

        JComboBox<String> trips = new JComboBox<>(new String[]{"All Statuses", "Boarding"});
        AdminFormStyle.tableFilter(trips);
        trips.setFont(new Font("SansSerif", Font.PLAIN, 13));
        trips.setFocusable(false);
        trips.setBackground(Color.WHITE);
        trips.setToolTipText("Filter queue status");

        JButton searchButton = button("Search", true);
        searchButton.setPreferredSize(new Dimension(100, 34));
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        searchButton.setBackground(new Color(225, 29, 72));
        searchButton.setBorder(BorderFactory.createEmptyBorder());
        filters.add(search);
        filters.add(trips);
        filters.add(searchButton);
        JButton printButton = button("View", true);
        printButton.setPreferredSize(new Dimension(95, 34));
        printButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        printButton.setBackground(new Color(59, 130, 246));
        printButton.setBorder(BorderFactory.createEmptyBorder());
        printButton.setToolTipText("Open the live commuter queue monitor");
        printButton.addActionListener(e -> QueueMonitor.open(this, isBoarding()));
        filters.add(printButton);
        toolbar.add(filters, BorderLayout.EAST);
        boarding.add(toolbar, BorderLayout.NORTH);

        boardingTable = queueTable(boardingModel);
        boardingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        boardingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !loading) showSelectedDetails();
        });
        boardingSorter = new TableRowSorter<>(boardingModel);
        for (int column=0;column<boardingModel.getColumnCount();column++) boardingSorter.setSortable(column,false);
        boardingTable.setRowSorter(boardingSorter);
        Runnable applySearch = () -> {
            boardingSearch = search.getText().trim().toLowerCase(java.util.Locale.ROOT);
            boardingStatus = trips.getSelectedItem().toString();
            boardingPage = 1;
            loadBoardingPage();
        };
        searchButton.addActionListener(e -> {
            if (AdminFormStyle.validateSearch(search)) applySearch.run();
        });
        search.addActionListener(e -> {
            if (AdminFormStyle.validateSearch(search)) applySearch.run();
        });
        trips.addActionListener(e -> applySearch.run());
        JScrollPane scroll = new JScrollPane(boardingTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        AdminCard.styleScrollBar(scroll,Color.WHITE);
        boarding.add(scroll, BorderLayout.CENTER);
        JPanel footer = transparent(new BorderLayout());
        footer.setBorder(new EmptyBorder(18,0,0,0));
        JPanel pageRow = transparent(new BorderLayout());
        boardingPageInfo.setFont(new Font("SansSerif",Font.PLAIN,12));
        boardingPageInfo.setForeground(new Color(130,130,130));
        pageRow.add(boardingPageInfo,BorderLayout.WEST);
        pageRow.add(boardingPagination,BorderLayout.EAST);
        footer.add(pageRow,BorderLayout.CENTER);
        boarding.add(footer,BorderLayout.SOUTH);
        loadBoardingPage();
        return boarding;
    }

    public void refreshData() {
        if (loading || acting) return;
        loading = true;
        showSelectedDetails();
        qpal.util.UiTask.run(() -> {
            var queues = new qpal.dao.QueueDao().today();
            long buses = new qpal.dao.BookingDao().availableTrips().stream().map(t -> t.bus()).distinct().count();
            return new Object[]{queues, buses, new qpal.dao.QueueDao().boarding(), new qpal.dao.QueueDao().stations("Payment"), new qpal.dao.QueueDao().stations("Boarding")};
        }, data -> {
            qpal.model.BookingData.QueueRow selected = waitingSelectedRow();
            var selectedBoarding = boardingTable.getSelectedRow() < 0 ? null
                    : boardingRows.get(boardingTable.convertRowIndexToModel(boardingTable.getSelectedRow()));
            @SuppressWarnings("unchecked")
            var loaded = (java.util.List<qpal.model.BookingData.QueueRow>)data[0];
            rows = loaded;
            paymentStations = (java.util.Map<Integer,Integer>)data[3]; boardingStations = (java.util.Map<Integer,Integer>)data[4];
            boardingModel.setRowCount(0);
            @SuppressWarnings("unchecked")
            var loadedBoarding = (java.util.List<qpal.model.BookingData.QueueRow>) data[2];
            boardingRows = loadedBoarding;
            for (var row : boardingRows) boardingModel.addRow(new Object[]{String.format("B%03d",row.number()),
                    row.route(),row.bus(),row.schedule(),row.payment(),row.status(),row.passengers()});
            loadBoardingPage();
            if (selectedBoarding != null) for (int i=0;i<boardingRows.size();i++)
                if (boardingRows.get(i).id()==selectedBoarding.id()) {
                    int view = boardingTable.convertRowIndexToView(i);
                    if (view >= 0) boardingTable.setRowSelectionInterval(view,view);
                }
            queueModel.setRowCount(0);
            int passengers = 0;
            qpal.model.BookingData.QueueRow serving = null;
            for (var row : rows) {
                queueModel.addRow(new Object[]{String.format("P%03d",row.number()),row.route(),row.bus(),row.schedule(),row.payment(),row.status(),row.passengers()});
                if (!row.status().equals("Cancelled")) passengers += row.passengers();
                if (row.status().equals("Serving")) serving = row;
            }
            loadQueuePage();
            if (selected != null) for (int i = 0; i < rows.size(); i++) if (rows.get(i).id() == selected.id()) {
                int view = table.convertRowIndexToView(i);
                if (view >= 0) table.setRowSelectionInterval(view, view);
            }
            stats.get(0).setText(String.valueOf(passengers));
            stats.get(1).setText(String.valueOf(data[1]));
            stats.get(2).setText(serving == null ? "—" : String.format("P%03d",serving.number()));
            number.setText(serving == null ? "—" : String.format("P%03d",serving.number()));
            detailLabels.get("Route").setText(serving == null ? "—" : serving.route());
            detailLabels.get("Bus").setText(serving == null ? "—" : serving.bus());
            detailLabels.get("Departure Time").setText(serving == null ? "—" : serving.schedule());
            detailLabels.get("Passenger").setText(serving == null ? "—" : serving.passenger());
            detailLabels.get("Payment Status").setText(serving == null ? "—" : serving.payment());
            detailLabels.get("Boarding Status").setText(serving == null ? "—" : serving.status());
            loadStatus.setText(rows.isEmpty() ? "No bookings in today's queue." : "Today's queues: " + rows.size());
            loading = false;
            showSelectedDetails();
        }, ex -> { loading = false; showSelectedDetails(); loadStatus.setText("Unable to refresh queues. Retrying in 5 seconds."); });
    }

    private int station() { return (isBoarding() ? gate : counter).getSelectedIndex(); }
    private qpal.model.BookingData.QueueRow stationRow() {
        Integer id=(isBoarding() ? boardingStations : paymentStations).get(station());
        return (isBoarding() ? boardingRows : rows).stream().filter(r -> java.util.Objects.equals(id,r.id())
                && (isBoarding() || r.status().equals("Serving"))).findFirst().orElse(null);
    }
    private boolean isBoarding() { return queues.getSelectedIndex() == 1; }

    private qpal.model.BookingData.QueueRow selectedRow() {
        if (!isBoarding()) return waitingSelectedRow();
        int index = boardingTable.getSelectedRow();
        return index < 0 ? null : boardingRows.get(boardingTable.convertRowIndexToModel(index));
    }

    private qpal.model.BookingData.QueueRow waitingSelectedRow() {
        int selected = table.getSelectedRow();
        if (selected < 0) return null;
        int index = table.convertRowIndexToModel(selected);
        return index < rows.size() ? rows.get(index) : null;
    }

    private void showSelectedDetails() {
        var row = selectedRow();
        detailTitle.setText(isBoarding() ? "Currently Boarding" : row == null ? "Currently Serving" : "Selected Queue Details");
        if (row == null) row = stationRow();
        counter.setEnabled(!acting && !loading); gate.setEnabled(!acting && !loading);
        final boolean hasRow = row != null;
        for (JButton button : actionButtons) button.setEnabled(station() > 0 && !loading && !acting
                && (!isBoarding() || hasRow || (button.getText().equals("Call Next Queue") && boardingTable.getRowCount() > 0)));
        number.setText(row == null ? "—" : String.format(isBoarding() ? "B%03d" : "P%03d", row.number()));
        detailLabels.get("Route").setText(row == null ? "—" : row.route());
        detailLabels.get("Bus").setText(row == null ? "—" : row.bus());
        detailLabels.get("Departure Time").setText(row == null ? "—" : row.schedule());
        detailLabels.get("Passenger").setText(row == null ? "—" : row.passenger());
        detailLabels.get("Payment Status").setText(row == null ? "—" : row.payment());
        detailLabels.get("Boarding Status").setText(row == null ? "—" : row.status());
    }

    private void act(String action) {
        if (acting || loading || station() == 0) return;
        if (isBoarding()) { actBoarding(action); return; }
        var selected = selectedRow();
        if (selected == null && !action.equals("Call Next Queue")) {
            selected = stationRow();
        }
        if (selected == null && !action.equals("Call Next Queue")) {
            qpal.components.AppDialogs.showMessageDialog(this, "Select a queue first."); return;
        }
        final var row = selected;
        final int selectedStation = station();
        if (action.equals("View Details")) {
            qpal.util.UiTask.run(() -> new qpal.dao.BookingDao().receipt(row.bookingId()), receipt -> {
                JTextArea text = new JTextArea(receipt.text()); text.setEditable(false);
                qpal.components.AppDialogs.showMessageDialog(this, text, "Booking Details", JOptionPane.PLAIN_MESSAGE);
            }, ex -> qpal.components.AppDialogs.showMessageDialog(this, ex.getMessage()));
            return;
        }
        if (action.equals("Payment") || action.equals("Print Ticket")) {
            acting = true; showSelectedDetails();
            Runnable closed = () -> { acting=false; refreshData(); };
            if (action.equals("Print Ticket")) QueuePaymentDialog.openTickets(this,row,selectedStation,closed);
            else QueuePaymentDialog.open(this,row,selectedStation,closed);
            return;
        }
        acting = true;
        actionButtons.forEach(b -> b.setEnabled(false));
        qpal.util.UiTask.run(() -> { new qpal.dao.QueueDao().act(row == null ? 0 : row.id(), action, selectedStation); return true; }, result -> {
            announceQueue(action,false,selectedStation,row);
            table.clearSelection();
            if (action.equals("Recall")) {
                qpal.components.AppDialogs.showMessageDialog(this,
                        String.format("Queue P%03d: please proceed to Counter %d.",row.number(),selectedStation),
                        "Queue Recall",JOptionPane.INFORMATION_MESSAGE);
            }
            acting = false; actionButtons.forEach(b -> b.setEnabled(true)); refreshData();
        }, ex -> { acting = false; actionButtons.forEach(b -> b.setEnabled(true));
            qpal.components.AppDialogs.showMessageDialog(this, ex.getMessage(), "Queue Action", JOptionPane.WARNING_MESSAGE); refreshData(); });
    }

    private void actBoarding(String action) {
        if (action.equals("Call Next Queue") || action.equals("Skip Queue")) {
            int selectedStation=station();
            var calledRow=stationRow();
            acting=true; showSelectedDetails();
            qpal.util.UiTask.run(() -> { new qpal.dao.QueueDao().callBoarding(0,selectedStation,action.equals("Skip Queue")); return true; }, result -> {
                announceQueue(action,true,selectedStation,calledRow);
                acting=false; boardingTable.clearSelection(); refreshData();
            }, ex -> { acting=false; qpal.components.AppDialogs.showMessageDialog(this,ex.getMessage()); refreshData(); });
            return;
        }
        var row = stationRow();
        if (row == null) return;        if (action.equals("View Details")) {
            qpal.util.UiTask.run(() -> new qpal.dao.BookingDao().receipt(row.bookingId()), receipt -> {
                JTextArea text = new JTextArea(receipt.text("B")); text.setEditable(false);
                qpal.components.AppDialogs.showMessageDialog(this,text,"Boarding Details",JOptionPane.PLAIN_MESSAGE);
            }, ex -> qpal.components.AppDialogs.showMessageDialog(this,ex.getMessage()));
        } else if (action.equals("Recall")) {
            announceQueue(action,true,station(),row);
            qpal.components.AppDialogs.showMessageDialog(this,String.format("Queue B%03d: please board bus %s for %s.",
                    row.number(),row.bus(),row.route()),"Boarding Recall",JOptionPane.INFORMATION_MESSAGE);
        } else if (action.equals("Complete Boarding")) {
            acting = true;
            showSelectedDetails();
            qpal.util.UiTask.run(() -> { new qpal.dao.QueueDao().completeBoarding(row.id()); return true; }, result -> {
                acting = false; refreshData();
            }, ex -> { acting = false; qpal.components.AppDialogs.showMessageDialog(this,ex.getMessage(),
                    "Unable to Complete Boarding",JOptionPane.WARNING_MESSAGE); refreshData(); });
        }
    }

    private void announceQueue(String action,boolean boarding,int station,qpal.model.BookingData.QueueRow row) {
        if (action.equals("Call Next Queue")) {
            qpal.util.UiTask.run(() -> {
                var dao=new qpal.dao.QueueDao();
                Integer id=dao.stations(boarding ? "Boarding" : "Payment").get(station);
                return (boarding ? dao.boarding() : dao.today()).stream()
                        .filter(r -> java.util.Objects.equals(id,r.id())).findFirst().orElse(null);
            }, called -> {
                if (called!=null) qpal.util.QueueVoice.announce(called.number(),boarding,station,false);
            }, ex -> System.err.println("Unable to load called queue for announcement: "+ex.getMessage()));
        } else if (row!=null && (action.equals("Recall") || action.equals("Skip Queue"))) {
            qpal.util.QueueVoice.announce(row.number(),boarding,station,action.equals("Skip Queue"));
        }
    }

    private void manage(String action) {
        if (loading || acting) return;
        var row = selectedRow();
        if (!action.equals("Add") && row == null) {
            qpal.components.AppDialogs.showMessageDialog(this,"Select a queue row first."); return;
        }
        if (action.equals("Print")) { act("Print Ticket"); return; }
        acting = true;
        if (action.equals("Delete")) {
            if (!QueueBookingDialog.confirmDelete(this,row)) { acting=false; return; }
            qpal.util.UiTask.run(() -> { new qpal.dao.QueueBookingDao().cancel(row.bookingId()); return true; }, result -> {
                acting=false; refreshData();
            }, ex -> { acting=false; qpal.components.AppDialogs.showMessageDialog(this,ex.getMessage(),"Unable to Delete",JOptionPane.WARNING_MESSAGE); refreshData(); });
        } else if (action.equals("Add")) {
            qpal.util.UiTask.run(() -> new qpal.dao.BookingDao().availableTrips(), trips -> {
                try { QueueBookingDialog.show(this,null,trips,java.util.List.of()); }
                finally { acting=false; refreshData(); }
            }, ex -> { acting=false; qpal.components.AppDialogs.showMessageDialog(this,ex.getMessage(),"Unable to Add",JOptionPane.WARNING_MESSAGE); });
        } else {
            qpal.util.UiTask.run(() -> new qpal.dao.QueueBookingDao().passengers(row.bookingId()), people -> {
                try { QueueBookingDialog.show(this,row,java.util.List.of(),people); }
                finally { acting=false; refreshData(); }
            }, ex -> { acting=false; qpal.components.AppDialogs.showMessageDialog(this,ex.getMessage(),"Unable to Edit",JOptionPane.WARNING_MESSAGE); });
        }
    }

    private JPanel stat(String title, JLabel icon, Color tint) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 8));
        // Add your summary ImageIcons to these labels when ready.
        icon.setOpaque(true);
        icon.setBackground(tint);
        icon.setPreferredSize(new Dimension(42, 42));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel top = transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.add(icon);
        panel.add(top, BorderLayout.NORTH);
        JPanel content = transparent(new GridLayout(2, 1));
        content.add(label(title, 10, MUTED, false));
        JLabel value = label("—", 30, new Color(166, 0, 44), true);
        stats.add(value);
        content.add(value);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel detail(String title, String value) {
        JPanel panel = transparent(new GridLayout(2, 1));
        panel.add(label(title, 10, MUTED, false));
        JLabel field = label(value, 12, Color.BLACK, true);
        detailLabels.put(title, field);
        panel.add(field);
        return panel;
    }

    private JTable queueTable() { return queueTable(queueModel); }

    private JTable queueTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override protected void processMouseEvent(MouseEvent event) {
                if (event.getID()==MouseEvent.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(event)
                        && rowAtPoint(event.getPoint())>=0 && rowAtPoint(event.getPoint())==getSelectedRow()) {
                    clearSelection();
                    return;
                }
                super.processMouseEvent(event);
            }
        };
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 3));
        table.setSelectionBackground(new Color(255, 235, 241));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(237, 240, 245));
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, center);
        return table;
    }

    private JButton unavailableAction(String title, boolean primary) {
        JButton button = button(title, primary);
        actionButtons.add(button);
        button.addActionListener(e -> act(button.getText()));
        return button;
    }

    private static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(primary ? RED : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : new Color(50, 63, 83));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? RED : new Color(210, 217, 228)),
                new EmptyBorder(6, 10, 6, 10)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JPanel card() { return new AdminCard(12); }
    private static JLabel sectionTitle(String text) {
        JLabel label = label(text, 13, new Color(30, 37, 48), true);
        return label;
    }

    private static JLabel label(String text, int size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }
}
