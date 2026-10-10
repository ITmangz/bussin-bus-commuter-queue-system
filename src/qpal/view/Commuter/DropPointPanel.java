package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import qpal.components.AppDialogs;
import qpal.model.RouteDropPoints;

/** Drop-off selection using the admin's route fare. */
public class DropPointPanel extends JPanel {
    private static final Color RED = new Color(225, 0, 45);
    private static final Color TEXT = new Color(35, 43, 55);
    private static final Color MUTED = new Color(85, 94, 108);
    private static final Color BORDER = new Color(224, 229, 236);
    private final TripDetailsPanel details;
    private final RouteStrip strip = new RouteStrip();
    private final JPanel options = new JPanel(new GridLayout(0, 1, 0, 6));
    private final JLabel subtitle = new JLabel("", SwingConstants.CENTER);
    private final JButton next = new JButton("Continue  >");
    private int loadVersion;

    public DropPointPanel(TripDetailsPanel details) {
        this.details = details;
        setLayout(null);
        setSize(1000, 650);
        setPreferredSize(new Dimension(1000, 650));
        setBackground(new Color(248, 248, 248));
        JPanel header = new JPanel(null);
        header.setBackground(RED);
        header.setBounds(0, 0, 1000, 100);
        add(header);
        JLabel logo = new JLabel(new ImageIcon(new ImageIcon("resources/icons/bussinlogokiosk.png")
                .getImage().getScaledInstance(120, 55, Image.SCALE_SMOOTH)));
        logo.setBounds(35, 20, 120, 55);
        header.add(logo);
        JLabel date = new JLabel("", SwingConstants.CENTER);
        date.setBounds(330, 24, 340, 50);
        date.setForeground(Color.WHITE);
        date.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(date);
        Runnable clock = () -> {
            var now = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Manila"));
            date.setText("<html><center>" + now.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    + "<br>" + now.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")) + "</center></html>");
        };
        clock.run();
        Timer time = new Timer(1000, e -> clock.run());
        JLabel reset = new JLabel("Start Over");
        reset.setBounds(822, 28, 100, 35);
        reset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reset.setForeground(Color.WHITE);
        reset.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(reset);
        JLabel refreshIcon = new JLabel(new ImageIcon(new ImageIcon("resources/icons/refresh.png")
                .getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
        refreshIcon.setBounds(905, 25, 40, 40);
        refreshIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.add(refreshIcon);
        MouseAdapter resetAction = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { Kiosk.startOver(DropPointPanel.this); }
        };
        reset.addMouseListener(resetAction);
        refreshIcon.addMouseListener(resetAction);
        JPanel steps = KioskStepsPanel.create(0);
        steps.setBounds(0, 100, 1000, 75);
        add(steps);
        JLabel title = new JLabel("Select Your Drop-off", SwingConstants.CENTER);
        title.setBounds(70, 181, 860, 44);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(RED);
        add(title);
        subtitle.setBounds(40, 226, 920, 24);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(MUTED);
        add(subtitle);
        strip.setBounds(70, 260, 860, 100);
        add(strip);
        options.setOpaque(false);
        JScrollPane scroll = new JScrollPane(options, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(70, 372, 860, 204);
        scroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        scroll.setBackground(getBackground());
        scroll.getViewport().setBackground(getBackground());
        qpal.components.ScrollBarStyle.apply(scroll, getBackground());
        add(scroll);
        JButton back = new JButton("Back");
        back.setBounds(0, 590, 500, 60);
        back.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        back.setBackground(new Color(240, 243, 245));
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.addActionListener(e -> showPage("TripDetails"));
        add(back);
        next.setBounds(500, 590, 500, 60);
        next.setFont(new Font("Segoe UI", Font.BOLD, 16));
        next.setBackground(RED);
        next.setForeground(Color.WHITE);
        next.setBorderPainted(false);
        next.setFocusPainted(false);
        next.addActionListener(e -> {
            if (details.getDropPoint() == null) {
                AppDialogs.showMessageDialog(this, "Please select your drop-off point.",
                        "Select Drop-off", JOptionPane.WARNING_MESSAGE);
            } else if (details.getSelectedFare() == null) {
                AppDialogs.showMessageDialog(this, "Fare is unavailable. Please ask staff for assistance.",
                        "Fare Unavailable", JOptionPane.WARNING_MESSAGE);
            } else showPage("AvailableTrip");
        });
        add(next);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { refresh(); }
        });
        addHierarchyListener(e -> { if (isShowing()) time.start(); else time.stop(); });
    }

    /** Set your own image here, e.g. getBusLabel().setIcon(new ImageIcon(...)). */
    public JLabel getBusLabel() { return strip.busLabel; }

    private void showPage(String page) {
        ((CardLayout) getParent().getLayout()).show(getParent(), page);
    }

    public void refresh() {
        String destination = details.getSearch().destination();
        if (destination == null || destination.isBlank()) return;
        int version = ++loadVersion;
        displayRoute(destination, null);
        subtitle.setText("Loading route fares...");
        qpal.util.UiTask.run(() -> new qpal.dao.RouteDao().getActiveFare(destination), fare -> {
            if (version != loadVersion || !destination.equals(details.getSearch().destination())) return;
            displayRoute(destination, fare);
        }, error -> {
            if (version != loadVersion || !destination.equals(details.getSearch().destination())) return;
            subtitle.setText("Fare is unavailable. Please go back and try again, or ask staff for assistance.");
        });
    }

    private void displayRoute(String destination, BigDecimal fullFare) {
        strip.points = new java.util.ArrayList<>(List.of("PITX"));
        strip.points.addAll(RouteDropPoints.options(destination));
        strip.selected = details.getDropPoint();
        strip.setToolTipText(String.join(" → ", strip.points));
        subtitle.setText("PITX to " + destination + " · Choose your stop. Fares shown are per passenger.");
        details.setSelectedFare(null);
        options.removeAll();
        ButtonGroup group = new ButtonGroup();
        JRadioButton origin = new JRadioButton("PITX  ·  Origin");
        styleRadioIcon(origin);
        origin.setFont(new Font("Segoe UI", Font.BOLD, 17));
        origin.setEnabled(false);
        origin.setBorderPainted(true);
        styleChoice(origin);
        options.add(origin);
        options.setPreferredSize(new Dimension(820, (RouteDropPoints.options(destination).size() + 1) * 60 - 6));

        for (String stop : RouteDropPoints.options(destination)) {
            BigDecimal fare = fullFare == null ? null : RouteDropPoints.fare(destination, stop, fullFare);
            JRadioButton choice = new JRadioButton(stop);
            styleRadioIcon(choice);
            choice.setLayout(new BorderLayout());
            choice.setFont(new Font("Segoe UI", Font.BOLD, 17));
            choice.setForeground(TEXT);
            choice.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            choice.setBorderPainted(true);
            choice.setFocusPainted(true);
            choice.setRolloverEnabled(true);
            choice.setEnabled(fare != null);
            choice.setSelected(stop.equals(details.getDropPoint()));

            choice.setToolTipText(stop + (stop.equals(destination) ? " · Final destination" : " · Drop-off"));
            JLabel amount = new JLabel(fare == null ? "—" : money(fare), SwingConstants.RIGHT);
            amount.setPreferredSize(new Dimension(156, 50));
            amount.setFont(new Font("Segoe UI", Font.BOLD, 20));
            amount.setForeground(RED);
            choice.add(amount, BorderLayout.EAST);
            // The fare label is part of the same large clickable row.
            amount.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { if (choice.isEnabled()) choice.doClick(); }
                @Override public void mouseEntered(MouseEvent e) { choice.getModel().setRollover(true); }
                @Override public void mouseExited(MouseEvent e) { choice.getModel().setRollover(false); }
            });
            choice.getAccessibleContext().setAccessibleName(stop + ", " + amount.getText());
            group.add(choice);
            options.add(choice);
            styleChoice(choice);
            choice.addChangeListener(e -> styleChoice(choice));
            choice.addActionListener(e -> {
                if (stop.equals(details.getDropPoint())) {
                    group.clearSelection();
                    details.clearDropPoint();
                } else {
                    details.setDropPoint(stop);
                    details.setSelectedFare(fare);
                }
                strip.selected = details.getDropPoint();
                for (Component component : options.getComponents()) styleChoice((JRadioButton) component);
                strip.repaint();
            });
            if (choice.isSelected() && fare != null) {
                details.setSelectedFare(fare);
            }
        }
        options.revalidate();
        options.repaint();
        strip.repaint();
    }

    private static String money(BigDecimal value) { return "₱" + qpal.model.FarePolicy.format(value); }

    private static void styleRadioIcon(JRadioButton button) {
        Icon icon = new Icon() {
            @Override public int getIconWidth() { return 24; }
            @Override public int getIconHeight() { return 24; }

            @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
                JRadioButton radio = (JRadioButton) component;
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = radio.isEnabled();
                Color ring = !active ? BORDER : radio.isSelected() || radio.getModel().isRollover()
                        ? RED : new Color(160, 170, 185);
                g.setColor(active ? Color.WHITE : new Color(235, 239, 243));
                g.fillOval(x + 2, y + 2, 20, 20);
                g.setStroke(new BasicStroke(2));
                g.setColor(ring);
                g.drawOval(x + 2, y + 2, 20, 20);
                if (radio.isSelected()) {
                    g.fillOval(x + 7, y + 7, 10, 10);
                }
                g.dispose();
            }
        };
        button.setIcon(icon);
        button.setSelectedIcon(icon);
        button.setRolloverIcon(icon);
        button.setRolloverSelectedIcon(icon);
        button.setPressedIcon(icon);
        button.setDisabledIcon(icon);
        button.setDisabledSelectedIcon(icon);
        button.setIconTextGap(12);
    }

    private static void styleChoice(JRadioButton choice) {
        choice.setBackground(!choice.isEnabled() ? new Color(240, 243, 245)
                : choice.isSelected() ? new Color(255, 243, 246)
                : choice.getModel().isRollover() ? new Color(255, 248, 250) : Color.WHITE);
        choice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(choice.isSelected() ? RED : BORDER, 2, true),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)));
    }

    private static class RouteStrip extends JPanel {
        List<String> points = List.of();
        String selected;
        final JLabel busLabel = new JLabel();

        RouteStrip() {
            setLayout(null);
            setOpaque(false);
            busLabel.setName("busLabel");
            busLabel.setBounds(54, 4, 52, 32);
            busLabel.setHorizontalAlignment(SwingConstants.CENTER);
            busLabel.getAccessibleContext().setAccessibleName("Bus at PITX origin");
            busLabel.setIcon(new ImageIcon("resources/icons/sidebusic.png"));
            add(busLabel);
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            g.setColor(BORDER);
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            if (points.size() >= 2) {
                double step = (getWidth() - 160.0) / (points.size() - 1);
                g.setStroke(new BasicStroke(3));
                g.drawLine(80, 43, getWidth() - 80, 43);
                int selectedIndex = points.indexOf(selected);
                if (selectedIndex > 0) {
                    g.setColor(RED);
                    g.drawLine(80, 43, 80 + (int) (selectedIndex * step), 43);
                }
                for (int i = 0; i < points.size(); i++) {
                    int x = 80 + (int) (i * step);
                    g.setColor(i == 0 || i <= selectedIndex ? RED : new Color(170, 179, 191));
                    g.fillOval(x - 6, 37, 12, 12);
                    g.setFont(new Font("Segoe UI", i == selectedIndex ? Font.BOLD : Font.PLAIN, 14));
                    g.setColor(i == 0 ? RED : TEXT);
                    String label = points.get(i);
                    while (g.getFontMetrics().stringWidth(label) > Math.min(step - 20, 150) && label.length() > 8)
                        label = label.substring(0, label.length() - 2) + "…";
                    g.drawString(label, x - g.getFontMetrics().stringWidth(label) / 2, 71);
                    if (i == 0 || i == points.size() - 1) {
                        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        g.setColor(MUTED);
                        String caption = i == 0 ? "Origin" : "Final destination";
                        g.drawString(caption, x - g.getFontMetrics().stringWidth(caption) / 2, 89);
                    }
                }
            }
            g.dispose();
        }
    }
}
