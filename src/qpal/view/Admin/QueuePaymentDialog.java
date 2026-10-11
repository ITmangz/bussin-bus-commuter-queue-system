package qpal.view.Admin;

import qpal.dao.*;
import qpal.model.BookingData.*;
import qpal.model.BookingData.QueueRow;
import qpal.model.BookingData.Receipt;
import qpal.util.UiTask;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Payment, receipt collection, and one boarding pass per reserved seat. */
public final class QueuePaymentDialog extends JDialog {
    private final QueueRow row;
    private final int station;
    private Receipt receipt;
    private QueuePaymentDao.Progress progress;
    private final JTextField received = new JTextField();
    private final JLabel change = new JLabel("Change: PHP 0.00");
    private final JLabel status = new JLabel(" ");
    private final JButton pay = new JButton("Mark as Paid");
    private boolean simulationApproved;
    private final JComboBox<String> paymentMethod =
            new JComboBox<>(new String[] {"Cash", "GCash", "Card"});
    private qpal.view.Commuter.PaymentDetailsPanel details;
    private CashlessSimulationPanel simulation;
    private final JPanel paymentBody = new JPanel(new BorderLayout(18, 0));
    private final JPanel paymentHeading = new JPanel(new GridLayout(0, 1, 0, 4));
    private final JTextField transactionReference = new JTextField();

    private boolean cashless() {
        return !"Cash".equals(paymentMethod.getSelectedItem());
    }

    private boolean busy;
    private Window dashboard;
    private boolean dashboardWasVisible;
    private boolean printingView;

    private void hideForPrinting() {
        if (printingView) return;
        printingView = true;
        dashboard = getOwner();
        dashboardWasVisible = dashboard != null && dashboard.isVisible();
        setVisible(false);
        if (dashboardWasVisible) dashboard.setVisible(false);
    }

    private void returnToQueue() {
        if (printingView && dashboardWasVisible) {
            dashboard.setVisible(true);
            dashboard.toFront();
        }
        printingView = false;
        busy = false;
        dispose();
    }

    public static void open(Component parent, QueueRow row, int station, Runnable closed) {
        UiTask.run(
                () ->
                        new Object[] {
                            new BookingDao().receipt(row.bookingId()),
                            new QueuePaymentDao().progress(row.id())
                        },
                data -> {
                    QueuePaymentDialog dialog =
                            new QueuePaymentDialog(
                                    parent,
                                    row,
                                    station,
                                    (Receipt) data[0],
                                    (QueuePaymentDao.Progress) data[1]);
                    dialog.addWindowListener(
                            new java.awt.event.WindowAdapter() {
                                @Override
                                public void windowClosed(java.awt.event.WindowEvent event) {
                                    closed.run();
                                }
                            });
                    dialog.setVisible(true);
                },
                ex -> {
                    qpal.components.AppDialogs.showMessageDialog(parent, ex.getMessage());
                    closed.run();
                });
    }

    public static void openTickets(Component parent, QueueRow row, int station, Runnable closed) {
        UiTask.run(
                () ->
                        new Object[] {
                            new BookingDao().receipt(row.bookingId()),
                            new QueuePaymentDao().progress(row.id())
                        },
                data -> {
                    Receipt receipt = (Receipt) data[0];
                    QueuePaymentDao.Progress progress = (QueuePaymentDao.Progress) data[1];
                    if (!"Paid".equals(receipt.paymentStatus()) || !progress.receiptPrinted()) {
                        qpal.components.AppDialogs.showMessageDialog(
                                parent,
                                "Please finish Payment and collect the payment receipt before"
                                        + " printing boarding tickets.",
                                "Payment Required",
                                JOptionPane.WARNING_MESSAGE);
                        closed.run();
                        return;
                    }
                    QueuePaymentDialog dialog =
                            new QueuePaymentDialog(parent, row, station, receipt, progress);
                    try {
                        dialog.printAnimated(true);
                    } finally {
                        dialog.dispose();
                        closed.run();
                    }
                },
                ex -> {
                    qpal.components.AppDialogs.showMessageDialog(parent, ex.getMessage());
                    closed.run();
                });
    }

    private QueuePaymentDialog(
            Component parent,
            QueueRow row,
            int station,
            Receipt receipt,
            QueuePaymentDao.Progress progress) {
        super(
                SwingUtilities.getWindowAncestor(parent),
                "Payment",
                Dialog.ModalityType.APPLICATION_MODAL);
        this.row = row;
        this.station = station;
        this.receipt = receipt;
        this.progress = progress;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        if (!busy) dispose();
                    }
                });
        setUndecorated(true);
        paymentMethod.setSelectedItem(java.util.Set.of("E-Wallet", "Maya", "MariBank", "PayPal").contains(receipt.method()) ? "GCash" : receipt.method());
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(16, 24, 16, 24));
        JPanel heading = paymentHeading;
        heading.setOpaque(false);
        JLabel title = new JLabel("Payment  /  " + String.format("P%03d", row.number()));
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(225, 29, 72));
        heading.add(title);
        heading.add(new JLabel("Total: PHP " + receipt.total().toPlainString()));
        content.add(heading, BorderLayout.NORTH);
        paymentBody.setOpaque(false);
        content.add(paymentBody, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.add(status, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.putClientProperty("fullWidthActions", true);
        buttons.setOpaque(false);
        buttons.setPreferredSize(new Dimension(420, 40));
        JButton close = new JButton("Close");
        close.addActionListener(
                e -> {
                    if (!busy) dispose();
                });
        for (JButton button : new JButton[] {pay, close}) {
            button.setBackground(button == pay ? new Color(225, 29, 72) : new Color(235, 238, 243));
            button.setForeground(button == pay ? Color.WHITE : new Color(40, 50, 65));
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            buttons.add(button);
        }
        footer.add(buttons, BorderLayout.SOUTH);
        content.add(footer, BorderLayout.SOUTH);
        JScrollPane scroll = new JScrollPane(AdminFormStyle.frame(content));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setContentPane(scroll);
        addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowOpened(java.awt.event.WindowEvent e) {
                        if ("Paid".equals(QueuePaymentDialog.this.receipt.paymentStatus())
                                && !QueuePaymentDialog.this.progress.receiptPrinted())
                            SwingUtilities.invokeLater(() -> printAnimated(false));
                    }
                });
        installAmountFilter();
        received.getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                                updateChange();
                            }

                            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                                updateChange();
                            }

                            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                                updateChange();
                            }
                        });
        paymentMethod.addActionListener(e -> showMethod());
        pay.addActionListener(e -> pay());
        showMethod();
    }

    private void showMethod() {
        paymentHeading.setPreferredSize(new Dimension(cashless() ? 700 : 440, 78));
        if (simulation != null) simulation.reset();
        simulationApproved = false;
        transactionReference.setText("");
        paymentBody.removeAll();
        details = null;
        simulation = null;
        if (cashless()) {
            setReceived(receipt.total().toPlainString());
            details =
                    new qpal.view.Commuter.PaymentDetailsPanel(
                            "GCash".equals(paymentMethod.getSelectedItem()));
            details.configureForCounter(
                    () -> {
                        simulationApproved = false;
                        transactionReference.setText("");
                        details.setTransactionReference(null);
                        if (simulation != null) {
                            simulation.reset();
                            simulation.setCardDetails(
                                    details.selectedProvider(), details.paymentNumber());
                            simulation.setWalletProvider(details.selectedProvider());
                        }
                        updateButtons();
                    });
            paymentBody.setLayout(new GridLayout(1, 2, 16, 0));
            JPanel form = new JPanel();
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createLineBorder(new Color(224, 229, 236)));
            form.add(methodSelector());
            details.setAlignmentX(Component.LEFT_ALIGNMENT);
            details.setMaximumSize(new Dimension(Integer.MAX_VALUE, 290));
            form.add(details);
            paymentBody.add(form);
            simulation =
                    new CashlessSimulationPanel(
                            (String) paymentMethod.getSelectedItem(),
                            receipt.total(),
                            () ->
                                    !busy
                                            && !"Paid".equals(receipt.paymentStatus())
                                            && details.validatePaymentDetails(),
                            reference -> {
                                simulationApproved = reference != null;
                                transactionReference.setText(reference == null ? "" : reference);
                                details.setTransactionReference(reference);
                                updateButtons();
                            });
            if ("GCash".equals(paymentMethod.getSelectedItem())) simulation.setWalletProvider(details.selectedProvider());
            paymentBody.add(simulation);
        } else {
            JPanel cash = new JPanel();
            cash.setOpaque(false);
            cash.setLayout(new BoxLayout(cash, BoxLayout.Y_AXIS));
            cash.add(methodSelector());
            addInput(cash, "Amount received (PHP)", received);
            received.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            cash.add(change);
            setReceived(progress.received() == null ? "" : progress.received().toPlainString());
            paymentBody.add(cash, BorderLayout.CENTER);
        }
        updateChange();
        updateButtons();
        paymentBody.revalidate();
        paymentBody.repaint();
        pack();
        Rectangle screen = getGraphicsConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        setSize(
                Math.min(getWidth() + 4, screen.width - 40),
                Math.min(getHeight(), screen.height - insets.top - insets.bottom - 40));
        setLocationRelativeTo(getOwner());
    }

    private JPanel methodSelector() {
        // Keep the shared padded renderer when the same combo is reparented on method changes.
        if (!Boolean.TRUE.equals(paymentMethod.getClientProperty("adminStyled"))) {
        paymentMethod.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list, "GCash".equals(value) ? "E-Wallet" : value, index, selected, focus);
            }
        });
        paymentMethod.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        qpal.components.FormInputStyle.styleCombo(paymentMethod);
        }
        JPanel selector = new JPanel(new BorderLayout(0, 8));
        selector.setOpaque(false);
        selector.setAlignmentX(Component.LEFT_ALIGNMENT);
        selector.setBorder(new EmptyBorder(14, cashless() ? 15 : 0, 8, cashless() ? 15 : 0));
        JLabel label = new JLabel("Payment method");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setLabelFor(paymentMethod);
        selector.add(label, BorderLayout.NORTH);
        selector.add(paymentMethod, BorderLayout.CENTER);
        selector.setPreferredSize(new Dimension(350, 90));
        selector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        return selector;
    }
    private void installAmountFilter() {
        ((javax.swing.text.AbstractDocument) received.getDocument())
                .setDocumentFilter(
                        new javax.swing.text.DocumentFilter() {
                            @Override
                            public void insertString(
                                    FilterBypass fb,
                                    int offset,
                                    String text,
                                    javax.swing.text.AttributeSet attributes)
                                    throws javax.swing.text.BadLocationException {
                                replace(fb, offset, 0, text, attributes);
                            }

                            @Override
                            public void replace(
                                    FilterBypass fb,
                                    int offset,
                                    int length,
                                    String text,
                                    javax.swing.text.AttributeSet attributes)
                                    throws javax.swing.text.BadLocationException {
                                String current =
                                        fb.getDocument().getText(0, fb.getDocument().getLength());
                                String value =
                                        current.substring(0, offset)
                                                + (text == null ? "" : text)
                                                + current.substring(offset + length);
                                String number = value.replace(",", "");
                                boolean format =
                                        value.matches("[0-9]*(\\.[0-9]{0,2})?")
                                                || value.matches("[0-9]{1,2},[0-9]{0,3}");
                                boolean withinLimit =
                                        number.isEmpty()
                                                || number.equals(".")
                                                || (format
                                                        && new BigDecimal(number)
                                                                        .compareTo(
                                                                                new BigDecimal(
                                                                                        "10000"))
                                                                <= 0);
                                if (value.length() <= 6 && format && withinLimit)
                                    super.replace(fb, offset, length, text, attributes);
                                else Toolkit.getDefaultToolkit().beep();
                            }
                        });
    }

    private void setReceived(String value) {
        var document = (javax.swing.text.AbstractDocument) received.getDocument();
        var filter = document.getDocumentFilter();
        document.setDocumentFilter(null);
        try {
            received.setText(value);
        } finally {
            document.setDocumentFilter(filter);
        }
    }

    private static void addField(JPanel content, String label, String value) {
        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setCaretPosition(0);
        addInput(content, label, field);
    }

    private static void addInput(JPanel content, String label, JTextField field) {
        JLabel caption = new JLabel(label);
        caption.setFont(new Font("SansSerif", Font.BOLD, 13));
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        caption.setLabelFor(field);
        content.add(caption);
        content.add(Box.createVerticalStrut(7));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(new Color(220, 220, 220));
        field.setBorder(new EmptyBorder(8, 10, 8, 10));
        field.setMaximumSize(new Dimension(300, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(field);
        content.add(Box.createVerticalStrut(14));
    }

    private void autoPrintReceipt() {
        printAnimated(false);
    }

    private void updateChange() {
        if (cashless()) {
            change.setText("Exact amount only. No cash change.");
            return;
        }
        try {
            change.setText(
                    "Change: PHP "
                            + QueuePaymentDao.validateReceived(received.getText(), receipt.total())
                                    .subtract(receipt.total())
                                    .toPlainString());
        } catch (IllegalArgumentException ex) {
            change.setText("Enter an amount covering the total fare.");
        }
    }

    private void updateButtons() {
        boolean paid = "Paid".equals(receipt.paymentStatus());
        received.setEditable(!cashless() && !paid && !busy);

        transactionReference.setEditable(false);
        paymentMethod.setEnabled(!paid && !busy);
        if (details != null) details.setInputsEnabled(!paid && !busy);
        if (simulation != null) simulation.setLocked(paid || busy);
        pay.setEnabled(!paid && !busy && (!cashless() || simulationApproved));
        pay.setText("Mark as Paid");

        status.setText(
                progress.ticketsPrinted()
                        ? "Tickets collected. You can now complete the queue."
                        : progress.receiptPrinted()
                                ? "Receipt collected. Use Print Ticket in the queue."
                                : paid
                                        ? "Paid. Print the payment receipt next."
                                        : cashless()
                                                ? (simulationApproved
                                                        ? "Approved. Review the reference,"
                                                              + " then mark as paid."
                                                        : "Complete the details, then scan or tap"
                                                              + " to simulate payment.")
                                                : "Enter payment to continue.");
    }

    private void pay() {
        if (busy || (cashless() && !simulationApproved)) return;
        final BigDecimal amount;
        try {
            amount = QueuePaymentDao.validateReceived(received.getText(), receipt.total());
        } catch (IllegalArgumentException ex) {
            error(ex);
            return;
        }
        final String reference = transactionReference.getText().trim();
        final boolean paymentVerified = simulationApproved;
        final String method = "GCash".equals(paymentMethod.getSelectedItem()) ? details.selectedProvider() : (String) paymentMethod.getSelectedItem();
        if (cashless() && !reference.matches("[A-Za-z0-9-]{1,64}")) {
            error(
                    new IllegalArgumentException(
                            "Enter the merchant or terminal reference (1–64 letters, numbers or"
                                    + " hyphens)."));
            return;
        }
        busy = true;
        updateButtons();
        UiTask.run(
                () -> {
                    new QueuePaymentDao()
                            .pay(row.id(), station, amount, reference, paymentVerified, method);
                    return new Object[] {
                        new BookingDao().receipt(row.bookingId()),
                        new QueuePaymentDao().progress(row.id())
                    };
                },
                data -> {
                    receipt = (Receipt) data[0];
                    progress = (QueuePaymentDao.Progress) data[1];
                    busy = false;
                    updateButtons();
                    autoPrintReceipt();
                },
                ex -> {
                    busy = false;
                    updateButtons();
                    error(ex);
                });
    }

    public static List<String> boardingTickets(Receipt receipt) {
        return java.util.Arrays.stream(receipt.seats().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(
                        seat ->
                                "BUSSIN | BOARDING PASS\n\nQueue: "
                                        + String.format("B%03d", receipt.queueNumber())
                                        + "\nBooking: "
                                        + receipt.reference()
                                        + "\nBus: "
                                        + receipt.bus()
                                        + "\nRoute: "
                                        + receipt.route()
                                        + "\nDeparture: "
                                        + receipt.schedule()
                                        + "\nSeat: "
                                        + seat
                                        + "\n\nOne passenger · Keep this ticket for boarding.")
                .toList();
    }

    private String receiptText() {
        String divider = "===================================";

        return divider
                + "\nBUSSIN PAYMENT RECEIPT\n"
                + divider
                + "\n"
                + receipt.fareBreakdown()
                + "\nTotal Number of Seats: "
                + row.passengers()
                + "\nTotal: PHP "
                + qpal.model.FarePolicy.format(receipt.total())
                + "\nMode of Payment: "
                + receipt.method()
                + "\n"
                + divider
                + (cashless() ? "\nAmount Paid: " : "\nAmount Received: ")
                + (progress.received() == null
                        ? "Not recorded"
                        : "PHP " + money(progress.received()))
                + (cashless()
                        ? ""
                        : "\nChange: "
                                + (progress.received() == null
                                        ? "Not recorded"
                                        : "PHP "
                                                + money(
                                                        progress.received()
                                                                .subtract(receipt.total()))))
                + "\n"
                + divider
                + "\nThank You!\nEnjoy your trip!";
    }

    private static String money(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private void printAnimated(boolean tickets) {
        List<String> pages = tickets ? boardingTickets(receipt) : List.of(receiptText());
        if (tickets && pages.size() != row.passengers()) {
            error(new IllegalStateException("Seat count changed. Reopen Payment before printing."));
            return;
        }
        busy = true;
        updateButtons();
        collect(tickets, pages);
    }

    static void drawDocument(Graphics2D g, String text, double width, double height) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        String[] lines = text.split("\n");
        int widest =
                java.util.Arrays.stream(lines)
                        .mapToInt(s -> g.getFontMetrics().stringWidth(s))
                        .max()
                        .orElse(300);
        double scale =
                Math.min(1, Math.min(width / (widest + 30.0), height / (lines.length * 22.0 + 30)));
        g.scale(scale, scale);
        g.setColor(Color.BLACK);
        int y = 21;
        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], 12, y);
            y += 22;
        }
    }

    private static int drawReceiptPaper(Graphics2D g, String text, int width, int height) {
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color ink = new Color(45, 45, 45),
                muted = new Color(110, 110, 110),
                burgundy = new Color(140, 16, 47);
        int left = 8, right = width - 8;
        g.setColor(burgundy);
        g.setFont(new Font("Serif", Font.BOLD, 19));
        centeredReceiptText(g, "BUSSIN", width, 27);
        g.setColor(muted);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        centeredReceiptText(g, "PAYMENT RECEIPT", width, 46);
        receiptDivider(g, left, right, 64);
        int y = 92;
        boolean paymentSection = false;
        for (String line : text.split("\n")) {
            int colon = line.indexOf(':');
            if (colon < 0) continue;
            String label = line.substring(0, colon), value = line.substring(colon + 1).trim();
            if ((label.equals("Amount Received") || label.equals("Amount Paid"))
                    && !paymentSection) {
                receiptDivider(g, left, right, y - 8);
                y += 20;
                paymentSection = true;
            }
            boolean total = label.equals("Total");
            g.setFont(new Font("SansSerif", total ? Font.BOLD : Font.PLAIN, 11));
            g.setColor(total ? burgundy : ink);
            g.drawString(label, left, y);
            int valueWidth = g.getFontMetrics().stringWidth(value);
            // Long values get their own line instead of colliding with the label.
            if (g.getFontMetrics().stringWidth(label) + valueWidth + 16 > right - left) y += 17;
            g.drawString(value, right - valueWidth, y);
            y += 32;
        }
        receiptDivider(g, left, right, y - 7);
        g.setColor(burgundy);
        g.setFont(new Font("Serif", Font.BOLD, 14));
        centeredReceiptText(g, "Thank You!", width, y + 19);
        g.setColor(muted);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        centeredReceiptText(g, "Enjoy your trip!", width, y + 38);
        return y + 52;
    }

    private static int receiptPaperHeight(String text, int width) {
        var image =
                new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            return drawReceiptPaper(g, text, width - 14, 0) + 20;
        } finally {
            g.dispose();
        }
    }

    private static void centeredReceiptText(Graphics2D g, String text, int width, int y) {
        g.drawString(text, (width - g.getFontMetrics().stringWidth(text)) / 2, y);
    }

    private static void receiptDivider(Graphics2D g, int left, int right, int y) {
        g.setColor(new Color(185, 185, 185));
        Stroke previous = g.getStroke();
        g.setStroke(
                new BasicStroke(
                        1,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10,
                        new float[] {3, 3},
                        0));
        g.drawLine(left, y, right, y);
        g.setStroke(previous);
    }

    private void collect(boolean tickets, List<String> pages) {
        // Independent owner keeps the printer visible while both application windows are hidden.
        JDialog popup =
                new JDialog(
                        (Window) null,
                        "Collect " + (tickets ? "boarding tickets" : "receipt"),
                        Dialog.ModalityType.APPLICATION_MODAL);
        popup.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        popup.setUndecorated(true);
        popup.setBackground(new Color(0, 0, 0, 0));
        popup.getRootPane().setOpaque(false);
        popup.getLayeredPane().setOpaque(false);
        final double[] fraction = {0};
        final int[] current = {0};
        final String[] seats = receipt.seats().split(",\\s*");
        final int paperX = 86, paperY = 99, paperWidth = 220;
        final int paperHeight = tickets ? 592 : receiptPaperHeight(pages.get(0), paperWidth);
        Image machine = new ImageIcon("resources/icons/printmachine.png").getImage();
        JPanel paper =
                new JPanel(null) {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        super.paintComponent(graphics);
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setRenderingHint(
                                RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        // Keep the machine at its existing size; the asset includes transparent
                        // margins.
                        g.drawImage(machine, 0, 0, 420, 672, this);
                        g.clipRect(paperX, paperY, paperWidth, paperHeight);
                        g.translate(paperX, paperY - paperHeight + paperHeight * fraction[0]);
                        java.awt.geom.Path2D sheet = new java.awt.geom.Path2D.Double();
                        sheet.moveTo(0, 0);
                        sheet.lineTo(paperWidth, 0);
                        sheet.lineTo(paperWidth, paperHeight - 3);
                        for (int x = paperWidth; x > 0; x -= 6) {
                            sheet.lineTo(Math.max(0, x - 3), paperHeight);
                            sheet.lineTo(Math.max(0, x - 6), paperHeight - 3);
                        }
                        sheet.closePath();
                        g.setColor(new Color(253, 253, 253));
                        g.fill(sheet);
                        g.clip(sheet);
                        if (tickets) {
                            g.translate(paperWidth, 0);
                            g.rotate(Math.PI / 2);
                            BoardingPass.draw(
                                    g, receipt, seats[current[0]], paperHeight, paperWidth);
                        } else {
                            g.translate(7, 8);
                            drawReceiptPaper(g, pages.get(0), paperWidth - 14, paperHeight - 20);
                        }
                        g.dispose();
                    }
                };
        paper.setOpaque(false);
        paper.setPreferredSize(new Dimension(420, paperY + paperHeight + 19));
        JButton collect = new JButton();
        collect.setEnabled(false);
        collect.setVisible(false);
        collect.setBounds(paperX, paperY, paperWidth, paperHeight);
        collect.setOpaque(false);
        collect.setContentAreaFilled(false);
        collect.setBorderPainted(false);
        collect.setFocusPainted(false);
        collect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        collect.getAccessibleContext()
                .setAccessibleName(
                        "Collect "
                                + (tickets
                                        ? pages.size() + " boarding tickets"
                                        : "payment receipt"));
        paper.add(collect);
        long[] start = {System.nanoTime()};
        Timer timer = new Timer(30, null);
        timer.addActionListener(
                e -> {
                    double elapsedMillis = (System.nanoTime() - start[0]) / 1000000.0;
                    fraction[0] = Math.min(1, elapsedMillis / 1000);
                    paper.repaint();
                    if (fraction[0] >= 1) {
                        timer.stop();
                        collect.setVisible(true);
                        collect.setEnabled(true);
                        collect.requestFocusInWindow();
                    }
                });
        Runnable printNext =
                () -> {
                    fraction[0] = 0;
                    collect.setVisible(false);
                    collect.setEnabled(false);
                    paper.repaint();
                    busy = true;
                    updateButtons();
                    UiTask.run(
                            () -> {
                                new QueuePaymentDao().requireBoardingPayment(row.id());
                                return true;
                            },
                            printed -> {
                                collect.getAccessibleContext()
                                        .setAccessibleName(
                                                "Click to collect boarding ticket "
                                                        + (current[0] + 1)
                                                        + " of "
                                                        + pages.size());
                                start[0] = System.nanoTime();
                                timer.start();
                            },
                            ex -> {
                                error(ex);
                                busy = false;
                                updateButtons();
                                popup.dispose();
                            });
                };
        collect.addActionListener(
                e -> {
                    collect.setEnabled(false);
                    if (tickets && current[0] + 1 < pages.size()) {
                        current[0]++;
                        printNext.run();
                        return;
                    }
                    UiTask.run(
                            () -> {
                                new QueuePaymentDao().printed(row.id(), tickets);
                                return new QueuePaymentDao().progress(row.id());
                            },
                            saved -> {
                                progress = saved;
                                busy = false;
                                updateButtons();
                                popup.dispose();
                                dispose();
                            },
                            ex -> {
                                collect.setEnabled(true);
                                error(ex);
                                popup.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                                popup.addWindowListener(
                                        new java.awt.event.WindowAdapter() {
                                            @Override
                                            public void windowClosed(
                                                    java.awt.event.WindowEvent event) {
                                                busy = false;
                                                updateButtons();
                                            }
                                        });
                            });
                });
        popup.setContentPane(paper);
        popup.pack();
        popup.setLocationRelativeTo(null);
        hideForPrinting();
        try {
            if (tickets) printNext.run();
            else timer.start();
            popup.setVisible(true);
        } finally {
            timer.stop();
            popup.dispose();
            returnToQueue();
        }
    }

    private void error(Exception ex) {
        qpal.components.AppDialogs.showMessageDialog(
                isShowing() ? this : null, ex.getMessage(), "Payment", JOptionPane.WARNING_MESSAGE);
    }
}
