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
import qpal.dao.BusDao;
import qpal.dao.RouteDao;
import qpal.dao.TripDao;
import qpal.model.Trip;

public class AdminRouteSchedPanel extends JPanel {
    private final boolean readOnly;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> cmbStatus;
    private String appliedSearch = "";
    private final JLabel[] tripCounts = new JLabel[4];

    private final List<Object[]> trips = new ArrayList<>();

    private int currentPage = 1;
    private boolean loading;
    private final int rowsPerPage = 10;

    private JLabel lblInfo;

    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnOne;
    private JButton btnTwo;

    private TripDao tripDao = new TripDao();
    private BusDao busDao = new BusDao();
    private RouteDao routeDao = new RouteDao();

    public AdminRouteSchedPanel() {
        this(false);
    }

    public AdminRouteSchedPanel(boolean readOnly) {
        this.readOnly = readOnly;

        setLayout(new BorderLayout());
        setBackground(new Color(245,245,245));
        setBorder(new EmptyBorder(20,25,20,25));

        add(createHeader(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(createTripSummary(), BorderLayout.NORTH);
        body.add(createContent(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        loadTrips();

        Timer timer = new Timer(5000, e -> loadTrips());
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) { timer.start(); loadTrips(); }
                else timer.stop();
            }
        });

    }

    private JPanel createHeader() {

        JPanel panel = new JPanel();

        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Route & Schedule Management");
        title.setFont(new Font("SansSerif", Font.BOLD,30));
        title.setForeground(new Color(228,0,70));

        JLabel subtitle = new JLabel("Manage routes, schedules, and assigned buses.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN,13));
        subtitle.setForeground(new Color(120,120,120));

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(18));

        return panel;

    }

    private JPanel createTripSummary() {
        JPanel summary = new JPanel(new GridLayout(1, 4, 14, 0));
        summary.setOpaque(false);
        summary.setPreferredSize(new Dimension(0, 150));
        String[] titles = {"SCHEDULED TRIPS", "BOARDING TRIPS", "DEPARTED TRIPS", "CANCELLED TRIPS"};
        for (int i = 0; i < titles.length; i++) {
            JPanel card = new AdminCard(18);
            card.setLayout(new GridBagLayout());

            JLabel title = new JLabel(titles[i]);
            title.setFont(new Font("SansSerif", Font.PLAIN, 10));
            title.setForeground(new Color(100,100,100));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            JPanel content = new JPanel(new BorderLayout(0, 8));
            content.setOpaque(false);
            content.add(title, BorderLayout.NORTH);

            tripCounts[i] = new JLabel("—");
            tripCounts[i].setFont(new Font("SansSerif", Font.BOLD, 32));
            tripCounts[i].setForeground(new Color(170,0,45));
            tripCounts[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(tripCounts[i], BorderLayout.CENTER);

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1;
            card.add(content, constraints);
            summary.add(card);
        }
        return summary;
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
        if (!readOnly) south.add(createActionButtons(), BorderLayout.SOUTH);

        panel.add(south, BorderLayout.SOUTH);

        return panel;

    }

        private JPanel createTopPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);

        panel.setBorder(new EmptyBorder(0,0,10,0));

        JLabel lblTitle = new JLabel("Trip Details");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD,22));
        lblTitle.setForeground(new Color(40,40,40));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,0,6));
        left.setOpaque(false);
        left.add(lblTitle);

        panel.add(left, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4));
        controls.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(280,34));
        searchField.setFont(new Font("SansSerif", Font.PLAIN,13));
        searchField.setMargin(new Insets(0,10,0,10));

        JButton btnSearch = new JButton("Search");
        btnSearch.setPreferredSize(new Dimension(100,34));
        btnSearch.setFont(new Font("SansSerif", Font.BOLD,13));
        btnSearch.setBackground(new Color(225,29,72));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setBorder(BorderFactory.createEmptyBorder());

        btnSearch.addActionListener(e -> searchTrips());
        AdminFormStyle.searchOnEnter(searchField, btnSearch);

            cmbStatus = new JComboBox<>(new String[]{
                "All Statuses", "Scheduled", "Boarding", "Departed", "Cancelled"
        });
        AdminFormStyle.tableFilter(cmbStatus);
        cmbStatus.setFont(new Font("SansSerif",Font.PLAIN,13));
        cmbStatus.setBackground(Color.WHITE);
        cmbStatus.setFocusable(false);
        cmbStatus.setToolTipText("Filter by status");
        cmbStatus.addActionListener(e -> {

            currentPage = 1;
            loadPage();
        });
    controls.add(searchField);
        controls.add(cmbStatus);
        controls.add(btnSearch);

        panel.add(controls, BorderLayout.EAST);

        return panel;

    }

    private JScrollPane createTable() {

        String[] columns = {
                "Trip ID",
                "Bus",
                "Route",
                "Departure Date",
                "Departure Time",
                "Fare",
                "Booked Seats",
                "Status"
        };

        model = new DefaultTableModel(columns,0) {

            @Override
            public boolean isCellEditable(int row,int column) {
                return false;
            }

        };

        table = new JTable(model);

        table.setFont(new Font("SansSerif", Font.PLAIN,12));
        table.setRowHeight(36);

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        table.setGridColor(new Color(236,236,236));
        table.setIntercellSpacing(new Dimension(0,0));
        table.setRowMargin(0);

        table.setSelectionBackground(new Color(240,247,255));
        table.setSelectionForeground(Color.BLACK);

        table.setDefaultRenderer(Object.class,new ModernTableCellRenderer());

        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);
        table.getColumnModel().getColumn(7).setPreferredWidth(110);

        JTableHeader header = table.getTableHeader();

        header.setPreferredSize(new Dimension(0,34));
        header.setFont(new Font("SansSerif", Font.BOLD,12));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(90,90,90));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(BorderFactory.createEmptyBorder());

        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scroll.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        return scroll;

    }

    public void loadTrips() {
        if (loading) return;
        loading = true;
        qpal.util.UiTask.run(() -> tripDao.getAllTrips(), tripList -> {
            try { displayTrips(tripList); }
            finally { loading = false; }
        }, ex -> { loading = false; });
    }

    private void displayTrips(List<Trip> tripList) {

        Object selectedId = table.getSelectedRow() < 0 ? null
                : table.getValueAt(table.getSelectedRow(), 0);

        trips.clear();
        int[] counts = new int[4];

        for (Trip trip : tripList) {
            String status = trip.getStatus();
            if ("Scheduled".equalsIgnoreCase(status)) counts[0]++;
            else if ("Boarding".equalsIgnoreCase(status)) counts[1]++;
            else if ("Departed".equalsIgnoreCase(status)) counts[2]++;
            else if ("Cancelled".equalsIgnoreCase(status)) counts[3]++;

            trips.add(new Object[]{

                    trip.getTripID(),
                    trip.getBusName(),
                    trip.getOrigin() + " → " + trip.getDestination(),
                    trip.getDepartureDate(),
                    trip.getDepartureTime(),
                    String.format("₱%.2f", trip.getFare()),
                    (trip.getSeatCapacity() - trip.getAvailableSeats()) + "/" + trip.getSeatCapacity(),
                    trip.getStatus()

            });

        }

        for (int i = 0; i < counts.length; i++) {
            tripCounts[i].setText(String.valueOf(counts[i]));
        }

        trips.sort(java.util.Comparator.comparingInt(row -> {
            switch (String.valueOf(row[7])) {
                case "Scheduled": return 0;
                case "Boarding": return 1;
                case "Cancelled": return 2;
                case "Departed": return 3;
                default: return 4;
            }
        }));

        currentPage = Math.max(1, Math.min(currentPage,
                (getVisibleRows().size() + rowsPerPage - 1) / rowsPerPage));

        if(model != null){

            loadPage();
            if (selectedId != null) {
                for (int row = 0; row < table.getRowCount(); row++) {
                    if (selectedId.equals(table.getValueAt(row, 0))) {
                        table.setRowSelectionInterval(row, row);
                        break;
                    }
                }
            }

        }

    }

    private void searchTrips() {
        if (!AdminFormStyle.validateSearch(searchField)) return;

        appliedSearch = searchField.getText().trim().toLowerCase();
        currentPage = 1;
        loadPage();
    }

    private List<Object[]> getVisibleRows() {

        List<Object[]> result = new ArrayList<>();
        String search = appliedSearch;

        for(Object[] row : trips) {

            if(row[0].toString().toLowerCase().contains(search) || row[1].toString().toLowerCase().contains(search) || row[2].toString().toLowerCase().contains(search) || row[3].toString().toLowerCase().contains(search) || row[4].toString().toLowerCase().contains(search)) {

                String selectedStatus = cmbStatus.getSelectedItem().toString();

                if(selectedStatus.equals("All Statuses") || row[7].toString().equalsIgnoreCase(selectedStatus)) {

                    result.add(row);
                }
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

            lblInfo.setText(
                    "Showing "
                    + (visibleRows.size() == 0 ? 0 : start + 1)
                    + " to "
                    + end
                    + " of "
                    + visibleRows.size()
                    + " trips");

        }

        updatePaginationButtons();

    }

        private void updatePaginationButtons() {

        int totalPages =
                (int) Math.ceil(getVisibleRows().size() / (double) rowsPerPage);

        if(btnPrev != null)
            btnPrev.setEnabled(currentPage > 1);

        if(btnNext != null)
            btnNext.setEnabled(currentPage < totalPages);

        if(btnOne != null){

            btnOne.setBackground(
                    currentPage == 1
                            ? new Color(225,29,72)
                            : Color.WHITE);

            btnOne.setForeground(
                    currentPage == 1
                            ? Color.WHITE
                            : new Color(80,80,80));

        }

        if(btnTwo != null){

            btnTwo.setVisible(totalPages >= 2);

            btnTwo.setBackground(
                    currentPage == 2
                            ? new Color(225,29,72)
                            : Color.WHITE);

            btnTwo.setForeground(
                    currentPage == 2
                            ? Color.WHITE
                            : new Color(80,80,80));

        }

    }

    private JPanel createBottomPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);

        panel.setBorder(new EmptyBorder(18,0,0,0));

        lblInfo = new JLabel();

        lblInfo.setFont(new Font("SansSerif", Font.PLAIN,12));
        lblInfo.setForeground(new Color(130,130,130));

        panel.add(lblInfo, BorderLayout.WEST);

        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));

        pagination.setOpaque(false);

        btnPrev = new JButton("<");
        btnOne = new JButton("1");
        btnTwo = new JButton("2");
        btnNext = new JButton(">");

        JButton[] buttons = {
                btnPrev,
                btnOne,
                btnTwo,
                btnNext
        };

        for(JButton b : buttons){

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

            int totalPages =
                    (int) Math.ceil(getVisibleRows().size() / (double) rowsPerPage);

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

        btnAdd.addActionListener(e -> addTrip());
        btnEdit.addActionListener(e -> editTrip());
        btnPrint.addActionListener(e -> printTrips());
        btnDelete.addActionListener(e -> deleteTrip());

        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnPrint);
        panel.add(btnDelete);

        return panel;

    }

    private void addTrip() {

        AddTripPanel panel = new AddTripPanel(
                tripDao,
                busDao,
                routeDao,
                this);

        panel.showDialog();

    }

    private void editTrip() {

        int selectedRow = table.getSelectedRow();

        if(selectedRow == -1){

            qpal.components.AppDialogs.showMessageDialog(
                    this,
                    "No selected trip to edit.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);

            return;

        }

        int id = (int) model.getValueAt(selectedRow,0);

        EditTripPanel panel = new EditTripPanel(
                tripDao,
                busDao,
                routeDao,
                this,
                id);

        panel.showDialog();

    }

    private void deleteTrip() {

        int selectedRow = table.getSelectedRow();

        if(selectedRow == -1){

            qpal.components.AppDialogs.showMessageDialog(
                    this,
                    "No selected trip.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);

            return;

        }

        int id = (int) model.getValueAt(selectedRow,0);

        String trip =
                model.getValueAt(selectedRow,2).toString()
                + " | "
                + model.getValueAt(selectedRow,3).toString()
                + " "
                + model.getValueAt(selectedRow,4).toString();

        int confirm = qpal.components.AppDialogs.showConfirmDialog(
                this,
                "Permanently delete this trip?\n\n" + trip
                + "\n\nIts bookings, passengers, seats, queues and payment/revenue records\nwill also be deleted. This cannot be undone.",
                "Delete Trip",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if(confirm == JOptionPane.YES_OPTION){

            boolean success = tripDao.deleteTrip(id);

            if(success){

                qpal.components.AppDialogs.showMessageDialog(
                        this,
                        "Trip deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                loadTrips();

            }else{

                qpal.components.AppDialogs.showMessageDialog(
                        this,
                        "Failed to delete trip.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

            }

        }

    }

    private void printTrips() {

        try {

            boolean complete = table.print();

            if(complete){
                qpal.dao.ActivityLogDao.recordActivity("Route & Schedule", "Print", "Printed the route & schedule table.");

                qpal.components.AppDialogs.showMessageDialog(
                        this,
                        "Table printed successfully.",
                        "Print",
                        JOptionPane.INFORMATION_MESSAGE);

            }else{

                qpal.components.AppDialogs.showMessageDialog(
                        this,
                        "Printing was cancelled.",
                        "Print",
                        JOptionPane.WARNING_MESSAGE);

            }

        } catch (PrinterException e) {

            qpal.components.AppDialogs.showMessageDialog(
                    this,
                    "Unable to print the table.",
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();

        }

    }

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setPreferredSize(new Dimension(145,40));

        button.setBackground(color);
        button.setForeground(Color.WHITE);

        button.setFont(new Font("SansSerif", Font.BOLD,13));

        button.setFocusPainted(false);

        button.setBorder(BorderFactory.createEmptyBorder());

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;

    }

}
