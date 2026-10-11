package qpal.view.Admin;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

import javax.swing.*;

/** Interactive demo only; no payment provider is contacted. */
final class CashlessSimulationPanel extends JPanel {
    private String cardType = "Card";
    private String lastFour = "----";
    // Put your QR image at this path. The label is 200 x 200 pixels.
    private final JLabel qrLabel = new JLabel("Click to scan QR", SwingConstants.CENTER);
    private final JLabel feedback = new JLabel(" ", SwingConstants.CENTER);
    private final JPanel scene = new JPanel(null);
    private final JButton action = new JButton();
    private final JButton close = new JButton("Cancel");
    private final JLabel card =
            new JLabel() {
                @Override
                protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setPaint(
                            new GradientPaint(
                                    0,
                                    0,
                                    new Color(10, 49, 111),
                                    220,
                                    132,
                                    new Color(0, 137, 211)));
                    g.fillRoundRect(0, 0, 220, 132, 18, 18);
                    g.setColor(new Color(255, 255, 255, 25));
                    g.fillOval(125, -50, 160, 160);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    g.drawString("Bussin", 17, 27);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g.drawString("CONTACTLESS", 126, 26);
                    g.setColor(new Color(232, 204, 139));
                    g.fillRoundRect(18, 42, 32, 24, 5, 5);
                    g.setColor(new Color(147, 123, 77));
                    g.drawLine(18, 54, 50, 54);
                    g.drawLine(29, 42, 29, 66);
                    g.drawLine(39, 42, 39, 66);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Monospaced", Font.PLAIN, 15));
                    g.drawString("•••• •••• •••• " + lastFour, 17, 88);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g.drawString("CARDHOLDER", 17, 108);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g.drawString("BUSSIN PASSENGER", 17, 122);
                    g.drawString(cardType, 140, 120);
                    g.dispose();
                }
            };
    private final JLabel scan = new JLabel();
    private final boolean wallet;
    private Timer animation;
    private int frame;
    private boolean scanned;
    private String reference;

    private final java.util.function.BooleanSupplier ready;
    private final java.util.function.Consumer<String> completed;
    private boolean locked;
    private final JLabel title = new JLabel();

    CashlessSimulationPanel(
            String method,
            BigDecimal amount,
            java.util.function.BooleanSupplier ready,
            java.util.function.Consumer<String> completed) {
        super(new BorderLayout());
        this.ready = ready;
        this.completed = completed;
        wallet =
                method.toLowerCase(java.util.Locale.ROOT).contains("gcash")
                        || method.toLowerCase(java.util.Locale.ROOT).contains("wallet");

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(224, 229, 236)),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JPanel heading = new JPanel(new GridLayout(1, 1));
        heading.setOpaque(false);
        title.setText(wallet ? "E-Wallet" : "Tap to Pay");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0, 91, 220));
        heading.add(title);
        content.add(heading, BorderLayout.NORTH);
        scene.setPreferredSize(new Dimension(wallet ? 320 : 390, wallet ? 190 : 300));
        scene.setBackground(new Color(247, 249, 252));
        scene.setBorder(BorderFactory.createLineBorder(new Color(230, 234, 240)));
        if (wallet) {
            qrLabel.setBounds(12, 15, 160, 160);
            qrLabel.setBorder(BorderFactory.createLineBorder(new Color(218, 226, 238)));
            qrLabel.setOpaque(true);
            qrLabel.setBackground(Color.WHITE);
            qrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            qrLabel.setForeground(new Color(100, 110, 125));
            ImageIcon qr = new ImageIcon("resources/icons/gcashqr.png");
            if (qr.getIconWidth() > 0) {
                qrLabel.setText("");
                qrLabel.setIcon(
                        new ImageIcon(
                                qr.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH)));
            }
            qrLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            qrLabel.addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            startAnimation();
                        }
                    });
            scan.setOpaque(true);
            scan.setBackground(new Color(0, 160, 240));
            scan.setBounds(12, 15, 160, 3);
            scan.setVisible(false);
            scene.add(scan);
            scene.add(qrLabel);
            JLabel name = new JLabel("Bussin", SwingConstants.CENTER);
            name.setBounds(182, 65, 130, 24);
            name.setFont(new Font("Segoe UI", Font.BOLD, 15));
            name.setForeground(new Color(30, 41, 59));
            scene.add(name);
            JLabel number = new JLabel("Scan to pay", SwingConstants.CENTER);
            number.setBounds(182, 92, 130, 24);
            number.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            number.setForeground(new Color(100, 110, 125));
            scene.add(number);
            action.setText("Scan QR");
        } else {
            JLabel terminal =
                    new JLabel() {
                        @Override
                        protected void paintComponent(Graphics graphics) {
                            Graphics2D g = (Graphics2D) graphics.create();
                            g.setRenderingHint(
                                    RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                            g.setColor(new Color(210, 217, 227));
                            g.fillRoundRect(5, 5, 180, 280, 28, 28);
                            g.setColor(new Color(25, 33, 47));
                            g.fillRoundRect(0, 0, 180, 280, 28, 28);
                            g.setPaint(
                                    new GradientPaint(
                                            0, 15, new Color(231, 245, 255), 0, 260, Color.WHITE));
                            g.fillRoundRect(9, 12, 162, 254, 20, 20);
                            g.setColor(new Color(25, 33, 47));
                            g.fillRoundRect(60, 18, 60, 7, 7, 7);
                            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
                            g.drawString("BUSSIN", 62, 63);
                            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                            g.setColor(new Color(100, 110, 125));
                            g.drawString("TOTAL TO PAY", 53, 92);
                            g.setFont(new Font("Segoe UI", Font.BOLD, 21));
                            g.setColor(new Color(0, 91, 220));
                            String price = "PHP " + amount.toPlainString();
                            g.drawString(
                                    price, (180 - g.getFontMetrics().stringWidth(price)) / 2, 124);
                            g.setStroke(new BasicStroke(3));
                            for (int radius = 12; radius <= 36; radius += 8)
                                g.drawArc(
                                        90 - radius / 2,
                                        170 - radius / 2,
                                        radius,
                                        radius,
                                        -55,
                                        110);
                            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                            g.drawString("Hold card near screen", 32, 221);
                            g.setColor(new Color(180, 190, 202));
                            g.fillRoundRect(65, 251, 50, 4, 4, 4);
                            g.dispose();
                        }
                    };
            terminal.setBounds(185, 12, 186, 286);
            card.setBounds(18, 162, 220, 132);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            startAnimation();
                        }
                    });
            scene.add(card);
            scene.add(terminal);
            action.setText("Tap Card");
        }
        content.add(scene, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(8, 12));
        footer.setOpaque(false);
        feedback.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        feedback.setPreferredSize(new Dimension(wallet ? 320 : 390, 18));
        feedback.setForeground(new Color(100, 110, 125));
        feedback.setText(
                wallet
                        ? "Click the QR area to scan."
                        : "Click the card to tap the terminal.");
        footer.add(feedback, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        styleButton(action, new Color(225, 0, 45), Color.WHITE);
        styleButton(close, new Color(235, 237, 240), new Color(55, 65, 81));
        action.addActionListener(
                e -> {
                    if (scanned) finish();
                    else startAnimation();
                });
        close.setText("Cancel attempt");
        close.addActionListener(
                e -> {
                    reset();
                    completed.accept(null);
                });
        buttons.add(close);
        buttons.add(action);
        footer.add(buttons, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(wallet ? 150 : 180, 36));
    }

    private void startAnimation() {
        if (locked
                || scanned
                || (animation != null && animation.isRunning())
                || !ready.getAsBoolean()) return;
        completed.accept(null);
        frame = 0;
        action.setEnabled(false);
        feedback.setText(wallet ? "Scanning QR…" : "Reading card…");
        scan.setVisible(wallet);
        animation =
                new Timer(
                        30,
                        e -> {
                            frame++;
                            if (wallet) scan.setLocation(12, 15 + Math.min(frame, 40) * 4);
                            else
                                card.setLocation(
                                        18 + Math.min(frame, 40) * 2,
                                        162 - Math.min(frame, 40) * 2);
                            if (frame == 40) feedback.setText("Processing payment…");
                            if (frame >= 65) {
                                animation.stop();
                                scan.setVisible(false);
                                finish();
                            }
                        });
        animation.start();
    }

    private void finish() {
        reference =
                String.format(
                        java.util.Locale.ROOT,
                        "%016d",
                        new java.security.SecureRandom()
                                .nextLong(1_000_000_000_000_000L, 10_000_000_000_000_000L));
        scanned = true;
        action.setEnabled(false);
        feedback.setText("Approved. Reference filled automatically.");
        completed.accept(reference);
    }

    void setLocked(boolean value) {
        locked = value;
        action.setEnabled(!value && !scanned && (animation == null || !animation.isRunning()));
        close.setEnabled(!value);
    }

    void setCardDetails(String type, String number) {
        cardType = type;
        lastFour = number.matches("[0-9]{4}") ? number : "----";
        card.repaint();
    }

    void setWalletProvider(String provider) {
        if (!wallet) return;
        title.setText("Card".equals(provider) ? "E-Wallet" : provider);
        qrLabel.setIcon(null);
        qrLabel.setText("Click to scan QR");
        if ("GCash".equals(provider)) {
            ImageIcon qr = new ImageIcon("resources/icons/gcashqr.png");
            if (qr.getIconWidth() > 0) {
                qrLabel.setText("");
                qrLabel.setIcon(new ImageIcon(qr.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH)));
            }
        }
    }

    void reset() {
        if (animation != null) animation.stop();
        scanned = false;
        reference = null;
        scan.setVisible(false);
        card.setLocation(18, 162);
        feedback.setText(
                wallet
                        ? "Click the QR area to scan."
                        : "Click the card to tap the terminal.");
        action.setEnabled(!locked);
    }

    @Override
    public void removeNotify() {
        reset();
        super.removeNotify();
    }
}
