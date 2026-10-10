import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import qpal.view.Admin.AdminRouteSchedPanel;

public class RouteSchedulePaginationTest {
    static Object get(Object panel, String name) throws Exception {
        Field field = AdminRouteSchedPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(panel);
    }

    static void check(boolean ok) {
        if (!ok) throw new AssertionError();
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        AdminRouteSchedPanel panel =
                                new AdminRouteSchedPanel(true) {
                                    @Override
                                    public void loadTrips() {}
                                };
                        List<Object[]> rows = (List<Object[]>) get(panel, "trips");
                        for (int i = 1; i <= 35; i++)
                            rows.add(
                                    new Object[] {
                                        i,
                                        "Bus",
                                        "Route",
                                        "2026-10-09",
                                        "12:00:00",
                                        "50",
                                        "0/40",
                                        "Scheduled"
                                    });
                        Method load = AdminRouteSchedPanel.class.getDeclaredMethod("loadPage");
                        load.setAccessible(true);
                        load.invoke(panel);
                        JButton next = (JButton) get(panel, "btnNext");
                        JButton previous = (JButton) get(panel, "btnPrev");
                        JButton one = (JButton) get(panel, "btnOne");
                        JButton two = (JButton) get(panel, "btnTwo");
                        JTable table = (JTable) get(panel, "table");
                        next.doClick(0);
                        next.doClick(0);
                        check(one.getText().equals("3") && table.getValueAt(0, 0).equals(21));
                        two.doClick(0);
                        check(
                                two.getText().equals("4")
                                        && table.getRowCount() == 5
                                        && !next.isEnabled());
                        one.doClick(0);
                        check(table.getValueAt(0, 0).equals(21));
                        previous.doClick(0);
                        check(table.getValueAt(0, 0).equals(11));
                        rows.subList(5, rows.size()).clear();
                        load.invoke(panel);
                        check(
                                table.getRowCount() == 5
                                        && !previous.isEnabled()
                                        && !next.isEnabled()
                                        && !two.isVisible());
                        rows.clear();
                        load.invoke(panel);
                        check(
                                table.getRowCount() == 0
                                        && ((JLabel) get(panel, "lblInfo"))
                                                .getText()
                                                .equals("Showing 0 to 0 of 0 trips"));
                        System.out.println(
                                "PASS: page 3/4 navigation, numbered buttons, shrinking results and"
                                    + " empty results");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }
}
