package qpal.view.Admin;

import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.BusDao;
import qpal.dao.RouteDao;
import qpal.dao.TripDao;
import qpal.model.Bus;
import qpal.model.Route;
import qpal.model.Trip;

public class EditTripPanel {

    private TripDao tripDao;
    private BusDao busDao;
    private RouteDao routeDao;
    private AdminRouteSchedPanel parent;
    private int tripID;

    public EditTripPanel(
            TripDao tripDao,
            BusDao busDao,
            RouteDao routeDao,
            AdminRouteSchedPanel parent,
            int tripID) {

        this.tripDao = tripDao;
        this.busDao = busDao;
        this.routeDao = routeDao;
        this.parent = parent;
        this.tripID = tripID;
    }

    public void showDialog() {

        Trip trip = tripDao.getTrip(tripID);

        if (trip == null) {
            qpal.components.AppDialogs.showMessageDialog(
                    parent, "Trip not found.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        panel.setPreferredSize(new Dimension(360, 590));

        JLabel title = new JLabel("Edit Trip Details");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(225, 29, 72));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(7));

        JLabel lblSubtitle = new JLabel("Update the selected trip and schedule.");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 100, 100));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblSubtitle);
        panel.add(Box.createVerticalStrut(20));

        JLabel lblBus = new JLabel("Bus");
        lblBus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblBus.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblBus);

        panel.add(Box.createVerticalStrut(7));

        JComboBox<Bus> cmbBus = new JComboBox<>();

        List<Bus> buses = busDao.getAllBuses();

        for (Bus bus : buses) {
            cmbBus.addItem(bus);

            if (bus.getBusID() == trip.getBusID()) {
                cmbBus.setSelectedItem(bus);
            }
        }

        cmbBus.setRenderer(
                new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        super.getListCellRendererComponent(
                                list, value, index, isSelected, cellHasFocus);

                        if (value instanceof Bus) {
                            setText(((Bus) value).getBusNumber());
                        }

                        return this;
                    }
                });

        cmbBus.setMaximumSize(new Dimension(300, 38));
        cmbBus.setBackground(new Color(220, 220, 220));
        cmbBus.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(cmbBus);

        panel.add(Box.createVerticalStrut(14));

        JLabel lblRoute = new JLabel("Route");
        lblRoute.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblRoute.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblRoute);
        panel.add(Box.createVerticalStrut(7));

        JComboBox<Route> cmbRoute = new JComboBox<>();
        cmbRoute.setMaximumRowCount(10);
        cmbRoute.setRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        super.getListCellRendererComponent(
                                list, value, index, isSelected, cellHasFocus);

                        if (value instanceof Route) {

                            Route route = (Route) value;
                            String routeName = route.getOrigin() + " - " + route.getDestination();
                            setText(routeName);
                        }

                        return this;
                    }
                });

        List<Route> routes = routeDao.getAllRoutes();

        for (Route route : routes) {

            cmbRoute.addItem(route);

            if (route.getRouteID() == trip.getRouteID()) {
                cmbRoute.setSelectedItem(route);
            }
        }

        cmbRoute.setMaximumSize(new Dimension(300, 38));
        cmbRoute.setBackground(new Color(220, 220, 220));
        cmbRoute.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(cmbRoute);

        panel.add(Box.createVerticalStrut(14));

        JLabel lblFare = new JLabel("Route Fare (PHP)");
        lblFare.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblFare.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblFare);
        panel.add(Box.createVerticalStrut(7));

        JTextField txtFare = new JTextField();
        txtFare.setBackground(new Color(220, 220, 220));
        txtFare.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        txtFare.setMaximumSize(new Dimension(300, 38));
        txtFare.setAlignmentX(Component.LEFT_ALIGNMENT);
        Route initialRoute = (Route) cmbRoute.getSelectedItem();

        if (initialRoute != null && initialRoute.getFare() > 0) {

            txtFare.setText(
                    java.math.BigDecimal.valueOf(initialRoute.getFare())
                            .stripTrailingZeros()
                            .toPlainString());
        }

        cmbRoute.addActionListener(
                e -> {
                    Route route = (Route) cmbRoute.getSelectedItem();
                    txtFare.setText("");

                    if (route != null && route.getFare() > 0) {

                        txtFare.setText(
                                java.math.BigDecimal.valueOf(route.getFare())
                                        .stripTrailingZeros()
                                        .toPlainString());
                    }
                });

        panel.add(txtFare);
        panel.add(Box.createVerticalStrut(14));
        JLabel lblDate = new JLabel("Departure Date");
        lblDate.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblDate);
        panel.add(Box.createVerticalStrut(7));

        JDateChooser txtDate = new JDateChooser();
        txtDate.setDateFormatString("yyyy-MM-dd");
        ((JTextField) txtDate.getDateEditor().getUiComponent()).setEditable(false);
        if (trip.getDepartureDate() != null) {
            try {
                txtDate.setDate(java.sql.Date.valueOf(trip.getDepartureDate()));
            } catch (IllegalArgumentException ex) {
                // Require a new selection for an invalid legacy date.
                txtDate.setDate(null);
            }
        }

        txtDate.setMaximumSize(new Dimension(300, 38));
        txtDate.setBackground(new Color(220, 220, 220));
        txtDate.setPreferredSize(new Dimension(300, 38));
        txtDate.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(txtDate);

        panel.add(Box.createVerticalStrut(14));

        JLabel lblTime = new JLabel("Departure Time");
        lblTime.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTime.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblTime);
        panel.add(Box.createVerticalStrut(7));

        JComboBox<java.time.LocalTime> cmbTime =
                ManagementComboBoxes.departureTime(trip.getDepartureTime());
        panel.add(cmbTime);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblStatus = new JLabel("Status");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblStatus);

        panel.add(Box.createVerticalStrut(7));

        JComboBox<String> cmbStatus =
                new JComboBox<>(new String[] {"Scheduled", "Boarding", "Departed", "Cancelled"});

        cmbStatus.setSelectedItem(trip.getStatus());
        cmbStatus.setMaximumSize(new Dimension(300, 38));
        cmbStatus.setBackground(new Color(220, 220, 220));
        cmbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(cmbStatus);

        panel.add(Box.createVerticalStrut(25));

        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(new Color(50, 170, 230));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBackground(new Color(240, 0, 55));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.setMaximumSize(new Dimension(300, 42));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        buttons.add(btnSave);
        buttons.add(btnCancel);

        panel.add(buttons);

        JDialog dialog =
                new JDialog(
                        SwingUtilities.getWindowAncestor(parent),
                        "Edit Trip",
                        Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setUndecorated(true);
        dialog.setContentPane(AdminFormStyle.frame(panel));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);

        btnCancel.addActionListener(e -> dialog.dispose());
        cmbStatus.addActionListener(
                e -> {
                    if ("Departed".equals(trip.getStatus())
                            && !"Departed".equals(cmbStatus.getSelectedItem())) {
                        cmbStatus.setSelectedItem("Departed");
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Trip is departed.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                });
        ManagementComboBoxes.validateBusOnSelection(cmbBus, dialog);

        btnSave.addActionListener(
                e -> {
                    if (!ManagementComboBoxes.validateBusSelection(cmbBus, dialog)) return;

                    Bus selectedBus = (Bus) cmbBus.getSelectedItem();
                    Route selectedRoute = (Route) cmbRoute.getSelectedItem();

                    String departureDate =
                            txtDate.getDate() == null
                                    ? ""
                                    : new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                            .format(txtDate.getDate());
                    java.time.LocalTime departureTime =
                            (java.time.LocalTime) cmbTime.getSelectedItem();
                    String status = cmbStatus.getSelectedItem().toString();

                    if (selectedBus == null) {
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Please select a bus.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (selectedRoute == null) {
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Please select a route.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (departureDate.isEmpty()) {
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Please select a departure date.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (departureTime == null) {
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Departure Time needs an input.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (!java.time.LocalDate.parse(departureDate)
                            .atTime(departureTime)
                            .isAfter(
                                    java.time.LocalDateTime.now(
                                            java.time.ZoneId.of("Asia/Manila")))) {
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Cannot save this trip because its departure date or time has"
                                    + " already passed. Please choose a future departure.",
                                "Departure Already Passed",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    Trip updatedTrip = new Trip();

                    updatedTrip.setTripID(tripID);
                    updatedTrip.setBusID(selectedBus.getBusID());
                    updatedTrip.setRouteID(selectedRoute.getRouteID());
                    updatedTrip.setDepartureDate(departureDate);
                    updatedTrip.setDepartureTime(
                            departureTime.format(
                                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                    updatedTrip.setAvailableSeats(selectedBus.getSeatCapacity());
                    updatedTrip.setStatus(status);

                    java.math.BigDecimal fare;

                    try {

                        fare = new java.math.BigDecimal(txtFare.getText().trim());

                        if (fare.compareTo(java.math.BigDecimal.ZERO) <= 0
                                || fare.compareTo(new java.math.BigDecimal("99999999")) > 0
                                || fare.stripTrailingZeros().scale() > 0) {

                            qpal.components.AppDialogs.showMessageDialog(
                                    dialog,
                                    "Enter a positive whole-peso fare (no decimals).",
                                    "Warning",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                    } catch (NumberFormatException ex) {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Fare needs a valid amount.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    boolean success;
                    try {
                        success = tripDao.saveTripWithFare(updatedTrip, fare, true);
                    } catch (TripDao.DepartedTripException ex) {
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog, ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    } catch (TripDao.ScheduleConflictException ex) {
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                ex.getMessage(),
                                "Schedule Conflict",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (success) {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Trip updated successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);

                        dialog.dispose();

                        parent.loadTrips();

                    } else {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Unable to update trip. Booked trips must keep their bus, route and"
                                    + " departure, and cannot be cancelled here.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

        dialog.setVisible(true);
    }
}
