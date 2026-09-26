package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.BusDao;
import qpal.model.Bus;

public class AddBusPanel {

    private BusDao busDao;
    private AdminBusPanel parent;

    public AddBusPanel(BusDao busDao, AdminBusPanel parent) {

        this.busDao = busDao;
        this.parent = parent;

    }

    public void showDialog() {

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(25,30,25,30));
        panel.setPreferredSize(new Dimension(360,381));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Add Bus");
        title.setFont(new Font("SansSerif", Font.BOLD,18));
        title.setForeground(new Color(225,29,72));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(7));

        JLabel lblSubtitle = new JLabel("Enter the details for a new bus.");
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

        JTextField txtBusNumber = new JTextField();
        txtBusNumber.setBackground(new Color(220,220,220));
        txtBusNumber.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        txtBusNumber.setPreferredSize(new Dimension(300,34));
        txtBusNumber.setMinimumSize(new Dimension(300,34));
        txtBusNumber.setMaximumSize(new Dimension(300,34));
        txtBusNumber.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(txtBusNumber);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblSeatCapacity = new JLabel("Seat Capacity");
        lblSeatCapacity.setFont(new Font("SansSerif", Font.BOLD,13));
        lblSeatCapacity.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblSeatCapacity);
        panel.add(Box.createVerticalStrut(7));

        JComboBox<Integer> cmbSeatCapacity = ManagementComboBoxes.seatCapacity(-1);
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
        cmbStatus.setPreferredSize(new Dimension(300,30));
        cmbStatus.setMinimumSize(new Dimension(300,30));
        cmbStatus.setMaximumSize(new Dimension(300,30));
        cmbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(cmbStatus);
        panel.add(Box.createVerticalStrut(25));

        JButton btnAdd = new JButton("Add Bus");
        btnAdd.setBackground(new Color(0,190,100));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBackground(new Color(240,0,55));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel buttons = new JPanel(new GridLayout(1,2,12,0));
        buttons.setOpaque(false);
        buttons.setPreferredSize(new Dimension(300,32));
        buttons.setMinimumSize(new Dimension(300,32));
        buttons.setMaximumSize(new Dimension(300,32));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        buttons.add(btnAdd);
        buttons.add(btnCancel);

        panel.add(buttons);

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Add Bus",
                Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setUndecorated(true);
        dialog.setContentPane(AdminFormStyle.frame(panel));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);

        btnCancel.addActionListener(e -> dialog.dispose());

                btnAdd.addActionListener(e -> {

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
                qpal.components.AppDialogs.showMessageDialog(dialog, "Choose a seat capacity from 20 to 50.",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if(busDao.checkBusNumber(busNumber)) {

                qpal.components.AppDialogs.showMessageDialog(
                        dialog,
                        "Bus Number already exists.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);

                return;
            }

            Bus bus = new Bus();

            bus.setBusNumber(busNumber);
            bus.setSeatCapacity(seatCapacity);

            // New bus starts with all seats available
            bus.setAvailableSeats(seatCapacity);

            bus.setBusStatus(status);

            boolean success = busDao.addBus(bus);

            if(success) {

                qpal.components.AppDialogs.showMessageDialog(
                        dialog,
                        "Bus added successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();

                parent.loadBuses();

            } else {

                qpal.components.AppDialogs.showMessageDialog(
                        dialog,
                        "Failed to add bus.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

            }

        });

        dialog.setVisible(true);

    }

}
