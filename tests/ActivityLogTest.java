import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import qpal.dao.*;
import qpal.model.*;
import qpal.util.DbConnection;
import qpal.view.Admin.AdminActivityLogPanel;

public class ActivityLogTest {
    static void check(boolean ok,String message) { if (!ok) throw new AssertionError(message); }
    static Object field(Object target,String name) throws Exception {
        Field field = AdminActivityLogPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
    static void call(Object target,String name) throws Exception {
        Method method = AdminActivityLogPanel.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }
    static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) if (child instanceof Container container) layout(container);
    }
    public static void main(String[] args) throws Exception {
        Account admin = new Account(1,"Admin User","admin@example.com","","Admin","Active");
        Account employee = new Account(2,"Employee User","employee@example.com","","Employee","Active");
        SwingUtilities.invokeAndWait(() -> {
            try {
                AdminActivityLogPanel panel = new AdminActivityLogPanel(admin) {
                    public void refreshData() {}
                };
                List<Object[]> rows = (List<Object[]>)field(panel,"activities");
                for (int i = 0; i < 23; i++) rows.add(new Object[]{23-i,"2026-10-03 00:30:00",
                        i % 2 == 0 ? "admin@example.com" : "staff@example.com",i % 2 == 0 ? "Admin" : "Employee",
                        "Bus Management",i % 2 == 0 ? "Create" : "Update","Added bus Bus01 with 40 seats."});
                call(panel,"searchActivities");
                DefaultTableModel model = (DefaultTableModel)field(panel,"model");
                check(model.getRowCount()==10,"10 entries per page");
                ((JButton)field(panel,"btnNext")).doClick();
                ((JButton)field(panel,"btnNext")).doClick();
                check(model.getRowCount()==3 && !((JButton)field(panel,"btnNext")).isEnabled(),"Last page");
                check(((JButton)field(panel,"btnOne")).getText().equals("3"),"Numbered page pairs");
                ((JComboBox<?>)field(panel,"cmbAction")).setSelectedItem("Create");
                check(((JLabel)field(panel,"lblInfo")).getText().contains("of 12 activities"),"Action filter");
                Field search = AdminActivityLogPanel.class.getDeclaredField("appliedSearch");
                search.setAccessible(true);
                search.set(panel,"missing");
                call(panel,"searchActivities");
                check(model.getRowCount()==0 && !((JButton)field(panel,"btnPrint")).isEnabled(),"Empty search and disabled print");
                search.set(panel,"staff@example.com");
                ((JComboBox<?>)field(panel,"cmbAction")).setSelectedIndex(0);
                check(((JLabel)field(panel,"lblInfo")).getText().contains("of 11 activities"),"Email search");
                search.set(panel,"");
                call(panel,"searchActivities");
                check(!model.isCellEditable(0,6),"Read-only log");
                ((JLabel)field(panel,"connectionStatus")).setVisible(false);
                Class<?> style = Class.forName("qpal.view.Admin.AdminFormStyle");
                Method inputs = style.getDeclaredMethod("styleInputs",Component.class);
                inputs.setAccessible(true);
                inputs.invoke(null,panel);
                panel.setSize(980,595);
                panel.addNotify();
                layout(panel);
                BufferedImage image = new BufferedImage(980,595,BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                panel.printAll(graphics);
                graphics.dispose();
                ImageIO.write(image,"png",new File("build/activity-log-preview.png"));
                panel.removeNotify();
                System.out.println("PASS: pagination, search, action filter, empty state, read-only rows, layout render");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        if (args.length == 0) return;
        String originalUrl = System.getProperty("qpal.db.url","jdbc:mysql://localhost:3306/qpal");
        String database = "qpal_activity_test_" + Long.toUnsignedString(System.nanoTime());
        try (Connection setup = DbConnection.getConnection(); Statement statement = setup.createStatement()) {
            try (ResultSet columns = setup.getMetaData().getColumns(setup.getCatalog(),null,"activity_logs",null)) {
                java.util.Set<String> names = new java.util.HashSet<>();
                while (columns.next()) names.add(columns.getString("COLUMN_NAME"));
                check(names.containsAll(java.util.Set.of("log_id","created_at","account_id","user_name","email","role","module","action","description")),"Existing local schema is ready");
            }
            statement.executeUpdate("CREATE DATABASE " + database);
            try {
                System.setProperty("qpal.db.url",originalUrl.substring(0,originalUrl.lastIndexOf('/')+1) + database);
                try (Connection connection = DbConnection.getConnection(); Statement sql = connection.createStatement()) {
                    sql.executeUpdate(Files.readString(Path.of("database/activity_logs.sql")));
                    sql.executeUpdate("CREATE TABLE buses(bus_id INT AUTO_INCREMENT PRIMARY KEY,bus_number VARCHAR(100),seat_capacity INT,available_seats INT,bus_status VARCHAR(100))");
                }
                ActivityLogDao dao = new ActivityLogDao();
                check(dao.addActivity(admin,"Authentication","Login","Signed in"),"Insert admin log");
                check(dao.addActivity(employee,"Queue Management","Update","Completed queue #1"),"Insert employee log");
                check(dao.getAllActivities(admin).size()==2,"Admin sees all records");
                List<ActivityLog> own = dao.getAllActivities(employee);
                check(own.size()==1 && own.get(0).getEmail().equals("employee@example.com"),"Employee queries restricted");
                employee.setEmail("new@example.com");
                check(dao.getAllActivities(employee).get(0).getEmail().equals("employee@example.com"),"Historical email and stable account ID");
                ActivityLogDao.setCurrentAccount(admin);
                Bus bus = new Bus();
                bus.setBusNumber("TEST01"); bus.setSeatCapacity(40); bus.setAvailableSeats(40); bus.setBusStatus("Available");
                check(new BusDao().addBus(bus),"Bus action succeeds");
                List<ActivityLog> result = dao.getAllActivities(admin);
                check(result.size()==3 && result.get(0).getAction().equals("Create"),"Successful bus action recorded");
                check(result.get(0).getDescription().contains("TEST01"),"Description identifies bus");
                ActivityLogDao.setCurrentAccount(null);
                new BusDao().addBus(bus);
                check(dao.getAllActivities(admin).size()==3,"No user attribution for unsigned actions");
                boolean rejected = false;
                try { dao.getAllActivities(null); } catch (SQLException expected) { rejected = true; }
                check(rejected,"Unauthenticated log reads rejected");
                System.out.println("PASS: local schema, SQL setup, inserts, access scope, historical email, bus hook, unsigned actions");
            } finally {
                ActivityLogDao.setCurrentAccount(null);
                System.setProperty("qpal.db.url",originalUrl);
                statement.executeUpdate("DROP DATABASE " + database);
            }
        }
    }
}
