package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TripCardPanel extends JPanel {
    private qpal.model.BookingData.TripOption trip;

    public TripCardPanel(qpal.model.BookingData.TripOption trip) {
        this(trip, trip.fare());
    }

    public TripCardPanel(
            qpal.model.BookingData.TripOption trip, java.math.BigDecimal passengerFare) {
        this(
                trip.bus(),
                trip.route(),
                trip.date()
                        + " | "
                        + trip.time()
                                .format(
                                        java.time.format.DateTimeFormatter.ofPattern(
                                                "h:mm a", java.util.Locale.ENGLISH)),
                "PHP " + qpal.model.FarePolicy.format(passengerFare),
                trip.available());
        this.trip = trip;
    }

    public qpal.model.BookingData.TripOption getTrip() {
        return trip;
    }

    private static TripCardPanel selectedCard;
    private final JLabel busIconLabel = new JLabel();
    private final JLabel seatIconLabel = new JLabel();
    private final String busName;
    private final String route;
    private final String schedule;
    private boolean selected;

    public TripCardPanel(String busName, String route, String schedule, String fare) {
        this(busName, route, schedule, fare, -1);
    }

    public TripCardPanel(String busName, String route, String schedule, String fare, int seats) {
        this.busName = busName;
        this.route = route;
        this.schedule = schedule;
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(430, 128));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(true);
        getAccessibleContext()
                .setAccessibleName(busName + ", " + route + ", " + schedule + ", " + fare);

        // Reserved for your bus image: busIconLabel.setIcon(...).
        busIconLabel.setName("busIconLabel");
        busIconLabel.setBounds(16, 22, 76, 88);
        busIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(busIconLabel);

        addLabel(busName, 112, 17, 72, 24, 16, Font.BOLD, Color.BLACK);
        JLabel badge =
                new JLabel(seats == 0 ? "Full" : "Available", SwingConstants.CENTER) {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(new Color(225, 247, 230));
                        g.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                        g.dispose();
                        super.paintComponent(graphics);
                    }
                };
        badge.setBounds(187, 19, 72, 22);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(new Color(16, 128, 45));
        add(badge);
        JLabel routeLabel =
                addLabel(
                        route.replace(" - ", "  \u2192  "),
                        112,
                        45,
                        195,
                        20,
                        12,
                        Font.PLAIN,
                        new Color(35, 40, 48));

        int separator = schedule.lastIndexOf('|');
        String time = separator >= 0 ? schedule.substring(separator + 1).trim() : schedule;
        JLabel clock = new JLabel(new DetailIcon(false));
        clock.setBounds(112, 69, 18, 18);
        add(clock);
        JLabel timeLabel = addLabel(time, 137, 68, 167, 21, 13, Font.PLAIN, new Color(35, 40, 48));
        addLabel(fare, 112, 92, 192, 23, 15, Font.BOLD, Color.BLACK);

        // Reserved for your seat image: seatIconLabel.setIcon(...).
        seatIconLabel.setName("seatIconLabel");
        seatIconLabel.setBounds(327, 46, 19, 22);
        seatIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(seatIconLabel);
        addLabel(
                seats < 0 ? "\u2014" : Integer.toString(seats),
                355,
                44,
                55,
                26,
                19,
                Font.BOLD,
                Color.BLACK);
        addLabel("seats available", 327, 76, 98, 22, 12, Font.PLAIN, new Color(85, 94, 108));

        MouseAdapter select =
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent event) {
                        requestFocusInWindow();
                        toggleSelection();
                    }
                };
        addMouseListener(select);
        for (Component child : getComponents()) child.addMouseListener(select);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "selectTrip");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "selectTrip");
        getActionMap()
                .put(
                        "selectTrip",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                toggleSelection();
                            }
                        });
        addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent event) {
                        repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent event) {
                        repaint();
                    }
                });
    }

    private JLabel addLabel(
            String text,
            int x,
            int y,
            int width,
            int height,
            int fontSize,
            int style,
            Color color) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, width, height);
        label.setFont(new Font("Segoe UI", style, fontSize));
        label.setForeground(color);
        add(label);
        return label;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
        g.setColor(selected ? new Color(225, 0, 45) : new Color(224, 229, 236));
        g.setStroke(new BasicStroke(selected ? 1.5f : 1f));
        g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
        g.setColor(new Color(233, 236, 241));
        g.drawLine(313, 19, 313, getHeight() - 19);
        if (hasFocus() && !selected) {
            g.setColor(new Color(120, 130, 145));
            g.setStroke(
                    new BasicStroke(
                            1f,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND,
                            10f,
                            new float[] {3f, 3f},
                            0f));
            g.drawRoundRect(5, 5, getWidth() - 11, getHeight() - 11, 12, 12);
        }
        if (selected) {
            int x = getWidth() - 30;
            g.setColor(new Color(210, 0, 43));
            g.fillOval(x, 9, 21, 21);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + 5, 19, x + 9, 23);
            g.drawLine(x + 9, 23, x + 16, 16);
        }
        g.dispose();
    }

    private void toggleSelection() {
        if (selectedCard == this) {
            clearSelection();
        } else {
            clearSelection();
            selectedCard = this;
            setSelected(true);
        }
    }

    public void setSelected(boolean value) {
        selected = value;
        repaint();
    }

    public JLabel getBusIconLabel() {
        return busIconLabel;
    }

    public JLabel getSeatIconLabel() {
        return seatIconLabel;
    }

    public String getBusName() {
        return busName;
    }

    public String getRoute() {
        return route;
    }

    public String getSchedule() {
        return schedule;
    }

    public boolean isSelectedCard() {
        return selected;
    }

    public static TripCardPanel getSelectedCard() {
        return selectedCard;
    }

    public static void clearSelection() {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
            selectedCard = null;
        }
    }

    private static class DetailIcon implements Icon {
        private final boolean seat;

        DetailIcon(boolean seat) {
            this.seat = seat;
        }

        public int getIconWidth() {
            return 19;
        }

        public int getIconHeight() {
            return 22;
        }

        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(66, 77, 91));
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (seat) {
                g.drawRoundRect(4, 2, 6, 13, 3, 3);
                g.drawLine(5, 7, 9, 7);
                g.drawRoundRect(5, 14, 12, 4, 2, 2);
                g.drawLine(4, 12, 3, 20);
                g.drawLine(3, 20, 16, 20);
            } else {
                g.drawOval(1, 3, 15, 15);
                g.drawLine(9, 6, 9, 11);
                g.drawLine(9, 11, 12, 13);
            }
            g.dispose();
        }
    }
}
