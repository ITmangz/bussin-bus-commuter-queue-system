import javax.swing.*;
import java.lang.reflect.*;
import java.awt.*;
import java.math.BigDecimal;
import qpal.view.Commuter.*;

public class DropPointUiTest {
    static Object field(Object o, String n) throws Exception {
        Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o);
    }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                TripDetailsPanel trip = new TripDetailsPanel();
                ((JComboBox<?>) field(trip, "destinationbox")).setSelectedItem("Amadeo");
                DropPointPanel panel = new DropPointPanel(trip);
                Method display = DropPointPanel.class.getDeclaredMethod("displayRoute", String.class, BigDecimal.class);
                display.setAccessible(true);
                display.invoke(panel, "Amadeo", new BigDecimal("50"));
                JPanel options = (JPanel) field(panel, "options");
                check(options.getComponentCount() == 4, "Origin plus three drop-offs");
                JRadioButton origin = (JRadioButton) options.getComponent(0);
                check(!origin.isEnabled(), "PITX cannot be selected");
                origin.doClick();
                check(trip.getDropPoint() == null, "Origin does not change selection");
                String[] stops = {"Silang", "Tagaytay", "Amadeo"};
                String[] fares = {"30.00", "40.00", "50.00"};
                for (int i = 0; i < 3; i++) {
                    JRadioButton choice = (JRadioButton) options.getComponent(i + 1);
                    check(choice.getText().equals(stops[i]), "Stop name");
                    check(((JLabel) choice.getComponent(0)).getText().equals("₱" + fares[i]), "Two decimal places");
                    choice.doClick();
                    check(trip.getDropPoint().equals(stops[i]), "Selected drop-off");
                    check(trip.getSelectedFare().equals(new BigDecimal(fares[i])), "Selected fare");
                    for (int j = 0; j < 3; j++)
                        check(((JRadioButton) options.getComponent(j + 1)).isSelected() == (i == j), "Single selection");
                }
                ((JRadioButton) options.getComponent(2)).doClick();
                ((JRadioButton) options.getComponent(2)).doClick();
                check(trip.getDropPoint() == null && trip.getSelectedFare() == null, "Click selected row to clear stop and fare");
                for (Component row : options.getComponents()) check(!((JRadioButton) row).isSelected(), "No selected rows");
                ((JRadioButton) options.getComponent(2)).doClick();
                check(panel.getBusLabel().getIcon() == null, "Replaceable bus image placeholder");
                layout(panel);
                JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, options);
                check(scroll != null && scroll.getVerticalScrollBar().isVisible(), "Styled scrolling list");
                JRadioButton hover = (JRadioButton) options.getComponent(1);
                Color normal = hover.getBackground();
                hover.getModel().setRollover(true);
                check(!normal.equals(hover.getBackground()), "Hover highlight");
                hover.getModel().setRollover(false);
                var image = new java.awt.image.BufferedImage(1000, 650, java.awt.image.BufferedImage.TYPE_INT_RGB);
                var graphics = image.createGraphics(); panel.printAll(graphics); graphics.dispose();
                javax.imageio.ImageIO.write(image, "png", new java.io.File("build/drop-point.png"));
                display.invoke(panel, "Amadeo", new BigDecimal("100"));
                check(trip.getSelectedFare().equals(new BigDecimal("80.00")), "Admin fare changes update selection");
                display.invoke(panel, "Amadeo", null);
                check(trip.getSelectedFare() == null, "Cannot proceed while fare is unavailable");
                ((JComboBox<?>) field(trip, "destinationbox")).setSelectedItem("Naic");
                check(trip.getDropPoint() == null && trip.getSelectedFare() == null, "Changed route clears selection");
                System.out.println("PASS: drop-off selection, fares, single selection, admin fare updates and route reset");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        System.exit(0);
    }
    static void layout(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) if (child instanceof Container nested) layout(nested);
    }
}

