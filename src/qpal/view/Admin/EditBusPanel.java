package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.BusDao;
import qpal.model.Bus;

public class EditBusPanel {

    private BusDao busDao;
    private AdminBusPanel parent;
    private int id;

    public EditBusPanel(BusDao busDao, AdminBusPanel parent, int id) {

        this.busDao = busDao;
        this.parent = parent;
        this.id = id;

    }

    public void showDialog() {

        Bus bus = busDao.getBus(id);

        if(bus == null) {

            qpal.components.AppDialogs.showMessageDialog(
                    parent,
                    "Bus not found.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);

            return;

        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25,30,25,30));
        panel.setPreferredSize(new Dimension(360,454));

        JLabel title = new JLabel("Edit Bus Details");

        title.setFont(new Font("SansSerif", Font.BOLD,18));
        title.setForeground(new Color(225,29,72));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(7));

        JLabel lblSubtitle = new JLabel("Update the selected bus details.");
        lblSubtitle.setFont(new Font("SansSerif",Font.PLAIN,12));
        lblSubtitle.setForeground(new Color(100,100,100));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblSubtitle);
        panel.add(Box.createVerticalStrut(20));

        JLabel lblBusNumber = new JLabel("Bus Number");
        lblBusNumber.setFont(new Font("SansSerif", Font.BOLD,13));
        lblBusNumber.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblBusNumber);
        panel.add(Box.createVerticalStrut(7));

        JTextField txtBusNumber = new JTextField(bus.getBusNumber());
        txtBusNumber.setBackground(new Color(220,220,220));
        txtBusNumber.setEditable(false);
        txtBusNumber.setForeground(new Color(30,30,30));
        txtBusNumber.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        txtBusNumber.setMaximumSize(new Dimension(300,38));
        txtBusNumber.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(txtBusNumber);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblSeatCapacity = new JLabel("Seat Capacity");
        lblSeatCapacity.setFont(new Font("SansSerif", Font.BOLD,13));
        lblSeatCapacity.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblSeatCapacity);
        panel.add(Box.createVerticalStrut(7));

        JComboBox<Integer> cmbSeatCapacity = ManagementComboBoxes.seatCapacity(bus.getSeatCapacity());
        if (cmbSeatCapacity.getSelectedIndex() < 0) {
            cmbSeatCapacity.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean selected, boolean focused) {
                    super.getListCellRendererComponent(list, value, index, selected, focused);
                    if (value == null) setText("Keep current (" + bus.getSeatCapacity() + " seats)");
                    return this;
                }
            });
        }
        panel.add(cmbSeatCapacity);
        panel.add(Box.createVerticalStrut(14));

                JLabel lblStatus = new JLabel("Bus Status");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD,13));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblStatus);
        panel.add(Box.createVerticalStrut(7));

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
                "Available",
                "Maintenance",
                "Inactive"
        });

        cmbStatus.setBackground(new Color(220,220,220));
        cmbStatus.setMaximumSize(new Dimension(300,38));
        cmbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbStatus.setSelectedItem(bus.getBusStatus());

        panel.add(cmbStatus);
        panel.add(Box.createVerticalStrut(25));

        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(new Color(50,170,230));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setFont(new Font("SansSerif",Font.BOLD,13));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBackground(new Color(240,0,55));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setFont(new Font("SansSerif",Font.BOLD,13));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel buttons = new JPanel(new GridLayout(1,2,12,0));
        buttons.setOpaque(false);
        buttons.setMaximumSize(new Dimension(300,42));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        buttons.add(btnSave);
        buttons.add(btnCancel);

        panel.add(buttons);

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Edit Bus",
                Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setUndecorated(true);
        dialog.setContentPane(AdminFormStyle.frame(panel));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);

        btnCancel.addActionListener(e -> {

            dialog.dispose();

        });

                btnSave.addActionListener(e -> {

            String busNumber = txtBusNumber.getText().trim();
            Integer seatCapacity = (Integer)cmbSeatCapacity.getSelectedItem();
            String status = cmbStatus.getSelectedItem().toString();

            if(busNumber.isEmpty()) {

                qpal.components.AppDialogs.showMessageDialog(
                        dialog,
                        "Bus Number needs an input.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);

                return;
            }

            if (seatCapacity == null) {
                seatCapacity = bus.getSeatCapacity();
            }
            if(!busNumber.equals(bus.getBusNumber())
                    && busDao.checkBusNumber(busNumber)) {

                qpal.components.AppDialogs.showMessageDialog(
                        dialog,
                        "Bus Number already exists.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);

                return;

            }

            qpal.model.Bus updated = new qpal.model.Bus();
            updated.setBusID(bus.getBusID());
            updated.setBusNumber(busNumber);
            updated.setSeatCapacity(seatCapacity);
            updated.setAvailableSeats(seatCapacity == bus.getSeatCapacity() ? bus.getAvailableSeats() : seatCapacity);
            updated.setBusStatus(status);

            boolean success;
            try { success = busDao.updateBusDetails(updated); }
            catch (java.sql.SQLException ex) {
                String message = ex.getErrorCode() == 1062 ? "Bus Number already exists."
                        : "Unable to update bus: " + ex.getMessage();
                qpal.components.AppDialogs.showMessageDialog(dialog, message, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(success) {

                qpal.components.AppDialogs.showMessageDialog(
                        dialog,
                        "Bus updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();

                parent.loadBuses();

            } else {

                qpal.components.AppDialogs.showMessageDialog(
                        dialog,
                        "Unable to update bus. Please reload the bus list and try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

            }

        });

        dialog.setVisible(true);

    }

}
