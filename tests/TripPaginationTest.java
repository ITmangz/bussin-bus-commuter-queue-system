import javax.swing.*;
import java.lang.reflect.*;
import qpal.view.Commuter.*;

public class TripPaginationTest {
    static Object get(Object target, String name) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }

    static void check(boolean condition) {
        if (!condition) throw new AssertionError();
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        AvailableTripPanel panel = new AvailableTripPanel(null, null);
                        var cards = (java.util.List<TripCardPanel>) get(panel, "tripCards");
                        for (int i = 0; i < 5; i++)
                            cards.add(new TripCardPanel("Bus " + i, "A - B", "12:00", "100", 4));
                        Method render = panel.getClass().getDeclaredMethod("showTripPage");
                        render.setAccessible(true);
                        render.invoke(panel);
                        JButton next = (JButton) get(panel, "nextPage"),
                                prev = (JButton) get(panel, "previousPage");
                        JPanel grid = (JPanel) get(panel, "tripcontainer");
                        check(
                                !prev.isEnabled()
                                        && next.isEnabled()
                                        && grid.getComponent(0) == cards.get(0));
                        cards.get(0).getActionMap().get("selectTrip").actionPerformed(null);
                        next.doClick(0);
                        check(grid.getComponent(0) == cards.get(2));
                        next.doClick(0);
                        check(grid.getComponent(0) == cards.get(4) && !next.isEnabled());
                        prev.doClick(0);
                        prev.doClick(0);
                        check(TripCardPanel.getSelectedCard() == cards.get(0));
                        cards.clear();
                        render.invoke(panel);
                        check(
                                !next.isEnabled()
                                        && !prev.isEnabled()
                                        && grid.getComponentCount() == 0);
                        System.out.println("Trip pagination checks passed");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
        System.exit(0);
    }
}
