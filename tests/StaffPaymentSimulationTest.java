import qpal.dao.QueuePaymentDao.Progress;
import qpal.model.BookingData.*;
import qpal.view.Admin.QueuePaymentDialog;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

public class StaffPaymentSimulationTest {
    static Object field(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    interface Checked {
        void run() throws Exception;
    }

    static void edt(Checked task) throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        task.run();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    public static void main(String[] args) throws Exception {
        for (String method : new String[] {"GCash", "Maya", "MariBank", "PayPal", "Card"}) {
            JDialog[] dialog = new JDialog[1];
            CountDownLatch approved = new CountDownLatch(1);
            try {
                edt(
                        () -> {
                            var constructor =
                                    QueuePaymentDialog.class.getDeclaredConstructor(
                                            Component.class,
                                            QueueRow.class,
                                            int.class,
                                            Receipt.class,
                                            Progress.class);
                            constructor.setAccessible(true);
                            var row =
                                    new QueueRow(
                                            1,
                                            1,
                                            12,
                                            "Manila - Baguio",
                                            "BUS-001",
                                            "Tomorrow",
                                            "Demo Passenger",
                                            "Pending",
                                            "Serving",
                                            1);
                            var receipt =
                                    new Receipt(
                                            1,
                                            "DEMO-BOOKING",
                                            LocalDate.now(),
                                            12,
                                            "BUS-001",
                                            "Manila - Baguio",
                                            "Tomorrow",
                                            "1A",
                                            new BigDecimal("500.00"),
                                            method,
                                            "Pending");
                            dialog[0] =
                                    (JDialog)
                                            constructor.newInstance(
                                                    new JPanel(),
                                                    row,
                                                    1,
                                                    receipt,
                                                    new Progress(null, false, false));
                            Object details = field(dialog[0], "details");
                            ((JComboBox<?>) field(details, "providerbox")).setSelectedItem(method.equals("Card") ? "Visa" : method);
                            ((JTextField) field(details, "numberfield"))
                                    .setText(method.equals("PayPal") ? "payer@example.com" : method.equals("Card") ? "1234" : "09123456789");
                            JTextField reference = (JTextField) field(details, "referencefield");
                            reference
                                    .getDocument()
                                    .addDocumentListener(
                                            new DocumentListener() {
                                                public void insertUpdate(DocumentEvent e) {
                                                    if (!reference.getText().isEmpty())
                                                        approved.countDown();
                                                }

                                                public void removeUpdate(DocumentEvent e) {}

                                                public void changedUpdate(DocumentEvent e) {}
                                            });
                            check(
                                    !((JButton) field(dialog[0], "pay")).isEnabled(),
                                    "No payment before approval");
                            ((JButton) field(field(dialog[0], "simulation"), "action")).doClick();
                        });
                check(
                        approved.await(6, TimeUnit.SECONDS),
                        "Animation automatically returns a reference");
                edt(
                        () -> {
                            Object details = field(dialog[0], "details");
                            JTextField reference = (JTextField) field(details, "referencefield");
                            check(
                                    reference.getText().matches("[0-9]{16}"),
                                    "Generated reference populated");
                            check(!reference.isEditable(), "Reference cannot be typed");
                            check(
                                    ((JButton) field(dialog[0], "pay")).isEnabled(),
                                    "Successful animation enables payment");
                            check(
                                    reference
                                            .getText()
                                            .equals(
                                                    ((JTextField)
                                                                    field(
                                                                            dialog[0],
                                                                            "transactionReference"))
                                                            .getText()),
                                    "Displayed and saved references match");
                            dialog[0].validate();
                            var pane = dialog[0].getContentPane();
                            var image =
                                    new BufferedImage(
                                            pane.getWidth(),
                                            pane.getHeight(),
                                            BufferedImage.TYPE_INT_RGB);
                            Graphics2D g = image.createGraphics();
                            pane.printAll(g);
                            g.dispose();
                            ImageIO.write(
                                    image,
                                    "png",
                                    new java.io.File("build/staff-payment-" + method + ".png"));
                            ((JTextField) field(details, "numberfield"))
                                    .setText(method.equals("PayPal") ? "other@example.com" : method.equals("Card") ? "9876" : "09987654321");
                            check(
                                    reference.getText().isEmpty(),
                                    "Editing identity clears reference");
                            check(
                                    !((JButton) field(dialog[0], "pay")).isEnabled(),
                                    "Editing identity revokes approval");
                            Object simulation = field(dialog[0], "simulation");
                            ((JButton) field(simulation, "action")).doClick();
                            ((JButton) field(simulation, "close")).doClick();
                            check(
                                    !((Timer) field(simulation, "animation")).isRunning(),
                                    "Cancel stops animation");
                            check(
                                    reference.getText().isEmpty(),
                                    "Cancelled attempt has no reference");
                            ((JButton) field(simulation, "action")).doClick();
                            ((JComboBox<?>) field(dialog[0], "paymentMethod"))
                                    .setSelectedItem("Cash");
                            check(
                                    !((Timer) field(simulation, "animation")).isRunning(),
                                    "Changing method stops old attempt");
                            check(
                                    ((JTextField) field(dialog[0], "transactionReference"))
                                            .getText()
                                            .isEmpty(),
                                    "Changing method clears reference");
                            ((JComboBox<?>) field(dialog[0], "paymentMethod"))
                                    .setSelectedItem(method.equals("Card") ? "Card" : "GCash");
                            check(
                                    !((JButton) field(dialog[0], "pay")).isEnabled(),
                                    "Changing back needs new approval");
                        });
            } finally {
                edt(
                        () -> {
                            if (dialog[0] != null) dialog[0].dispose();
                        });
            }
        }
        System.out.println(
                "PASS: actual scan/tap timers, auto references, cancellation, detail edits, method"
                    + " switching, previews");
    }
}
