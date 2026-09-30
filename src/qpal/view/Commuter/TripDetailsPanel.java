package qpal.view.Commuter;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TripDetailsPanel extends JPanel {
    private static final java.time.format.DateTimeFormatter TIME_FORMAT =
            java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH);

    private JComboBox<String> originbox;
    private JComboBox<String> destinationbox;
    private JDateChooser datebox;
    private JComboBox<String> timebox;

    public TripDetailsPanel() {

        setSize(1000,650);
        setLayout(null);

        JPanel tripdetailspanel = new JPanel(null);
        tripdetailspanel.setBounds(0,0,1000,650);
        tripdetailspanel.setBackground(new Color(248,248,248));
        add(tripdetailspanel);

        JPanel toppanel = new JPanel(null);
        toppanel.setBounds(0,0,1000,100);
        toppanel.setBackground(new Color(225,0,45));
        tripdetailspanel.add(toppanel);

        JLabel bussinlogo = new JLabel();
        bussinlogo.setBounds(35,20,120,55);
        ImageIcon bussinpic = new ImageIcon("resources/icons/bussinlogokiosk.png");
        Image bussinimg = bussinpic.getImage().getScaledInstance(120,55,Image.SCALE_SMOOTH);
        bussinlogo.setIcon(new ImageIcon(bussinimg));
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

        JPanel stepspanel = KioskStepsPanel.create(0);
        stepspanel.setBounds(0,100,1000,75);
        tripdetailspanel.add(stepspanel);

        JLabel title = new JLabel("Trip Details");
        title.setBounds(70, 192, 860, 44);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(20, 23, 28));
        tripdetailspanel.add(title);

        JLabel subtext = new JLabel("Enter your trip details to find available buses.");
        subtext.setBounds(70, 238, 860, 25);
        subtext.setHorizontalAlignment(SwingConstants.CENTER);
        subtext.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtext.setForeground(new Color(85, 94, 108));
        tripdetailspanel.add(subtext);

        // Empty labels reserved for your own icons; setIcon(...) when assets are ready.
        JLabel originIconLabel = new JLabel();
        JLabel destinationIconLabel = new JLabel();
        JLabel dateIconLabel = new JLabel();
        JLabel timeIconLabel = new JLabel();

        originbox = new JComboBox<>(new String[]{"", "PITX"});
        destinationbox = new JComboBox<>(new String[]{"",
                "Dasmariñas", "General Mariano Alvarez (GMA)", "Trece Martires", "Alfonso",
                "Amadeo", "Mendez", "Silang", "Tagaytay", "Maragondon", "Naic", "Ternate",
                "Cavite City", "Lancaster City", "Molino", "Paliparan", "Tanza",
                "Balibago", "Sta. Cruz", "Batangas City", "Lipa City", "San Juan",
                "Nasugbu via Aguinaldo Highway", "Nasugbu via Kaybiang", "Antipolo City",
                "Lucena City", "Calauag", "Guinayangan", "San Andres", "Tagkawayan"});
        timebox = new JComboBox<>(new String[]{""});
        java.util.List<String> destinations = new java.util.ArrayList<>();
        for (int i = 1; i < destinationbox.getItemCount(); i++) destinations.add(destinationbox.getItemAt(i));
        destinations.sort(java.text.Collator.getInstance(java.util.Locale.ENGLISH));
        destinationbox.removeAllItems();
        destinationbox.addItem("");
        destinations.forEach(destinationbox::addItem);
        for (int minute = 0; minute < 24 * 60; minute += 30)
            timebox.addItem(java.time.LocalTime.MIDNIGHT.plusMinutes(minute).format(TIME_FORMAT));
        styleComboBox(originbox);
        styleComboBox(destinationbox);
        styleComboBox(timebox);
        destinationbox.setMaximumRowCount(8);
        timebox.setMaximumRowCount(8);

        datebox = new JDateChooser();
        datebox.setDateFormatString("MMMM d, yyyy");
        qpal.components.FormInputStyle.styleCalendar(datebox);
        ((JTextField) datebox.getDateEditor().getUiComponent()).setEditable(false);
        datebox.addPropertyChangeListener("date", e -> {
            if (isShowing()) warnIfDatePassed();
        });

        tripdetailspanel.add(createInputCard("Origin", "Select your departure terminal.",
                originIconLabel, originbox, 70, 282));
        tripdetailspanel.add(createInputCard("Destination", "Where are you going?",
                destinationIconLabel, destinationbox, 510, 282));
        tripdetailspanel.add(createInputCard("Date", "Select your preferred travel date.",
                dateIconLabel, datebox, 70, 427));
        tripdetailspanel.add(createInputCard("Time", "No exact schedule? We'll show nearby times.",
                timeIconLabel, timebox, 510, 427));
        JLabel startover = new JLabel("Start Over");
        startover.setBounds(822,28,100,35);
        startover.setFont(new Font("Segoe UI",Font.BOLD,16));
        startover.setForeground(Color.WHITE);
        startover.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toppanel.add(startover);

        startover.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                Kiosk.startOver(TripDetailsPanel.this);
            }
        });

        JLabel refreshicon = new JLabel();
        refreshicon.setBounds(905,25,40,40);
        ImageIcon refreshpic = new ImageIcon("resources/icons/refresh.png");
        Image refreshimg = refreshpic.getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH);
        refreshicon.setIcon(new ImageIcon(refreshimg));
        refreshicon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toppanel.add(refreshicon);

        refreshicon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                Kiosk.startOver(TripDetailsPanel.this);
            }
        });

        JPanel bottompanel = new JPanel(null);
        bottompanel.setBounds(0,590,1000,60);
        bottompanel.setBackground(new Color(240,243,245));
        tripdetailspanel.add(bottompanel);

        JButton backbtn = new JButton("Back");
        backbtn.setBounds(0,0,500,60);
        backbtn.setFont(new Font("Segoe UI",Font.PLAIN,16));
        backbtn.setForeground(Color.BLACK);
        backbtn.setBackground(new Color(240,243,245));
        backbtn.setFocusPainted(false);
        backbtn.setBorderPainted(false);
        backbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottompanel.add(backbtn);

        ActionListener backaction = new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {

                int choice = qpal.components.AppDialogs.showConfirmDialog(null, "Are you sure you want to go back?", "Confirmation",
                        JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {

                        TripCardPanel.clearSelection();
                        resetInputs();  

                        CardLayout cardlayout = (CardLayout) getParent().getLayout();
                        cardlayout.show(getParent(), "Home");

                    } else {
                        
                        qpal.components.AppDialogs.showMessageDialog(null, "You chose not to proceed.");

                    }
            }
            
        };

        backbtn.addActionListener(backaction);

        JButton continuebtn = new JButton("Continue  >");
        continuebtn.setBounds(500,0,500,60);
        continuebtn.setFont(new Font("Segoe UI",Font.BOLD,16));
        continuebtn.setForeground(Color.WHITE);
        continuebtn.setBackground(new Color(235,25,35));
        continuebtn.setFocusPainted(false);
        continuebtn.setBorderPainted(false);
        continuebtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottompanel.add(continuebtn);

        ActionListener continueaction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (warnIfDatePassed()) return;

                if(isMissing(originbox) || isMissing(destinationbox) || datebox.getDate() == null || isMissing(timebox)) {

                    qpal.components.AppDialogs.showMessageDialog(TripDetailsPanel.this,
                            "Please select an origin, destination, date, and time before continuing.",
                            "Incomplete Trip Details", JOptionPane.WARNING_MESSAGE);

                    return;
                }

                qpal.model.TripSearch search = getSearch();
                continuebtn.setEnabled(false);
                continuebtn.setText("Checking schedules...");
                qpal.util.UiTask.run(() -> search.find(new qpal.dao.BookingDao().availableTrips()), results -> {
                    continuebtn.setEnabled(true);
                    continuebtn.setText("Continue  >");
                    if (!isShowing() || !search.equals(getSearch()) || isMissing(originbox)) return;
                    if (results.isEmpty()) {
                        qpal.components.AppDialogs.showMessageDialog(TripDetailsPanel.this,
                                "No exact or nearby schedule is available for your destination on the selected date. Please choose another date or destination.",
                                "No Schedule Available", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    CardLayout cardlayout = (CardLayout)getParent().getLayout();
                    cardlayout.show(getParent(),"AvailableTrip");
                }, ex -> {
                    continuebtn.setEnabled(true);
                    continuebtn.setText("Continue  >");
                    if (isShowing()) qpal.components.AppDialogs.showMessageDialog(TripDetailsPanel.this,
                            "Unable to check schedules. Please try again.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        };

        continuebtn.addActionListener(continueaction);
        resetInputs();

        Timer timeTimer = new Timer(1000,new ActionListener() {

            @Override

            public void actionPerformed(ActionEvent e) {

                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                datelabel.setText(now.format(
                    java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));

                timelabel.setText(now.format(
                    java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));

            }

        });

        timeTimer.start();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        datelabel.setText(now.format(
            java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        timelabel.setText(now.format(
            java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));


    }

    private static boolean isMissing(JComboBox<String> box) {
        return box.getSelectedItem() == null || box.getSelectedItem().toString().trim().isEmpty();
    }

    private boolean warnIfDatePassed() {
        if (datebox.getDate() == null) return false;
        var search = getSearch();
        var now = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Manila"));
        boolean pastDate = search.date().isBefore(now.toLocalDate());
        if (!pastDate) return false;
        qpal.components.AppDialogs.showMessageDialog(this,
                "The selected travel date has already passed. Please choose today or a future date.",
                "Departure Already Passed", JOptionPane.WARNING_MESSAGE);
        return true;
    }

    public qpal.model.TripSearch getSearch() {
        java.time.LocalDate date = datebox.getDate() == null ? null : java.time.LocalDate.parse(
                new java.text.SimpleDateFormat("yyyy-MM-dd").format(datebox.getDate()));
        return new qpal.model.TripSearch((String)destinationbox.getSelectedItem(), date,
                isMissing(timebox) ? null : java.time.LocalTime.parse((String)timebox.getSelectedItem(), TIME_FORMAT));
    }

    private static JPanel createInputCard(String title, String hint, JLabel iconLabel,
            JComponent input, int x, int y) {
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(251, 252, 254));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.setColor(new Color(228, 232, 238));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.dispose();
            }
        };
        card.setOpaque(false);
        card.setBounds(x, y, 420, 130);
        iconLabel.setName(title.toLowerCase(java.util.Locale.ROOT) + "IconLabel");
        iconLabel.setBounds(16, 17, 30, 30);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel);

        JLabel label = new JLabel(title);
        label.setBounds(58, 13, 344, 24);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(20, 23, 28));
        label.setLabelFor(input);
        card.add(label);

        JLabel description = new JLabel(hint);
        description.setBounds(58, 38, 344, 20);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        description.setForeground(new Color(85, 94, 108));
        card.add(description);

        input.setBounds(14, 72, 392, 44);
        input.getAccessibleContext().setAccessibleName(title);
        card.add(input);
        return card;
    }

    private static void styleComboBox(JComboBox<String> combo) {
        qpal.components.FormInputStyle.styleCombo(combo);
    }

    public void resetInputs() {

    originbox.setSelectedIndex(0);
    destinationbox.setSelectedIndex(0);
    datebox.setDate(null);
    timebox.setSelectedIndex(0);

    }

}
