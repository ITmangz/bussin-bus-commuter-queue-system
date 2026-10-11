package qpal.view.Commuter;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;

/** Payment details form; counter mode receives a read-only reference from the simulation. */
public class PaymentDetailsPanel extends JPanel {
    private final boolean wallet;
    private final JComboBox<String> providerbox;
    private final JTextField numberfield = new JTextField();
    private final JTextField referencefield = new JTextField();
    private final JLabel providererror = new JLabel();
    private final JLabel numbererror = new JLabel();
    private final JLabel referenceerror = new JLabel();
    private final JLabel paymentImageLabel = new JLabel();
    private boolean automaticReference;
    private JLabel numberlabel;

    public PaymentDetailsPanel(boolean wallet) {
        this.wallet = wallet;
        setLayout(null);
        setPreferredSize(new Dimension(860, 290));
        setOpaque(false);

        // ================ FORM =================//
        JPanel formpanel = new JPanel(null);
        formpanel.setBounds(330, 14, 500, 270);
        formpanel.setOpaque(false);
        add(formpanel);

        JLabel providerlabel = new JLabel(wallet ? "E-Wallet Provider *" : "Card Type *");
        providerlabel.setBounds(0, 0, 480, 22);
        providerlabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formpanel.add(providerlabel);

        providerbox =
                new JComboBox<>(
                        wallet
                                ? new String[] {"Select e-wallet provider", "GCash", "Maya", "MariBank", "PayPal"}
                                : new String[] {"Select card type", "Visa", "Mastercard", "Other"});
        providerbox.setBounds(0, 26, 480, 40);
        qpal.components.FormInputStyle.styleCombo(providerbox);
        providerlabel.setLabelFor(providerbox);
        formpanel.add(providerbox);
        styleError(providererror, formpanel, 68);

        numberlabel = new JLabel(wallet ? "Mobile Number *" : "Last Four Card Digits *");
        numberlabel.setBounds(0, 88, 480, 22);
        numberlabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formpanel.add(numberlabel);
        numberfield.setBounds(0, 112, 480, 38);
        numberfield.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        limitInput(numberfield, wallet ? 11 : 4, "[0-9]*");
        numberlabel.setLabelFor(numberfield);
        formpanel.add(numberfield);
        styleError(numbererror, formpanel, 150);

        JLabel referencelabel = new JLabel("Reference Number / Transaction Code *");
        referencelabel.setBounds(0, 176, 480, 22);
        referencelabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formpanel.add(referencelabel);
        referencefield.setBounds(0, 200, 480, 38);
        referencefield.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        limitInput(referencefield, 20, "[A-Za-z0-9]*");
        referencelabel.setLabelFor(referencefield);
        formpanel.add(referencefield);
        styleError(referenceerror, formpanel, 238);

        // ================ IMAGE PLACEHOLDER =================//
        paymentImageLabel.setName(wallet ? "ewalletQrLabel" : "cardImageLabel");
        paymentImageLabel.setBounds(30, 26, 260, 210);
        paymentImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        paymentImageLabel.setOpaque(true);
        paymentImageLabel.setBackground(Color.WHITE);
        paymentImageLabel.setBorder(BorderFactory.createLineBorder(new Color(224, 229, 236)));
        paymentImageLabel
                .getAccessibleContext()
                .setAccessibleName(wallet ? "E-wallet QR image" : "Card payment image");
        // Add your image here later, for example:
        // paymentImageLabel.setIcon(new ImageIcon("resources/icons/your-image.png"));
        add(paymentImageLabel);

        JLabel imagecaption =
                new JLabel(wallet ? "E-Wallet QR Code" : "Card Payment", SwingConstants.CENTER);
        imagecaption.setBounds(30, 242, 260, 24);
        imagecaption.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        imagecaption.setForeground(new Color(85, 94, 108));
        add(imagecaption);
        resetInputs();
        if (wallet) providerbox.addActionListener(e -> {
            boolean paypal = "PayPal".equals(providerbox.getSelectedItem());
            numberlabel.setText(paypal ? "PayPal Email *" : "Mobile Number *");
            numberfield.setText("");
            limitInput(numberfield, paypal ? 254 : 11, paypal ? "[^\\s]*" : "[0-9]*");
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (automaticReference) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
        g.setColor(new Color(224, 229, 236));
        g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
        g.dispose();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (automaticReference) {
            JPanel form = (JPanel) getComponent(0);
            int width = Math.max(1, getWidth() - 30);
            form.setBounds(15, 14, width, 270);
            for (Component component : form.getComponents()) {
                Rectangle bounds = component.getBounds();
                component.setBounds(bounds.x, bounds.y, width, bounds.height);
            }
        }
    }

    private static void styleError(JLabel label, JPanel panel, int y) {
        label.setBounds(0, y, 480, 22);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(225, 0, 45));
        panel.add(label);
    }

    public JLabel getPaymentImageLabel() {
        return paymentImageLabel;
    }

    /** Reuse the kiosk form beside the staff simulation, without its image placeholder. */
    public void configureForCounter(Runnable detailsChanged) {
        automaticReference = true;
        setPreferredSize(new Dimension(350, 290));
        JPanel form = (JPanel) getComponent(0);
        form.setBounds(15, 14, 320, 270);
        for (Component component : form.getComponents()) {
            Rectangle bounds = component.getBounds();
            component.setBounds(bounds.x, bounds.y, 320, bounds.height);
        }
        paymentImageLabel.setVisible(false);
        getComponent(2).setVisible(false);
        referencefield.setEditable(false);
        referencefield.setToolTipText("Filled automatically after a successful scan or tap.");
        providerbox.addActionListener(e -> detailsChanged.run());
        numberfield
                .getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                                detailsChanged.run();
                            }

                            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                                detailsChanged.run();
                            }

                            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                                detailsChanged.run();
                            }
                        });
    }

    public void setTransactionReference(String reference) {
        referencefield.setText(reference == null ? "" : reference);
        referenceerror.setText("");
        mark(referencefield, true);
    }

    public void setInputsEnabled(boolean enabled) {
        providerbox.setEnabled(enabled);
        numberfield.setEnabled(enabled);
    }

    public String selectedProvider() {
        return providerbox.getSelectedIndex() > 0 ? (String) providerbox.getSelectedItem() : "Card";
    }

    public String paymentNumber() {
        return numberfield.getText();
    }

    public boolean validatePaymentDetails() {
        return validateInputs(false);
    }

    public boolean validateInputs() {
        return validateInputs(true);
    }

    private boolean validateInputs(boolean requireReference) {
        String number = numberfield.getText();
        boolean providerValid = providerbox.getSelectedIndex() > 0;
        boolean paypal = wallet && "PayPal".equals(providerbox.getSelectedItem());
        boolean numberValid = paypal ? number.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") : wallet ? number.matches("09[0-9]{9}") : number.matches("[0-9]{4}");
        String reference = referencefield.getText();
        boolean referenceValid = !requireReference || reference.matches("[A-Za-z0-9]{1,20}");
        providererror.setText(
                providerValid
                        ? ""
                        : (wallet
                                ? "Please select an e-wallet provider."
                                : "Please select a card type."));
        String numberMessage = "Card number must contain exactly 4 digits.";
        if (wallet) {
            numberMessage =
                    number.isEmpty()
                            ? "Please enter a valid mobile number."
                            : number.length() != 11
                                    ? "Mobile number must contain 11 digits."
                                    : "Please enter a valid mobile number starting with 09.";
        }
        if (paypal) numberMessage = "Please enter a valid PayPal email address.";
        numbererror.setText(numberValid ? "" : numberMessage);
        String referenceMessage =
                reference.isEmpty()
                        ? (automaticReference
                                ? "Complete a successful scan or tap first."
                                : "Please enter a reference number.")
                        : reference.length() > 20
                                ? "Reference number must not exceed 20 characters."
                                : "Reference number must contain only letters and numbers.";
        referenceerror.setText(referenceValid ? "" : referenceMessage);
        mark(providerbox, providerValid);
        mark(numberfield, numberValid);
        mark(referencefield, referenceValid);
        if (providerValid && numberValid && referenceValid) return true;

        JComponent invalidField =
                !providerValid ? providerbox : !numberValid ? numberfield : referencefield;
        String message =
                !providerValid
                        ? providererror.getText()
                        : !numberValid ? numbererror.getText() : referenceerror.getText();
        showValidationError(message);
        invalidField.requestFocusInWindow();
        return false;
    }

    protected void showValidationError(String message) {
        qpal.components.AppDialogs.showMessageDialog(
                this, message, "Payment Details", JOptionPane.ERROR_MESSAGE);
    }

    // Check the complete proposed value so typing, pasting and selection replacement obey the same
    // rules.
    private static void limitInput(JTextField field, int maxLength, String allowedCharacters) {
        ((AbstractDocument) field.getDocument())
                .setDocumentFilter(
                        new DocumentFilter() {
                            @Override
                            public void insertString(
                                    FilterBypass fb, int offset, String text, AttributeSet attrs)
                                    throws BadLocationException {
                                replace(fb, offset, 0, text, attrs);
                            }

                            @Override
                            public void replace(
                                    FilterBypass fb,
                                    int offset,
                                    int length,
                                    String text,
                                    AttributeSet attrs)
                                    throws BadLocationException {
                                String current =
                                        fb.getDocument().getText(0, fb.getDocument().getLength());
                                String proposed =
                                        current.substring(0, offset)
                                                + (text == null ? "" : text)
                                                + current.substring(offset + length);
                                if (proposed.length() <= maxLength
                                        && proposed.matches(allowedCharacters)) {
                                    fb.replace(offset, length, text, attrs);
                                }
                            }
                        });
    }

    private static void mark(JComponent field, boolean valid) {
        var outline = BorderFactory.createLineBorder(
                valid ? new Color(220, 225, 232) : new Color(225, 0, 45));
        // Combo renderers already supply 10px of horizontal padding.
        if (field instanceof JComboBox<?>) {
            field.setBorder(outline);
            return;
        }
        field.setBorder(
                BorderFactory.createCompoundBorder(
                        outline,
                        BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }

    public void resetInputs() {
        providerbox.setSelectedIndex(0);
        numberfield.setText("");
        referencefield.setText("");
        providererror.setText("");
        numbererror.setText("");
        referenceerror.setText("");
        mark(providerbox, true);
        mark(numberfield, true);
        mark(referencefield, true);
    }
}
