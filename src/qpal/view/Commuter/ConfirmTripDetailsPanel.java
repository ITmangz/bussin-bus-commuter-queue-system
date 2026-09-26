package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ConfirmTripDetailsPanel extends JPanel {

    public ConfirmTripDetailsPanel(TripDetailsPanel tripDetailsPanel, PassengerDetailsPanel passengerPanel, PaymentPanel paymentPanel) {

        setSize(1000,650);
        setLayout(null);

        JPanel confirmtripdetailspanel = new JPanel(null);
        confirmtripdetailspanel.setBounds(0,0,1000,650);
        confirmtripdetailspanel.setBackground(Color.WHITE);
        add(confirmtripdetailspanel);

        //================ TOP =================//

        JPanel toppanel = new JPanel(null);
        toppanel.setBounds(0,0,1000,100);
        toppanel.setBackground(new Color(225,0,45));
        confirmtripdetailspanel.add(toppanel);

        JLabel bussinlogo = new JLabel();
        bussinlogo.setBounds(35,20,120,55);

        bussinlogo.setIcon(new ImageIcon(new ImageIcon("resources/icons/bussinlogokiosk.png").getImage().getScaledInstance(120,55,Image.SCALE_SMOOTH)));
        toppanel.add(bussinlogo);

        JLabel datelabel = new JLabel();
        datelabel.setBounds(370,27,260,20);
        datelabel.setHorizontalAlignment(SwingConstants.CENTER);
        datelabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        datelabel.setForeground(Color.WHITE);
        toppanel.add(datelabel);

        JLabel timelabel = new JLabel();
        timelabel.setBounds(370,47,260,20);
        timelabel.setHorizontalAlignment(SwingConstants.CENTER);
        timelabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        timelabel.setForeground(Color.WHITE);
        toppanel.add(timelabel);

        JLabel startover = new JLabel("Start Over");
        startover.setBounds(822,28,100,35);
        startover.setFont(new Font("Segoe UI",Font.BOLD,16));
        startover.setForeground(Color.WHITE);
        startover.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toppanel.add(startover);

        JLabel refreshicon = new JLabel();
        refreshicon.setBounds(905,25,40,40);

        refreshicon.setIcon(new ImageIcon(new ImageIcon("resources/icons/refresh.png").getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH)));
        refreshicon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toppanel.add(refreshicon);

        MouseAdapter resetAction = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                Kiosk.startOver(ConfirmTripDetailsPanel.this);
            }
        };

        startover.addMouseListener(resetAction);
        refreshicon.addMouseListener(resetAction);

        //================ STEPS =================//

        JPanel stepspanel = KioskStepsPanel.create(4);
        stepspanel.setBounds(0,100,1000,75);
        confirmtripdetailspanel.add(stepspanel);

        //================ TITLE =================//

        JLabel title = new JLabel("Confirm Trip Details");
        title.setBounds(0,205,1000,50);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI",Font.BOLD,32));
        title.setForeground(new Color(225,0,45));
        confirmtripdetailspanel.add(title);

        JLabel subtext = new JLabel("Select your preferred route and schedule.");
        subtext.setBounds(0,245,1000,32);
        subtext.setHorizontalAlignment(SwingConstants.CENTER);
        subtext.setFont(new Font("Segoe UI",Font.PLAIN,18));
        subtext.setForeground(new Color(100,100,100));
        confirmtripdetailspanel.add(subtext);

        //================ TRIP SUMMARY =================//

        JPanel summarypanel = new JPanel(null);
        summarypanel.setBounds(133,297,734,224);
        summarypanel.setBackground(Color.WHITE);
        summarypanel.setBorder(
                BorderFactory.createLineBorder(
                        new Color(120,120,120)));

        confirmtripdetailspanel.add(summarypanel);

        JPanel summarytitlepanel = new JPanel(null);
        summarytitlepanel.setBounds(0,0,734,45);
        summarytitlepanel.setBackground(new Color(235,25,35));
        summarypanel.add(summarytitlepanel);

        JLabel summarytitle = new JLabel("Trip Summary");
        summarytitle.setBounds(0,0,734,45);
        summarytitle.setHorizontalAlignment(SwingConstants.CENTER);
        summarytitle.setFont(new Font("Segoe UI",Font.BOLD,22));
        summarytitle.setForeground(Color.WHITE);
        summarytitlepanel.add(summarytitle);

        //================ LEFT SIDE =================//

        JLabel queuelabel = new JLabel("🎟");
        queuelabel.setBounds(58,66,45,45);
        queuelabel.setFont(new Font("Segoe UI Symbol",Font.PLAIN,32));
        queuelabel.setForeground(new Color(225,0,45));
        summarypanel.add(queuelabel);

        JLabel queuetext = new JLabel("Queue No. :");
        queuetext.setBounds(100,65,150,20);
        queuetext.setFont(new Font("Segoe UI",Font.PLAIN,12));
        queuetext.setForeground(Color.BLACK);
        summarypanel.add(queuetext);

        JLabel queuenumber = new JLabel("Assigned after booking");
        queuenumber.setBounds(100,81,150,20);
        queuenumber.setFont(new Font("Segoe UI",Font.BOLD,13));
        queuenumber.setForeground(Color.BLACK);
        summarypanel.add(queuenumber);

        JLabel buslabel = new JLabel("🚌");
        buslabel.setBounds(58,116,45,45);
        buslabel.setFont(new Font("Segoe UI Emoji",Font.PLAIN,30));
        buslabel.setForeground(new Color(225,0,45));
        summarypanel.add(buslabel);

        JLabel bustext = new JLabel("Bus Number:");
        bustext.setBounds(100,114,150,20);
        bustext.setFont(new Font("Segoe UI",Font.PLAIN,12));
        bustext.setForeground(Color.BLACK);
        summarypanel.add(bustext);

        JLabel busnumber = new JLabel("Bus 01");
        busnumber.setBounds(100,130,150,20);
        busnumber.setFont(new Font("Segoe UI",Font.BOLD,13));
        busnumber.setForeground(Color.BLACK);
        summarypanel.add(busnumber);

        JLabel seatlabel = new JLabel("💺");
        seatlabel.setBounds(58,164,45,45);
        seatlabel.setFont(new Font("Segoe UI Emoji",Font.PLAIN,30));
        seatlabel.setForeground(new Color(225,0,45));
        summarypanel.add(seatlabel);

        JLabel seattext = new JLabel("Number of seats:");
        seattext.setBounds(100,163,150,20);
        seattext.setFont(new Font("Segoe UI",Font.PLAIN,12));
        seattext.setForeground(Color.BLACK);
        summarypanel.add(seattext);

        JLabel seats = new JLabel();
        seats.setBounds(100,179,290,20);
        seats.setFont(new Font("Segoe UI",Font.BOLD,13));
        seats.setForeground(Color.BLACK);
        summarypanel.add(seats);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                TripCardPanel selected = TripCardPanel.getSelectedCard();
                if(selected != null) {
                    busnumber.setText(selected.getBusName());
                }
                for(Component component : getParent().getComponents()) {
                    if(component instanceof SelectSeatsPanel) {
                        String selectedSeats = ((SelectSeatsPanel)component).getSelectedSeats();
                        seats.setText(selectedSeats);
                        seats.setToolTipText(selectedSeats);
                    }
                }
            }
        });

        //================ RIGHT SIDE =================//

        JLabel locationlabel = new JLabel("●");
        locationlabel.setBounds(395,65,45,45);
        locationlabel.setFont(new Font("Segoe UI",Font.BOLD,35));
        locationlabel.setForeground(new Color(225,0,45));
        summarypanel.add(locationlabel);

        JLabel destinationtext = new JLabel("Destination:");
        destinationtext.setBounds(435,65,180,20);
        destinationtext.setFont(new Font("Segoe UI",Font.PLAIN,12));
        destinationtext.setForeground(Color.BLACK);
        summarypanel.add(destinationtext);

        JLabel destination = new JLabel("PITX - Lancaster City");
        destination.setBounds(435,81,220,20);
        destination.setFont(new Font("Segoe UI",Font.BOLD,13));
        destination.setForeground(Color.BLACK);
        summarypanel.add(destination);

        JLabel datelabel2 = new JLabel("▦");
        datelabel2.setBounds(395,115,45,45);
        datelabel2.setFont(new Font("Segoe UI Symbol",Font.BOLD,34));
        datelabel2.setForeground(new Color(225,0,45));
        summarypanel.add(datelabel2);

        JLabel datetext = new JLabel("Date & Time:");
        datetext.setBounds(435,114,180,20);
        datetext.setFont(new Font("Segoe UI",Font.PLAIN,12));
        datetext.setForeground(Color.BLACK);
        summarypanel.add(datetext);

        JLabel datetime = new JLabel("September 1, 2026 | 9:00 PM");
        datetime.setBounds(435,130,250,20);
        datetime.setFont(new Font("Segoe UI",Font.BOLD,13));
        datetime.setForeground(Color.BLACK);
        summarypanel.add(datetime);

        JLabel farelabel = new JLabel("₱");
        farelabel.setBounds(395,163,45,45);
        farelabel.setFont(new Font("Segoe UI",Font.BOLD,32));
        farelabel.setForeground(new Color(225,0,45));
        summarypanel.add(farelabel);

        JLabel faretext = new JLabel("Fare:");
        faretext.setBounds(435,163,180,20);
        faretext.setFont(new Font("Segoe UI",Font.PLAIN,12));
        faretext.setForeground(Color.BLACK);
        summarypanel.add(faretext);

        JLabel fare = new JLabel("Php 100.00");
        fare.setBounds(435,179,180,20);
        fare.setFont(new Font("Segoe UI",Font.BOLD,13));
        fare.setForeground(Color.BLACK);
        summarypanel.add(fare);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                TripCardPanel selected = TripCardPanel.getSelectedCard();
                if (selected == null || selected.getTrip() == null) return;
                destination.setText(selected.getRoute());
                datetime.setText(selected.getSchedule());
                fare.setText("PHP " + selected.getTrip().fare().multiply(
                        java.math.BigDecimal.valueOf(passengerPanel.getPassengerCount())));
            }
        });

        //================ BOTTOM =================//

        JPanel bottompanel = new JPanel(null);
        bottompanel.setBounds(0,590,1000,60);
        bottompanel.setBackground(new Color(240,243,245));
        confirmtripdetailspanel.add(bottompanel);

        JButton backbtn = new JButton("Back");
        backbtn.setBounds(0,0,500,60);
        backbtn.setFont(new Font("Segoe UI",Font.PLAIN,16));
        backbtn.setForeground(Color.BLACK);
        backbtn.setBackground(new Color(240,243,245));
        backbtn.setFocusPainted(false);
        backbtn.setBorderPainted(false);
        backbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottompanel.add(backbtn);

        JButton continuebtn = new JButton("Continue  >");
        continuebtn.setBounds(500,0,500,60);
        continuebtn.setFont(new Font("Segoe UI",Font.BOLD,16));
        continuebtn.setForeground(Color.WHITE);
        continuebtn.setBackground(new Color(235,25,35));
        continuebtn.setFocusPainted(false);
        continuebtn.setBorderPainted(false);
        continuebtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottompanel.add(continuebtn);

        //================ BUTTON ACTIONS =================//

        backbtn.addActionListener(e -> {

            int choice = qpal.components.AppDialogs.showConfirmDialog(
                    null,
                    "Are you sure you want to go back?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {

                CardLayout cardlayout =
                        (CardLayout)getParent().getLayout();

                cardlayout.show(getParent(),"SelectSeats");

            }

        });

        continuebtn.addActionListener(e -> {

            CardLayout cardlayout =
                    (CardLayout)getParent().getLayout();

            cardlayout.show(getParent(),"Payment");

        });

        //================ WORKING DATE AND TIME =================//

        Timer timeTimer = new Timer(1000,new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                java.time.LocalDateTime now =
                        java.time.LocalDateTime.now();

                datelabel.setText(now.format(
                        java.time.format.DateTimeFormatter.ofPattern(
                                "MMMM d, yyyy")));

                timelabel.setText(now.format(
                        java.time.format.DateTimeFormatter.ofPattern(
                                "hh:mm a")));

            }

        });

        timeTimer.start();

        java.time.LocalDateTime now =
                java.time.LocalDateTime.now();

        datelabel.setText(now.format(
                java.time.format.DateTimeFormatter.ofPattern(
                        "MMMM d, yyyy")));

        timelabel.setText(now.format(
                java.time.format.DateTimeFormatter.ofPattern(
                        "hh:mm a")));

    }

}

