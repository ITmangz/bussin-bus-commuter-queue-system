import java.awt.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import qpal.model.*;
import qpal.model.BookingData.TripOption;
import qpal.view.Commuter.*;

public class SeatLayoutUiTest {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static Object field(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target);
    }
    static Object call(Object target, String name) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name); m.setAccessible(true); return m.invoke(target);
    }
    static void render(JPanel panel, String name) throws Exception {
        panel.setSize(1000, 650); layout(panel);
        var image = new java.awt.image.BufferedImage(1000, 650, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics(); panel.printAll(g); g.dispose();
        javax.imageio.ImageIO.write(image, "png", new java.io.File("build/" + name + ".png"));
    }
    static void layout(Container parent) {
        parent.doLayout();
        for (Component c : parent.getComponents()) if (c instanceof Container child) layout(child);
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        for (SeatLayout layout : SeatLayout.values()) {
            for (int i = 1; i <= 44; i++) check(layout.number(layout.label(i)) == i, "Seat label round trip");
            try { layout.number("0A"); throw new AssertionError("Invalid row accepted"); }
            catch (IllegalArgumentException expected) { }
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                PassengerDetailsPanel passengers = new PassengerDetailsPanel(null);
                SeatSelectionPanel seats = new SeatSelectionPanel(null, passengers);
                TripCardPanel card = new TripCardPanel(new TripOption(1, "Test bus", "A", "B",
                        LocalDate.now().plusDays(1), LocalTime.NOON, BigDecimal.TEN, 24, 24));
                card.getActionMap().get("selectTrip").actionPerformed(null);
                int[] counts = (int[]) field(passengers, "counts");
                counts[0] = 1; counts[2] = 1; counts[3] = 1;
                call(passengers, "rebuildForms");
                List<?> forms = (List<?>) field(passengers, "passengers");
                Object pwd = forms.get(2);
                ((JTextField) field(forms.get(1), "id")).setText("SENIOR-123");
                ((JTextField) field(pwd, "id")).setText("12-1234-123-1234567");
                JComboBox<?> disability = (JComboBox<?>) field(pwd, "disability");
                JTextField other = (JTextField) field(pwd, "otherDisability");
                disability.setSelectedItem("Other");
                check(other.isVisible(), "Other field is shown");
                other.setText("  ");
                try {
                    Object valid = call(pwd, "validateFields");
                    check(Boolean.FALSE.equals(valid), "Blank Other is rejected");
                } catch (InvocationTargetException ex) {
                    // Validation opens an error dialog, unavailable in headless mode.
                    if (!(ex.getCause() instanceof HeadlessException)) throw ex;
                }
                other.setText("Specified disability");
                check(Boolean.TRUE.equals(call(pwd, "validateFields")), "Specified Other accepted");
                disability.setSelectedIndex(1);
                check(!other.isVisible(), "Other field hidden for named category");
                disability.setSelectedItem("Other");
                call(passengers, "rebuildForms");
                check(other.getText().equals("Specified disability"), "Other retained on rebuild");
                Method show = PassengerDetailsPanel.class.getDeclaredMethod("showDetail", int.class);
                show.setAccessible(true); show.invoke(passengers, 1);
                render(passengers, "pwd-other");
                for (SeatLayout plan : SeatLayout.values()) {
                    List<SeatSelectionPanel.SeatData> data = new ArrayList<>();
                    for (int i = 1; i <= 24; i++) data.add(new SeatSelectionPanel.SeatData(plan.label(i), "Available"));
                    seats.setSeatData(plan.ordinal() + 1, "Bus · " + plan, 24, data, plan);
                    JPanel grid = (JPanel) field(seats, "seatsPanel");
                    check(grid.getComponentCount() == plan.perRow() * 4, "Four complete rows per page");
                    JButton front = (JButton) grid.getComponent(0);
                    JButton leftLast = (JButton) grid.getComponent(plan.left() - 1);
                    JButton rightFirst = (JButton) grid.getComponent(plan.left());
                    check(rightFirst.getX() - leftLast.getX() - leftLast.getWidth() == 72, "Clear aisle for every layout");
                    JButton lastSeat = (JButton) grid.getComponent(plan.perRow() - 1);
                    check(lastSeat.getX() + lastSeat.getWidth() == 540, "Every layout fills the available width");
                    front.doClick(0);
                    ((JButton) grid.getComponent(1)).doClick(0);
                    check(!((JButton) grid.getComponent(2)).isEnabled(), "No more priority seats than eligible passengers");
                    ((JButton) grid.getComponent(plan.perRow())).doClick(0);
                    check(seats.getSelectedSeatNumbers().equals(List.of("2A", "1A", "1B")), "Priority seats assigned to Senior/PWD, companion gets standard seat");
                    render(seats, "seats-" + plan.code());
                    JButton next = (JButton) field(seats, "btnNext"); next.doClick(0);
                    check(grid.getComponentCount() == 24 - plan.perRow() * 4, "Partial last row preserves capacity");
                }
                passengers.resetInputs(); call(passengers, "rebuildForms");
                List<SeatSelectionPanel.SeatData> data = new ArrayList<>();
                for (int i = 1; i <= 24; i++) data.add(new SeatSelectionPanel.SeatData(SeatLayout.TWO_TWO.label(i), "Available"));
                seats.setSeatData(99, "Regular-only", 24, data);
                check(!((JButton) ((JPanel) field(seats, "seatsPanel")).getComponent(0)).isEnabled(), "Regular cannot select front row");
                System.out.println("PASS: layout geometry, pagination, priority assignments, regular restrictions, Other validation and persistence, rendered UI.");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
