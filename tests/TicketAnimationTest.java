import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.*;
import qpal.model.BookingData.Receipt;
import qpal.view.Commuter.PrintTicketPanel;

public class TicketAnimationTest {
    static Field field(Object object, String name) throws Exception {
        Field f = object.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    static Object get(Object object, String name) throws Exception {
        return field(object, name).get(object);
    }

    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        PrintTicketPanel panel = new PrintTicketPanel();
                        JPanel pages = new JPanel(new CardLayout());
                        JPanel home = new JPanel();
                        pages.add(home, "Home");
                        pages.add(panel, "PrintTicket");
                        ((CardLayout) pages.getLayout()).show(pages, "PrintTicket");
                        Receipt receipt =
                                new Receipt(
                                        1,
                                        "TEST-123",
                                        LocalDate.now(),
                                        42,
                                        "Bus",
                                        "Route",
                                        "Schedule",
                                        "1",
                                        BigDecimal.TEN,
                                        "Cash",
                                        "Pending");
                        panel.showReceipt(receipt);
                        JButton print = (JButton) get(panel, "print");
                        JPanel printer = (JPanel) get(panel, "printer");
                        JButton collect = (JButton) get(printer, "collect");
                        check(print.isEnabled(), "Receipt must enable printing");
                        collect.doClick();
                        check(panel.isVisible(), "Cannot collect before printing");
                        check(
                                ((Component) get(panel, "ticket")).getParent() == panel,
                                "Original ticket preview must remain on page");
                        check(
                                printer.getParent() == null,
                                "Machine must not replace ticket preview");
                        Method start = PrintTicketPanel.class.getDeclaredMethod("startAnimation");
                        start.setAccessible(true);
                        start.invoke(panel);
                        field(panel, "animationStarted")
                                .setLong(panel, System.nanoTime() - 1_000_000_000L);
                        Method tick = PrintTicketPanel.class.getDeclaredMethod("advanceAnimation");
                        tick.setAccessible(true);
                        tick.invoke(panel);
                        check(!collect.isVisible(), "Cannot collect during animation");
                        check(
                                (double) get(panel, "progress") > 0
                                        && (double) get(panel, "progress") < 1,
                                "Ticket must emerge gradually");
                        collect.doClick();
                        check(panel.isVisible(), "Early collection must not reset kiosk");
                        field(panel, "animationStarted")
                                .setLong(panel, System.nanoTime() - 5_000_000_000L);
                        tick.invoke(panel);
                        check(collect.isVisible(), "Successful print must allow collection");
                        check(
                                get(panel, "state").toString().equals("READY"),
                                "Ready state missing");
                        panel.setSize(1000, 650);
                        BufferedImage image =
                                new BufferedImage(1000, 650, BufferedImage.TYPE_INT_RGB);
                        Graphics2D graphics = image.createGraphics();
                        panel.paint(graphics);
                        graphics.dispose();
                        javax.imageio.ImageIO.write(
                                image, "png", new java.io.File("build/ticket-ready.png"));
                        collect.doClick();
                        check(
                                home.isVisible() && !panel.isVisible(),
                                "Collection must return home");
                        check(
                                !print.isEnabled() && !collect.isVisible(),
                                "Reset must clear ticket controls");
                        check(
                                get(get(panel, "ticket"), "receipt") == null,
                                "Reset must clear receipt");
                        check(
                                !((Timer) get(panel, "animation")).isRunning(),
                                "Reset must stop animation");
                        System.out.println("Ticket animation checks passed");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }
}
