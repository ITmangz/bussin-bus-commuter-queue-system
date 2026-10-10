import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import qpal.dao.*;
import qpal.model.*;
import qpal.view.Admin.*;
import qpal.view.Employee.*;

public class EmployeeUiTest {
    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    static Object field(Object object, String name) throws Exception {
        Class<?> type = object.getClass();
        while (type != null) {
            try {
                Field f = type.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(object);
            } catch (NoSuchFieldException ex) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    static java.util.List<String> buttons(Container c) {
        var result = new ArrayList<String>();
        for (Component child : c.getComponents()) {
            if (child instanceof AbstractButton b) result.add(b.getText());
            if (child instanceof Container nested) result.addAll(buttons(nested));
        }
        return result;
    }

    static void layout(Container c) {
        c.doLayout();
        for (Component child : c.getComponents())
            if (child instanceof Container nested) layout(nested);
    }

    static void render(JPanel page, String name, Account account) throws Exception {
        JPanel shell = new JPanel(new BorderLayout());
        shell.add(new EmployeeSidebarPanel(p -> {}, () -> {}), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout());
        main.add(
                new EmployeeTopPanel(
                        account, new JLabel("Saturday, October 3, 2026 | 09:30 PM"), p -> {}),
                BorderLayout.NORTH);
        main.add(page, BorderLayout.CENTER);
        shell.add(main, BorderLayout.CENTER);
        shell.setSize(1200, 700);
        layout(shell);
        BufferedImage image = new BufferedImage(1200, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        shell.printAll(g);
        g.dispose();
        ImageIO.write(image, "png", new File("build/" + name + ".png"));
    }

    public static void main(String[] args) throws Exception {
        Account employee =
                new Account(1, "Enzo", "employee@example.test", "", "Employee", "Active");
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        var selection = new EmployeeStationPanel(station -> {});
                        Map<?, ?> choices = (Map<?, ?>) field(selection, "choices");
                        check(choices.size() == 4, "Four station choices");
                        check(
                                !((JButton) field(selection, "confirm")).isEnabled(),
                                "Cannot confirm before choosing available station");
                        for (Object value : choices.values())
                            ((JToggleButton) value).setEnabled(true);
                        ((JToggleButton) choices.values().iterator().next()).doClick();
                        check(
                                ((JButton) field(selection, "confirm")).isEnabled(),
                                "Selection enables confirmation");
                        JToggleButton first = (JToggleButton) choices.values().iterator().next();
                        first.doClick();
                        check(
                                !first.isSelected()
                                        && field(selection, "selected") == null
                                        && !((JButton) field(selection, "confirm")).isEnabled(),
                                "Second click deselects and disables confirmation");
                        first.doClick();
                        var second = (JToggleButton) choices.values().toArray()[1];
                        second.doClick();
                        check(
                                second.isSelected() && !first.isSelected(),
                                "Selecting a different station remains exclusive");
                        for (String image :
                                new String[] {
                                    "lblCounter1Image",
                                    "lblCounter2Image",
                                    "lblGate1Image",
                                    "lblGate2Image"
                                })
                            check(
                                    ((JLabel) field(selection, image)).getIcon() == null,
                                    "Image placeholder starts empty");
                        ((JLabel) field(selection, "status"))
                                .setText("Select an available station to continue.");
                        render(selection, "employee-stations", employee);
                        for (String kind : new String[] {"Payment", "Boarding"})
                            for (int number = 1; number <= 2; number++) {
                                EmployeeStation station = new EmployeeStation(kind, number);
                                EmployeeQueuePanel panel = new EmployeeQueuePanel(station);
                                JComboBox<?> counter = (JComboBox<?>) field(panel, "counter"),
                                        gate = (JComboBox<?>) field(panel, "gate");
                                check(
                                        !counter.isEnabled() && !gate.isEnabled(),
                                        "Station cannot be changed in queue panel");
                                check(
                                        (station.boarding() ? gate : counter).getSelectedIndex()
                                                == number,
                                        "Correct station selected");
                                JTabbedPane tabs = (JTabbedPane) field(panel, "queues");
                                check(
                                        tabs.getSelectedIndex() == (station.boarding() ? 1 : 0),
                                        "Correct queue type");
                                check(
                                        !tabs.isEnabledAt(station.boarding() ? 0 : 1),
                                        "Other queue type disabled");
                            }
                        JPanel bus =
                                new AdminBusPanel(true) {
                                    @Override
                                    public void loadBuses() {}
                                };
                        JPanel route =
                                new AdminRouteSchedPanel(true) {
                                    @Override
                                    public void loadTrips() {}
                                };
                        for (JPanel panel : new JPanel[] {bus, route}) {
                            var texts = buttons(panel);
                            for (String forbidden : new String[] {"Add", "Edit", "Print", "Delete"})
                                check(
                                        !texts.contains(forbidden),
                                        "Read-only panel must omit " + forbidden);
                            check(texts.contains("Search"), "Search retained");
                        }
                        var sidebar = new EmployeeSidebarPanel(p -> {}, () -> {});
                        check(
                                buttons(sidebar).stream()
                                        .noneMatch(
                                                t ->
                                                        t.contains("Revenue")
                                                                || t.contains("Accounts")),
                                "No admin-only sidebar pages");
                        var session =
                                new EmployeeStationDao.Session(
                                        1, new EmployeeStation("Payment", 1), "preview");
                        var navigation = new ArrayList<String>();
                        var dashboard =
                                new EmployeeDashboardPanel(
                                        employee,
                                        session,
                                        () -> navigation.add("queue"),
                                        () -> navigation.add("route"));
                        dashboard.showSummary(
                                new EmployeeDashboardDao.Summary(
                                        67,
                                        new BigDecimal("8350.00"),
                                        0,
                                        8,
                                        null,
                                        java.util.List.of(
                                                new DashboardDao.BoardingTrip(
                                                        "Bus01",
                                                        "PITX → Alfonso",
                                                        "2026-10-04 • 09:00 AM",
                                                        14,
                                                        40),
                                                new DashboardDao.BoardingTrip(
                                                        "Bus02",
                                                        "PITX → Tagaytay",
                                                        "2026-10-04 • 10:00 AM",
                                                        8,
                                                        20)),
                                        null));
                        check(
                                ((JPanel) field(dashboard, "active")).getComponentCount() == 4,
                                "All active buses have cards");
                        check(
                                ((JPanel) field(dashboard, "queue")).getComponentCount() == 2,
                                "Only assigned station card and spacing shown");
                        click(dashboard, "View schedules");
                        click(dashboard, "Manage queues");
                        check(
                                navigation.equals(java.util.List.of("route", "queue")),
                                "Dashboard buttons navigate to the right pages");
                        dashboard.setPreferredSize(new Dimension(980, 880));
                        JPanel preview = new JPanel(new BorderLayout());
                        preview.add(dashboard);
                        renderTall(preview, "employee-dashboard", employee);
                        new EmployeeActivityLogPanel(employee);
                        System.out.println(
                                "PASS: four stations, selection confirmation, all station locks,"
                                    + " read-only controls, employee navigation, dashboard"
                                    + " rendering.");
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }

    static void click(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JButton b && text.equals(b.getText())) b.doClick();
            else if (child instanceof Container c) click(c, text);
        }
    }

    static void renderTall(JPanel page, String name, Account account) throws Exception {
        page.setSize(980, 880);
        layout(page);
        BufferedImage image = new BufferedImage(980, 880, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        page.printAll(g);
        g.dispose();
        ImageIO.write(image, "png", new File("build/" + name + ".png"));
    }
}
