package qpal.view.Commuter;

import java.awt.*;
import java.awt.print.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import qpal.model.BookingData.Receipt;

public class PrintTicketPanel extends JPanel {
    private final QueueTicket ticket = new QueueTicket();
    private final JButton print = new JButton("Print Ticket");

    private enum State {
        IDLE,
        PRINTING,
        READY
    }

    private State state = State.IDLE;
    private final PrinterView printer = new PrinterView();
    private final Timer animation = new Timer(30, e -> advanceAnimation());
    private long animationStarted;
    private double progress;
    private JDialog machinePopup;
    private Window kioskWindow;

    private void advanceAnimation() {
        double elapsedMillis = (System.nanoTime() - animationStarted) / 1000000.0;
        progress = Math.min(1.0, elapsedMillis / 1000);
        if (progress >= 1) {
            state = State.READY;
            print.setText("Collect your ticket in the popup");
            animation.stop();
            printer.collect.setVisible(true);
            printer.collect.requestFocusInWindow();
        }
        printer.repaint();
    }

    public PrintTicketPanel() {
        setLayout(null);
        setBackground(Color.WHITE);
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 1000, 100);
        header.setBackground(new Color(225, 0, 45));
        JLabel bussinlogo = new JLabel(
                new ImageIcon(new ImageIcon("resources/icons/bussinlogokiosk.png")
                        .getImage().getScaledInstance(120, 55, Image.SCALE_SMOOTH)));
        bussinlogo.setBounds(35, 20, 120, 55);
        header.add(bussinlogo);
        JLabel date = new JLabel("", SwingConstants.CENTER);
        JLabel time = new JLabel("", SwingConstants.CENTER);
        date.setBounds(370, 27, 260, 20);
        time.setBounds(370, 47, 260, 20);
        for (JLabel label : new JLabel[] {date, time}) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(Color.WHITE);
            header.add(label);
        }
        Runnable updateClock =
                () -> {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    date.setText(
                            now.format(
                                    DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)));
                    time.setText(
                            now.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)));
                };
        updateClock.run();
        Timer clock = new Timer(1000, e -> updateClock.run());
        addHierarchyListener(
                e -> {
                    if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                        if (isShowing()) {
                            updateClock.run();
                            clock.start();
                        } else clock.stop();
                    }
                });
        add(header);
        JPanel steps = KioskStepsPanel.create(5);
        steps.setBounds(0, 100, 1000, 75);
        add(steps);
        JLabel title = new JLabel("Booking Completed", SwingConstants.CENTER);
        title.setBounds(0, 185, 1000, 45);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(225, 0, 45));
        add(title);
        JLabel subtitle =
                new JLabel(
                        "Print your queue ticket and wait for your number to be called.",
                        SwingConstants.CENTER);
        subtitle.setBounds(0, 230, 1000, 28);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(100, 100, 100));
        add(subtitle);
        ticket.setBounds(300, 278, 400, 278);
        add(ticket);
        styleButton(print, true);
        print.setBounds(0, 590, 1000, 60);
        print.setBorderPainted(false);
        print.setEnabled(false);
        print.addActionListener(e -> printQueueTicket());
        add(print);
    }

    private static void styleButton(JButton button, boolean primary) {
        button.setPreferredSize(new Dimension(230, 48));
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(primary ? new Color(230, 0, 55) : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : new Color(180, 0, 45));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void showReceipt(Receipt receipt) {
        closeMachinePopup();
        animation.stop();
        state = State.IDLE;
        progress = 0;
        ticket.repaint();
        printer.collect.setVisible(false);
        ticket.receipt = receipt;
        print.setText("Print Ticket");
        print.setEnabled(receipt != null);
        printer.repaint();
    }

    private void printQueueTicket() {
        if (ticket.receipt == null || state != State.IDLE) return;
        kioskWindow = SwingUtilities.getWindowAncestor(this);
        machinePopup = new JDialog((Frame) null, "Payment ticket", false);
        machinePopup.setUndecorated(true);
        machinePopup.setBackground(new Color(0, 0, 0, 0));
        machinePopup.getRootPane().setOpaque(false);
        machinePopup.getLayeredPane().setOpaque(false);
        machinePopup.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        machinePopup.setResizable(false);
        printer.setPreferredSize(new Dimension(500, 800));
        machinePopup.setContentPane(printer);
        machinePopup.setSize(500, 800);
        machinePopup.setLocationRelativeTo(kioskWindow);
        if (kioskWindow != null) kioskWindow.setVisible(false);
        startAnimation();
        machinePopup.setVisible(true);
    }

    private void startAnimation() {
        print.setEnabled(false);
        state = State.PRINTING;
        progress = 0;
        printer.collect.setVisible(false);
        print.setText("Ticket is printing...");
        animationStarted = System.nanoTime();
        animation.start();
    }

    private void closeMachinePopup() {
        if (machinePopup != null) {
            machinePopup.dispose();
            machinePopup = null;
        }
    }

    @Override
    public void removeNotify() {
        animation.stop();
        closeMachinePopup();
        super.removeNotify();
    }

    private class PrinterView extends JPanel {
        private final JButton collect = new JButton("Collect payment ticket");
        private final Image machineImage = new ImageIcon("resources/icons/tixmach.png").getImage();

        PrinterView() {
            setOpaque(false);
            setLayout(null);
            collect.setBounds(115, 340, 270, 188);
            collect.setContentAreaFilled(false);
            collect.setBorderPainted(false);
            collect.setText("");
            collect.setFocusPainted(false);
            collect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            collect.getAccessibleContext().setAccessibleName("Grab payment ticket and return home");
            collect.setVisible(false);
            collect.addActionListener(
                    e -> {
                        if (state == State.READY
                                && PrintTicketPanel.this.getParent() instanceof JPanel) {
                            Kiosk.reset((JPanel) PrintTicketPanel.this.getParent(), "Home");
                            if (kioskWindow != null) {
                                kioskWindow.setVisible(true);
                                kioskWindow.toFront();
                            }
                        }
                    });
            add(collect);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(machineImage, 0, 0, 500, 800, this);
            if (state == State.PRINTING) {
                int phase = (int) ((System.nanoTime() - animationStarted) / 90_000_000L) % 8;
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    g.setColor(new Color(95, 99, 105, 55 + ((i - phase + 8) % 8) * 28));
                    g.fillOval(
                            243 + (int) (32 * Math.cos(angle)),
                            139 + (int) (32 * Math.sin(angle)),
                            14,
                            14);
                }
            } else {
                QueueTicket.centered(
                        g,
                        state == State.READY ? "READY TO COLLECT" : "YOUR PAYMENT TICKET",
                        250,
                        150,
                        22,
                        Font.BOLD,
                        new Color(185, 0, 40),
                        350);
            }
            QueueTicket.centered(
                    g,
                    state == State.PRINTING
                            ? "Ticket is printing..."
                            : state == State.READY
                                    ? "Please grab your payment ticket."
                                    : "Press Print Ticket to begin",
                    250,
                    222,
                    20,
                    Font.PLAIN,
                    new Color(195, 0, 40),
                    350);
            if (progress > 0) {
                Graphics2D paper = (Graphics2D) g.create();
                paper.clipRect(115, 340, 270, 188);
                paper.translate(115, 335 - 188 + 188 * progress);
                paper.scale(270.0 / QueueTicket.WIDTH, 188.0 / QueueTicket.HEIGHT);
                ticket.drawTicket(paper);
                paper.dispose();
            }
            g.dispose();
        }
    }

    // Detailed receipt printing remains available at the admin counter.
    public static void printTicket(Component parent, String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        try {
            area.print();
        } catch (PrinterException ex) {
            showPrintError(parent);
        }
    }

    private static void showPrintError(Component parent) {
        qpal.components.AppDialogs.showMessageDialog(
                parent,
                "Unable to print. Your booking is saved and can be reprinted at the counter.",
                "Printing unavailable",
                JOptionPane.WARNING_MESSAGE);
    }

    private static class QueueTicket extends JPanel {
        static final int WIDTH = 560, HEIGHT = 390;
        private final JLabel bussinlogo = new JLabel(
                new ImageIcon(new ImageIcon("resources/icons/bussinlogokiosk.png")
                        .getImage().getScaledInstance(180, 82, Image.SCALE_SMOOTH)));
        private Receipt receipt;

        QueueTicket() {
            setOpaque(false);
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            double scale = Math.min(getWidth() / (double) WIDTH, getHeight() / (double) HEIGHT);
            g.translate((getWidth() - WIDTH * scale) / 2, (getHeight() - HEIGHT * scale) / 2);
            g.scale(scale, scale);
            drawTicket(g);
            g.dispose();
        }

        void drawTicket(Graphics2D g) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setPaint(
                    new GradientPaint(0, 0, new Color(237, 0, 58), 0, 114, new Color(165, 14, 48)));
            g.fillRect(0, 0, WIDTH, 114);
            if (bussinlogo.getIcon() != null) bussinlogo.getIcon().paintIcon(this, g, 190, 16);

            centered(
                    g,
                    "QUEUE NUMBER",
                    WIDTH / 2,
                    158,
                    21,
                    Font.BOLD,
                    new Color(85, 85, 85),
                    WIDTH - 32);
            String number =
                    receipt == null
                            ? "—"
                            : String.format(Locale.US, "P%03d", receipt.queueNumber());
            centered(g, number, WIDTH / 2, 278, 126, Font.BOLD, new Color(210, 0, 50), WIDTH - 48);
            String instruction =
                    receipt != null && "Paid".equalsIgnoreCase(receipt.paymentStatus())
                            ? "PLEASE WAIT FOR YOUR QUEUE NUMBER TO BE CALLED"
                            : "GO TO THE PAYMENT COUNTER AND PLEASE WAIT FOR YOUR CALL";
            centered(
                    g,
                    instruction,
                    WIDTH / 2,
                    317,
                    13,
                    Font.PLAIN,
                    new Color(85, 85, 85),
                    WIDTH - 36);
            g.setColor(new Color(215, 215, 215));
            g.drawLine(0, 345, WIDTH, 345);
            g.drawLine(WIDTH / 2, 354, WIDTH / 2, HEIGHT - 6);
            g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
            footer(
                    g,
                    "Transaction Date:",
                    receipt == null
                            ? "—"
                            : receipt.queueDate()
                                    .format(
                                            DateTimeFormatter.ofPattern(
                                                    "MMMM d, yyyy", Locale.ENGLISH)),
                    WIDTH / 4);
            footer(
                    g,
                    "Ticket Reference:",
                    receipt == null ? "—" : receipt.reference(),
                    WIDTH * 3 / 4);
        }

        private static void footer(Graphics2D g, String title, String value, int center) {
            centered(g, title, center, 363, 12, Font.BOLD, new Color(85, 85, 85), WIDTH / 2 - 20);
            centered(g, value, center, 381, 12, Font.PLAIN, new Color(85, 85, 85), WIDTH / 2 - 20);
        }

        private static void centered(
                Graphics2D g,
                String text,
                int center,
                int baseline,
                int size,
                int weight,
                Color color,
                int maxWidth) {
            Font font = new Font("Segoe UI", weight, size);
            while (g.getFontMetrics(font).stringWidth(text) > maxWidth && size > 8)
                font = font.deriveFont((float) --size);
            g.setFont(font);
            g.setColor(color);
            g.drawString(text, center - g.getFontMetrics().stringWidth(text) / 2, baseline);
        }
    }
}
