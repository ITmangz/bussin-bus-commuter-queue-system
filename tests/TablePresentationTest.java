import qpal.components.*;
import qpal.dao.AccountDao;
import qpal.model.Account;
import qpal.view.Admin.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;

/** Isolated Swing regression checks. No live database or printer is used. */
public class TablePresentationTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("qpal.db.url", "jdbc:table-presentation-test");
        DriverManager.registerDriver(
                new Driver() {
                    public boolean acceptsURL(String url) {
                        return url.equals("jdbc:table-presentation-test");
                    }

                    public Connection connect(String url, Properties properties) {
                        return acceptsURL(url) ? empty(Connection.class) : null;
                    }

                    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) {
                        return new DriverPropertyInfo[0];
                    }

                    public int getMajorVersion() {
                        return 1;
                    }

                    public int getMinorVersion() {
                        return 0;
                    }

                    public boolean jdbcCompliant() {
                        return false;
                    }

                    public java.util.logging.Logger getParentLogger() {
                        return java.util.logging.Logger.getGlobal();
                    }
                });
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        actionsAndPrinting();
                        accountLayout();
                        for (JPanel panel :
                                new JPanel[] {
                                    new AdminBusPanel(),
                                    new AdminRouteSchedPanel(),
                                    new AdminBusPanel(true),
                                    new AdminRouteSchedPanel(true)
                                }) {
                            JTable table = (JTable) field(panel, "table");
                            boolean readOnly = (boolean) field(panel, "readOnly");
                            check(
                                    hasColumn(table, "Actions") != readOnly,
                                    "Respect read-only permissions");
                            check(
                                    buttons(panel, "Add").size() == (readOnly ? 0 : 1),
                                    "Only editable pages have Add");
                            check(buttons(panel, "Print").size() == 1, "Print stays available");
                            render(
                                    panel,
                                    (panel instanceof AdminBusPanel ? "buses" : "trips")
                                            + (readOnly ? "-readonly" : ""),
                                    1040,
                                    780);
                        }
                        for (JPanel panel :
                                new JPanel[] {
                                    new AdminRevenuePanel(),
                                    new AdminActivityLogPanel(null),
                                    new AdminQueuePanel()
                                }) {
                            JTable table = (JTable) field(panel, "table");
                            check(
                                    !hasColumn(table, "Actions"),
                                    "Operational/read-only tables keep their permissions");
                            check(table.getRowHeight() == 42, "Tables share row sizing");
                            render(panel, panel.getClass().getSimpleName(), 1040, 940);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        System.out.println(
                "PASS: row actions, filtered/sorted mapping, refresh cancellation, print snapshot,"
                    + " accounts pagination/filter, permissions and toolbar layout");
    }

    @SuppressWarnings("unchecked")
    private static <T> T empty(Class<T> type) {
        return (T)
                Proxy.newProxyInstance(
                        type.getClassLoader(),
                        new Class<?>[] {type},
                        (p, m, a) -> {
                            Class<?> result = m.getReturnType();
                            if (result == boolean.class) return false;
                            if (result == int.class) return 0;
                            if (result == long.class) return 0L;
                            if (result.isInterface() && result.getPackageName().equals("java.sql"))
                                return empty(result);
                            return null;
                        });
    }

    private static void actionsAndPrinting() throws Exception {
        DefaultTableModel model =
                new DefaultTableModel(new String[] {"ID", "Name", "Actions"}, 0) {
                    public boolean isCellEditable(int row, int column) {
                        return column == 2;
                    }
                };
        model.addRow(new Object[] {10, "First", null});
        model.addRow(new Object[] {20, "Second", null});
        model.addRow(new Object[] {30, "Third", null});
        JTable table = new JTable(model);
        AtomicInteger edited = new AtomicInteger(), deleted = new AtomicInteger();
        TableStyle.apply(table);
        TableActions.install(
                table,
                r -> edited.set((int) model.getValueAt(r, 0)),
                r -> deleted.set((int) model.getValueAt(r, 0)));
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, 10, 0));
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
        check(table.editCellAt(0, 2), "Actions must be editable");
        buttons((Container) table.getEditorComponent(), "Edit").get(0).doClick();
        check(
                edited.get() == 30 && !table.isEditing(),
                "Edit receives sorted model row and ends editor");
        table.editCellAt(1, 2);
        buttons((Container) table.getEditorComponent(), "Delete").get(0).doClick();
        check(deleted.get() == 20, "Delete receives filtered model row");
        JTable print = TableStyle.printSnapshot(table);
        check(
                print.getColumnCount() == 2 && print.getRowCount() == 2,
                "Print excludes Actions and filtered rows");
        check(print.getValueAt(0, 0).equals(30), "Print preserves display order");
        BufferedImage paper = new BufferedImage(800, 1100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = paper.createGraphics();
        check(
                print.getPrintable(JTable.PrintMode.FIT_WIDTH, null, null)
                                .print(graphics, new java.awt.print.PageFormat(), 0)
                        == java.awt.print.Printable.PAGE_EXISTS,
                "Detached snapshot renders a printable page");
        graphics.dispose();
        table.editCellAt(0, 2);
        JButton staleDelete = buttons((Container) table.getEditorComponent(), "Delete").get(0);
        model.setRowCount(0);
        check(!table.isEditing(), "Refresh cancels stale editor");
        staleDelete.doClick();
        check(deleted.get() == 20, "Cancelled editor cannot trigger a stale action");
        check(print.getRowCount() == 2, "Print snapshot survives a refresh");
        JTable draft = new JTable(new DefaultTableModel(new String[] {"Name", "Actions"}, 0));
        DefaultTableModel draftModel = (DefaultTableModel) draft.getModel();
        draftModel.addRow(new Object[] {"Passenger 1", null});
        draftModel.addRow(new Object[] {"Passenger 2", null});
        TableActions.install(draft, null, draftModel::removeRow);
        draft.editCellAt(1, 1);
        check(
                buttons((Container) draft.getEditorComponent(), "Edit").isEmpty(),
                "Inline forms only need Delete");
        buttons((Container) draft.getEditorComponent(), "Delete").get(0).doClick();
        check(draftModel.getRowCount() == 1, "Draft passenger deletion targets its row");
    }

    @SuppressWarnings("unchecked")
    private static void accountLayout() throws Exception {
        AdminManageAccountsPanel panel = new AdminManageAccountsPanel();
        Field dao = panel.getClass().getDeclaredField("accountDao");
        dao.setAccessible(true);
        dao.set(
                panel,
                new AccountDao() {
                    public List<Account> getAllAccounts() {
                        List<Account> accounts = new ArrayList<>();
                        for (int i = 1; i <= 12; i++) {
                            Account a = new Account();
                            a.setID(i);
                            a.setName("Sample Staff " + i);
                            a.setEmail("staff" + i + "@example.com");
                            a.setRole(i % 2 == 0 ? "Admin" : "Employee");
                            a.setStatus(i == 3 ? "Inactive" : "Active");
                            accounts.add(a);
                        }
                        return accounts;
                    }
                });
        panel.loadAccounts();
        JTable table = (JTable) field(panel, "table");
        check(
                !hasColumn(table, "Password") && hasColumn(table, "Actions"),
                "Correct account columns");
        check(
                table.getRowCount() == 10 && table.getValueAt(0, 0).equals(12),
                "Newest first with pagination");
        ((JButton) field(panel, "btnNext")).doClick();
        check(
                table.getRowCount() == 2 && table.getValueAt(0, 0).equals(2),
                "Second page maps correct accounts");
        ((JComboBox<String>) field(panel, "cmbRole")).setSelectedItem("Employee");
        check(
                table.getRowCount() == 6 && table.getValueAt(0, 0).equals(11),
                "Role filter preserves order");
        ((JComboBox<String>) field(panel, "cmbRole")).setSelectedItem("All Roles");
        for (int width : new int[] {790, 1120}) {
            render(panel, "accounts-" + width, width, 780);
            JButton search = buttons(panel, "Search").get(0);
            for (String name : new String[] {"Add", "Print"}) {
                JButton button = buttons(panel, name).get(0);
                check(
                        button.getParent() == search.getParent(),
                        "Toolbar has Search, Add and Print together");
                check(button.getY() == search.getY(), "Toolbar buttons align");
                check(
                        button.getX() + button.getWidth() <= button.getParent().getWidth(),
                        "Toolbar fits viewport");
            }
        }
    }

    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private static boolean hasColumn(JTable table, String name) {
        for (int col = 0; col < table.getColumnCount(); col++)
            if (name.equals(table.getColumnName(col))) return true;
        return false;
    }

    private static List<JButton> buttons(Container root, String text) {
        List<JButton> buttons = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText())) buttons.add(b);
            else if (c instanceof Container container && !(c instanceof JTable))
                buttons.addAll(buttons(container, text));
        }
        return buttons;
    }

    private static void layout(Container root) {
        root.doLayout();
        if (root instanceof JViewport)
            root.dispatchEvent(
                    new java.awt.event.ComponentEvent(
                            root, java.awt.event.ComponentEvent.COMPONENT_RESIZED));
        for (Component c : root.getComponents())
            if (c instanceof Container container) layout(container);
    }

    private static void render(JPanel panel, String name, int width, int height) throws Exception {
        panel.setSize(width, height);
        panel.addNotify();
        layout(panel);
        layout(panel);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.printAll(g);
        g.dispose();
        panel.removeNotify();
        File directory = new File("build/table-previews");
        directory.mkdirs();
        ImageIO.write(image, "png", new File(directory, name + ".png"));
    }

    private static void check(boolean passed, String message) {
        if (!passed) throw new AssertionError(message);
    }
}
