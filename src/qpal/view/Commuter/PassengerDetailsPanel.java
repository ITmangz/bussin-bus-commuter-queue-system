package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PassengerDetailsPanel extends JPanel {

        private TripDetailsPanel tripDetailsPanel;

        private int passengerCount = 1;
        private JLabel lblCount;
        private JButton minusbtn;
        private JButton plusbtn;
        private JLabel maximumNote;
        private String selectedType = "";

        private JPanel regularCard;
        private JPanel studentCard;
        private JPanel seniorCard;
        private JPanel pwdCard;

                public PassengerDetailsPanel(TripDetailsPanel tripDetailsPanel) {

                this.tripDetailsPanel = tripDetailsPanel;

                setSize(1000,650);
                setLayout(null);

                JPanel passengerdetailspanel = new JPanel(null);
                passengerdetailspanel.setBounds(0,0,1000,650);
                passengerdetailspanel.setBackground(new Color(248,248,248));
                add(passengerdetailspanel);

                //================ TOP =================//

                JPanel toppanel = new JPanel(null);
                toppanel.setBounds(0,0,1000,100);
                toppanel.setBackground(new Color(225,0,45));
                passengerdetailspanel.add(toppanel);

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

                JLabel startover = new JLabel("Start Over");
                startover.setBounds(822,28,100,35);
                startover.setFont(new Font("Segoe UI",Font.BOLD,16));
                startover.setForeground(Color.WHITE);
                startover.setCursor(new Cursor(Cursor.HAND_CURSOR));
                toppanel.add(startover);

                startover.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                Kiosk.startOver(PassengerDetailsPanel.this);
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

                Kiosk.startOver(PassengerDetailsPanel.this);
            }
        });

                JPanel stepspanel = KioskStepsPanel.create(2);
        stepspanel.setBounds(0,100,1000,75);
        passengerdetailspanel.add(stepspanel);

                JLabel title = new JLabel("Passenger Details");
                title.setBounds(0,205,1000,50);
                title.setHorizontalAlignment(SwingConstants.CENTER);
                title.setFont(new Font("Segoe UI",Font.BOLD,32));
                title.setForeground(new Color(225,0,45));
                passengerdetailspanel.add(title);

                JLabel subtext = new JLabel("Select your preferred passenger information.");
                subtext.setBounds(0,245,1000,32);
                subtext.setHorizontalAlignment(SwingConstants.CENTER);
                subtext.setFont(new Font("Segoe UI",Font.PLAIN,18));
                subtext.setForeground(new Color(100,100,100));
                passengerdetailspanel.add(subtext);

                JPanel counterpanel = new JPanel(null) {
                    @Override protected void paintComponent(Graphics graphics) {
                        super.paintComponent(graphics);
                        paintRoundedBox(graphics, getWidth(), getHeight(), Color.WHITE,
                                new Color(228, 232, 238));
                    }
                };
                counterpanel.setOpaque(false);
                counterpanel.setBounds(90, 285, 820, 122);
                passengerdetailspanel.add(counterpanel);

                JLabel numberlabel = new JLabel("Number of Passengers");
                numberlabel.setBounds(0, 10, 820, 24);
                numberlabel.setHorizontalAlignment(SwingConstants.CENTER);
                numberlabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                numberlabel.setForeground(Color.BLACK);
                counterpanel.add(numberlabel);

                minusbtn = createCounterButton(false);
                minusbtn.setBounds(302, 38, 52, 52);
                counterpanel.add(minusbtn);

                lblCount = new JLabel("1", SwingConstants.CENTER) {
                    @Override protected void paintComponent(Graphics graphics) {
                        paintRoundedBox(graphics, getWidth(), getHeight(), Color.WHITE,
                                new Color(205, 212, 223));
                        super.paintComponent(graphics);
                    }
                };
                lblCount.setBounds(365, 38, 90, 52);
                lblCount.setFont(new Font("Segoe UI", Font.BOLD, 32));
                lblCount.setForeground(Color.BLACK);
                lblCount.getAccessibleContext().setAccessibleName("Number of passengers");
                counterpanel.add(lblCount);

                plusbtn = createCounterButton(true);
                plusbtn.setBounds(466, 38, 52, 52);
                counterpanel.add(plusbtn);

                maximumNote = new JLabel("Maximum of 10 passengers per transaction.",
                        SwingConstants.CENTER);
                maximumNote.setBounds(0, 95, 820, 20);
                maximumNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                maximumNote.setForeground(new Color(90, 100, 116));
                counterpanel.add(maximumNote);

                minusbtn.addActionListener(e -> {
                    if (passengerCount > 1) passengerCount--;
                    updatePassengerCounter();
                });
                plusbtn.addActionListener(e -> {
                    if (passengerCount < passengerLimit()) passengerCount++;
                    updatePassengerCounter();
                });
                updatePassengerCounter();
                addComponentListener(new ComponentAdapter() {
                    @Override public void componentShown(ComponentEvent e) {
                        updatePassengerCounter();
                    }
                });
                regularCard = createCard(
                        "Regular",
                        "PHP",
                        "resources/icons/regular.png",
                        90,
                        425);

                studentCard = createCard(
                        "Student",
                        "PHP",
                        "resources/icons/student.png",
                        300,
                        425);

                seniorCard = createCard(
                        "Senior",
                        "PHP",
                        "resources/icons/senior.png",
                        510,
                        425);

                pwdCard = createCard(
                        "PWD",
                        "PHP",
                        "resources/icons/pwd.png",
                        720,
                        425);

                passengerdetailspanel.add(regularCard);
                passengerdetailspanel.add(studentCard);
                passengerdetailspanel.add(seniorCard);
                passengerdetailspanel.add(pwdCard);

                JPanel bottompanel = new JPanel(null);
                bottompanel.setBounds(0,590,1000,60);
                bottompanel.setBackground(new Color(240,243,245));
                passengerdetailspanel.add(bottompanel);

                JButton backbtn = new JButton("Back");
                backbtn.setBounds(0,0,500,60);
                backbtn.setFont(new Font("Segoe UI",Font.PLAIN,16));
                backbtn.setForeground(Color.BLACK);
                backbtn.setBackground(new Color(240,243,245));
                backbtn.setFocusPainted(false);
                backbtn.setBorderPainted(false);
                backbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                bottompanel.add(backbtn);

                backbtn.addActionListener(e -> {

                int choice = JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to go back?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {

                        CardLayout cardlayout =
                                (CardLayout)getParent().getLayout();

                        cardlayout.show(getParent(),"AvailableTrip");

                }

                });

                JButton continuebtn = new JButton("Continue  >");
                continuebtn.setBounds(500,0,500,60);
                continuebtn.setFont(new Font("Segoe UI",Font.BOLD,16));
                continuebtn.setForeground(Color.WHITE);
                continuebtn.setBackground(new Color(235,25,35));
                continuebtn.setFocusPainted(false);
                continuebtn.setBorderPainted(false);
                continuebtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                bottompanel.add(continuebtn);

                continuebtn.addActionListener(e -> {

                updatePassengerCounter();
                if (passengerCount == 0) {
                    JOptionPane.showMessageDialog(this,"No seats remain. Please select another trip.",
                            "Bus full",JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (selectedType.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                null,
                                "Please select a passenger type.",
                                "Incomplete Information",
                                JOptionPane.WARNING_MESSAGE);

                        return;

                }

                CardLayout cardlayout =
                        (CardLayout)getParent().getLayout();


                cardlayout.show(getParent(),"SelectSeats");

                });

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

        private static void paintRoundedBox(Graphics graphics, int width, int height,
                Color fill, Color border) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(1, 1, width - 3, height - 3, 14, 14);
            g.setColor(border);
            g.drawRoundRect(1, 1, width - 3, height - 3, 14, 14);
            g.dispose();
        }

        private JButton createCounterButton(boolean plus) {
            JButton button = new JButton() {
                @Override protected void paintComponent(Graphics graphics) {
                    Color fill = plus && isEnabled() ? new Color(218, 0, 43) : new Color(242, 244, 247);
                    if (isEnabled() && getModel().isPressed()) fill = plus
                            ? new Color(180, 0, 35) : new Color(222, 227, 234);
                    paintRoundedBox(graphics, getWidth(), getHeight(), fill,
                            plus && isEnabled() ? fill : new Color(215, 222, 231));
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(plus && isEnabled() ? Color.WHITE : new Color(133, 143, 156));
                    g.setStroke(new BasicStroke(2.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int x = getWidth() / 2, y = getHeight() / 2;
                    g.drawLine(x - 8, y, x + 8, y);
                    if (plus) g.drawLine(x, y - 8, x, y + 8);
                    if (hasFocus()) {
                        g.setStroke(new BasicStroke(1));
                        g.drawRoundRect(5, 5, getWidth() - 11, getHeight() - 11, 10, 10);
                    }
                    g.dispose();
                }
            };
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.getAccessibleContext().setAccessibleName(plus ? "Add passenger" : "Remove passenger");
            return button;
        }

        private int passengerLimit() {
            TripCardPanel selected = TripCardPanel.getSelectedCard();
            if (selected == null || selected.getTrip() == null) return 0;
            return Math.max(0,Math.min(10,Math.min(selected.getTrip().capacity(),selected.getTrip().available())));
        }

        private void updatePassengerCounter() {
            int limit = passengerLimit();
            passengerCount = limit == 0 ? 0 : Math.max(1,Math.min(passengerCount,limit));
            lblCount.setText(String.valueOf(passengerCount));
            minusbtn.setEnabled(passengerCount > 1);
            plusbtn.setEnabled(passengerCount < limit);
            maximumNote.setText(limit == 0 ? "No seats available. Please select another trip."
                    : "Maximum of " + limit + " passenger" + (limit == 1 ? "" : "s")
                    + " for this trip (up to 10 per transaction).");
        }
        private JPanel createCard(
                String text,
                String price,
                String iconPath,
                int x,
                int y) {

                JPanel panel = new JPanel(null) {
                    @Override protected void paintComponent(Graphics graphics) {
                        super.paintComponent(graphics);
                        paintRoundedBox(graphics, getWidth(), getHeight(), Color.WHITE, Color.WHITE);
                    }
                };
                panel.setOpaque(false);

                panel.setBounds(x,y,190,105);
                panel.setBackground(Color.WHITE);
                panel.setBorder(
                        new OptionBorder(
                                Color.LIGHT_GRAY));

                JLabel icon = new JLabel();

                ImageIcon iconpic = new ImageIcon(iconPath);

                if (iconpic.getIconWidth() > 0) {

                Image iconimg =
                        iconpic.getImage().getScaledInstance(
                                48,
                                48,
                                Image.SCALE_SMOOTH);

                icon.setIcon(new ImageIcon(iconimg));

                }

                icon.setBounds(15,28,48,48);
                icon.setHorizontalAlignment(SwingConstants.CENTER);
                panel.add(icon);

                JLabel typelabel = new JLabel(text);
                typelabel.setBounds(70,25,105,25);
                typelabel.setFont(
                        new Font("Segoe UI",Font.BOLD,17));
                typelabel.setForeground(Color.BLACK);
                panel.add(typelabel);

                JLabel pricelabel = new JLabel(price);
                pricelabel.setBounds(70,51,105,20);
                pricelabel.setFont(
                        new Font("Segoe UI",Font.PLAIN,15));
                pricelabel.setForeground(
                        new Color(80,80,80));
                panel.add(pricelabel);

                panel.setCursor(
                        new Cursor(Cursor.HAND_CURSOR));

                panel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                        // If already selected, unselect it
                        if (selectedType.equals(text)) {
                        panel.setBorder(
                                new OptionBorder(Color.LIGHT_GRAY));
                        selectedType = "";
                        return;
                        }

                        // Otherwise select it
                        resetCards();

                        panel.setBorder(
                                new OptionBorder(
                                        new Color(225,0,45),
                                        3));

                        selectedType = text;
                }

                });

                return panel;

        }

        private static class OptionBorder extends javax.swing.border.AbstractBorder {
                private final Color color;
                private final int thickness;

                OptionBorder(Color color) { this(color, 1); }

                OptionBorder(Color color, int thickness) {
                        this.color = color;
                        this.thickness = thickness;
                }

                @Override public void paintBorder(Component component, Graphics graphics,
                        int x, int y, int width, int height) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(color);
                        g.setStroke(new BasicStroke(thickness));
                        g.drawRoundRect(x + 2, y + 2, width - 5, height - 5, 14, 14);
                        g.dispose();
                }
        }
        private void resetCards() {

                regularCard.setBorder(
                        new OptionBorder(
                                Color.LIGHT_GRAY));

                studentCard.setBorder(
                        new OptionBorder(
                                Color.LIGHT_GRAY));

                seniorCard.setBorder(
                        new OptionBorder(
                                Color.LIGHT_GRAY));

                pwdCard.setBorder(
                        new OptionBorder(
                                Color.LIGHT_GRAY));

        }

        public String getPassengerType() { return selectedType; }
        public java.util.List<String> getPassengerNames() {
            java.util.List<String> labels = new java.util.ArrayList<>();
            for (int i = 1; i <= passengerCount; i++) labels.add("Passenger " + i);
            return labels;
        }
        public int getPassengerCount() { return passengerCount; }

        public void resetInputs() {

                if(getParent() != null) {
                    for(Component component : getParent().getComponents()) {
                        if(component instanceof SelectSeatsPanel) {
                            ((SelectSeatsPanel)component).resetInputs();
                        }
                    }
                }

                passengerCount = 1;
                updatePassengerCounter();

                selectedType = "";

                resetCards(); // Remove the red border from all passenger cards



        }

}
