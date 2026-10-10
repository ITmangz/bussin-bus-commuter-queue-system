package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PaymentPanel extends JPanel {

    private JPanel cashCard;
    private JPanel ewalletCard;
    private JPanel cardCard;

    private final PaymentDetailsPanel ewalletDetails = new PaymentDetailsPanel(true);
    private final PaymentDetailsPanel cardDetails = new PaymentDetailsPanel(false);

    public JLabel getEwalletQrLabel() {
        return ewalletDetails.getPaymentImageLabel();
    }

    public JLabel getCardImageLabel() {
        return cardDetails.getPaymentImageLabel();
    }

    private boolean enteringDetails;
    private JLabel paymentTitle, paymentSubtitle;

    private void showPaymentChoices() {
        enteringDetails = false;
        ewalletDetails.setVisible(false);
        cardDetails.setVisible(false);
        cashCard.setVisible(true);
        ewalletCard.setVisible(true);
        cardCard.setVisible(true);
        paymentTitle.setText("Select Payment");
        paymentSubtitle.setText("Select your preferred payment method.");
    }

    private void showPaymentDetails() {
        enteringDetails = true;
        cashCard.setVisible(false);
        ewalletCard.setVisible(false);
        cardCard.setVisible(false);
        boolean wallet = selectedPayment.equals("E-Wallet");
        ewalletDetails.setVisible(wallet);
        cardDetails.setVisible(!wallet);
        paymentTitle.setText(wallet ? "E-Wallet Payment" : "Card Payment");
        paymentSubtitle.setText(
                "Enter your payment reference. Staff will confirm payment at the counter.");
    }

    private String selectedPayment = "";
    private String bookingReference = newReference();

    private static String newReference() {
        return "BK-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 26);
    }

    public PaymentPanel(TripDetailsPanel tripDetailsPanel, PassengerDetailsPanel passengerPanel) {

        setSize(1000, 650);
        setLayout(null);

        JPanel paymentpanel = new JPanel(null);
        paymentpanel.setBounds(0, 0, 1000, 650);
        paymentpanel.setBackground(new Color(248, 248, 248));
        add(paymentpanel);

        // ================ TOP =================//

        JPanel toppanel = new JPanel(null);
        toppanel.setBounds(0, 0, 1000, 100);
        toppanel.setBackground(new Color(225, 0, 45));
        paymentpanel.add(toppanel);

        JLabel bussinlogo = new JLabel();
        bussinlogo.setBounds(35, 20, 120, 55);

        bussinlogo.setIcon(
                new ImageIcon(
                        new ImageIcon("resources/icons/bussinlogokiosk.png")
                                .getImage()
                                .getScaledInstance(120, 55, Image.SCALE_SMOOTH)));
        toppanel.add(bussinlogo);

        JLabel datelabel = new JLabel();
        datelabel.setBounds(370, 27, 260, 20);
        datelabel.setHorizontalAlignment(SwingConstants.CENTER);
        datelabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        datelabel.setForeground(Color.WHITE);
        toppanel.add(datelabel);

        JLabel timelabel = new JLabel();
        timelabel.setBounds(370, 47, 260, 20);
        timelabel.setHorizontalAlignment(SwingConstants.CENTER);
        timelabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timelabel.setForeground(Color.WHITE);
        toppanel.add(timelabel);

        JPanel stepspanel = KioskStepsPanel.create(5);
        stepspanel.setBounds(0, 100, 1000, 75);
        paymentpanel.add(stepspanel);

        // ================ TITLE =================//

        JLabel title = new JLabel("Select Payment");
        title.setBounds(70, 192, 860, 44);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(225, 0, 45));
        paymentpanel.add(title);
        paymentTitle = title;

        JLabel subtext = new JLabel("Select your preferred payment method.");
        subtext.setBounds(70, 238, 860, 25);
        subtext.setHorizontalAlignment(SwingConstants.CENTER);
        subtext.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtext.setForeground(new Color(100, 100, 100));
        paymentpanel.add(subtext);
        paymentSubtitle = subtext;

        // ================ PAYMENT CARDS =================//

        cashCard = createPaymentCard("Cash", "Pay at the counter", "resources/icons/cashpaymentic.png", 135);

        ewalletCard =
                createPaymentCard(
                        "E-Wallet", "Pay at the counter", "resources/icons/e-cashpaymentic.png", 387);

        cardCard = createPaymentCard("Card", "Pay at the counter", "resources/icons/cardpaymentic.png", 639);

        paymentpanel.add(cashCard);
        paymentpanel.add(ewalletCard);
        paymentpanel.add(cardCard);
        ewalletDetails.setBounds(70, 282, 860, 290);
        cardDetails.setBounds(70, 282, 860, 290);
        paymentpanel.add(ewalletDetails);
        paymentpanel.add(cardDetails);
        ewalletDetails.setVisible(false);
        cardDetails.setVisible(false);

        // ================ BOTTOM =================//

        JPanel bottompanel = new JPanel(null);
        bottompanel.setBounds(0, 590, 1000, 60);
        bottompanel.setBackground(new Color(240, 243, 245));
        paymentpanel.add(bottompanel);

        JButton continuebtn = new JButton("Continue  >");
        continuebtn.setBounds(500, 0, 500, 60);
        continuebtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        continuebtn.setForeground(Color.WHITE);
        continuebtn.setBackground(new Color(235, 25, 35));
        continuebtn.setFocusPainted(false);
        continuebtn.setBorderPainted(false);
        continuebtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottompanel.add(continuebtn);
        JButton backbtn = new JButton("Back");
        backbtn.setBounds(0, 0, 500, 60);
        backbtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        backbtn.setForeground(Color.BLACK);
        backbtn.setBackground(new Color(240, 243, 245));
        backbtn.setFocusPainted(false);
        backbtn.setBorderPainted(false);
        backbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backbtn.addActionListener(
                e -> {
                    if (enteringDetails) showPaymentChoices();
                    else
                        ((CardLayout) getParent().getLayout())
                                .show(getParent(), "ConfirmTripDetails");
                });
        bottompanel.add(backbtn);

        // ================ BUTTON ACTIONS =================//

        continuebtn.addActionListener(
                e -> {
                    if (selectedPayment.isEmpty()) {

                        qpal.components.AppDialogs.showMessageDialog(
                                null,
                                "Please select a payment method first.",
                                "No Payment Selected",
                                JOptionPane.WARNING_MESSAGE);

                        return;
                    }

                    // All methods receive a pending queue ticket. Payment is verified at the
                    // counter.
                    TripCardPanel card = TripCardPanel.getSelectedCard();
                    SeatSelectionPanel seatPanel = null;
                    PrintTicketPanel ticketPanel = null;
                    for (Component component : getParent().getComponents()) {
                        if (component instanceof SeatSelectionPanel)
                            seatPanel = (SeatSelectionPanel) component;
                        if (component instanceof PrintTicketPanel)
                            ticketPanel = (PrintTicketPanel) component;
                    }
                    if (card == null
                            || card.getTrip() == null
                            || seatPanel == null
                            || ticketPanel == null) return;
                    if (!passengerPanel.validatePassengerDetails()) {
                        qpal.components.AppDialogs.showMessageDialog(
                                this, "Please complete the passenger details before payment.");
                        ((CardLayout) getParent().getLayout())
                                .show(getParent(), "PassengerDetails");
                        return;
                    }
                    java.util.List<String> names = passengerPanel.getPassengerNames();
                    java.util.List<String> types = passengerPanel.getPassengerTypes();
                    java.util.List<String> seats = seatPanel.getSelectedSeatNumbers();
                    if (names.size() != passengerPanel.getPassengerCount()
                            || seats.size() != names.size()) {
                        qpal.components.AppDialogs.showMessageDialog(
                                this, "Please go back and select one seat for each passenger.");
                        return;
                    }
                    java.util.List<qpal.model.BookingData.Passenger> passengers =
                            new java.util.ArrayList<>();
                    for (int i = 0; i < names.size(); i++)
                        passengers.add(
                                new qpal.model.BookingData.Passenger(
                                        names.get(i),
                                        types.get(i),
                                        qpal.dao.BookingDao.seatNumber(seats.get(i))));
                    String method = selectedPayment.equals("E-Wallet") ? "GCash" : selectedPayment;
                    PrintTicketPanel target = ticketPanel;
                    continuebtn.setEnabled(false);
                    backbtn.setEnabled(false);
                    continuebtn.setText("Saving booking...");
                    qpal.util.UiTask.run(
                            () ->
                                    new qpal.dao.BookingDao()
                                            .book(
                                                    bookingReference,
                                                    card.getTrip(),
                                                    passengers,
                                                    method,
                                                    tripDetailsPanel.getDropPoint()),
                            receipt -> {
                                target.showReceipt(receipt);
                                ((CardLayout) getParent().getLayout())
                                        .show(getParent(), "PrintTicket");
                                continuebtn.setEnabled(true);
                                backbtn.setEnabled(true);
                                continuebtn.setText("Continue  >");
                            },
                            ex -> {
                                continuebtn.setEnabled(true);
                                backbtn.setEnabled(true);
                                continuebtn.setText("Continue  >");
                                qpal.components.AppDialogs.showMessageDialog(
                                        this,
                                        ex.getMessage(),
                                        "Booking not completed",
                                        JOptionPane.WARNING_MESSAGE);
                            });
                });

        // ================ WORKING DATE AND TIME =================//

        Timer timeTimer =
                new Timer(
                        1000,
                        new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {

                                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                                datelabel.setText(
                                        now.format(
                                                java.time.format.DateTimeFormatter.ofPattern(
                                                        "MMMM d, yyyy")));

                                timelabel.setText(
                                        now.format(
                                                java.time.format.DateTimeFormatter.ofPattern(
                                                        "hh:mm a")));
                            }
                        });

        timeTimer.start();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        datelabel.setText(now.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        timelabel.setText(now.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
    }

    // ================ CREATE PAYMENT CARD =================//

    private JPanel createPaymentCard(String text, String subtext, String iconPath, int x) {

        JPanel panel = new JPanel(null);

        panel.setBounds(x, 310, 215, 180);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel typelabel = new JLabel(text);
        typelabel.setBounds(0, 17, 215, 25);
        typelabel.setHorizontalAlignment(SwingConstants.CENTER);
        typelabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        typelabel.setForeground(new Color(100, 100, 100));
        panel.add(typelabel);

        JLabel icon = new JLabel();

        ImageIcon iconpic = new ImageIcon(iconPath);

        if (iconpic.getIconWidth() > 0) {

            Image iconimg = iconpic.getImage().getScaledInstance(110, 80, Image.SCALE_SMOOTH);

            icon.setIcon(new ImageIcon(iconimg));
        }

        icon.setBounds(52, 48, 110, 80);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(icon);

        JLabel subtextlabel = new JLabel(subtext);
        subtextlabel.setBounds(0, 145, 215, 25);
        subtextlabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtextlabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtextlabel.setForeground(new Color(100, 100, 100));
        panel.add(subtextlabel);

        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        MouseAdapter selectPayment =
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        resetCards();

                        panel.setBorder(BorderFactory.createLineBorder(new Color(225, 0, 45), 3));

                        selectedPayment = text;
                    }
                };
        panel.addMouseListener(selectPayment);
        for (Component child : panel.getComponents()) child.addMouseListener(selectPayment);
        return panel;
    }

    // ================ RESET CARDS =================//

    private void resetCards() {

        cashCard.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        ewalletCard.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        cardCard.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    }

    public void resetInputs() {
        bookingReference = newReference();
        selectedPayment = "";
        ewalletDetails.resetInputs();
        cardDetails.resetInputs();
        showPaymentChoices();
        resetCards();
    }
}
