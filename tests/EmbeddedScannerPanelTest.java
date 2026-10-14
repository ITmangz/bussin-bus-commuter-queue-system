import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;
import qpal.view.Commuter.PassengerDetailsPanel;
import qpal.util.StudentIdScanner;

public class EmbeddedScannerPanelTest {
    static class Panel extends PassengerDetailsPanel {
        StudentIdScanner.Listener listener;
        Panel() { super(null); }
        @Override protected StudentIdScanner.Session openScanner(StudentIdScanner.Listener listener) {
            this.listener = listener; return null;
        }
    }
    static Object field(Object target, String name) throws Exception {
        Class<?> type = target instanceof Panel ? PassengerDetailsPanel.class : target.getClass();
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(target);
    }
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                Panel panel = new Panel();
                int[] counts = (int[]) field(panel, "counts");
                counts[0] = 0; counts[1] = 1;
                Method rebuild = PassengerDetailsPanel.class.getDeclaredMethod("rebuildForms");
                rebuild.setAccessible(true); rebuild.invoke(panel);
                Method show = PassengerDetailsPanel.class.getDeclaredMethod("showDetail", int.class);
                show.setAccessible(true); show.invoke(panel, 0);
                JPanel form = (JPanel) ((java.util.List<?>) field(panel, "passengers")).get(0);
                JTextField id = (JTextField) field(form, "id");
                JButton scan = null;
                for (Component child : form.getComponents())
                    if (child instanceof JButton button && button.getText().equals("Scan ID")) scan = button;
                int windows = Window.getWindows().length;
                scan.doClick(0);
                check(field(panel, "cameraPanel") != null, "Camera replaces form");
                check(!((JButton) field(panel, "next")).isEnabled(), "Cannot advance during scan");
                check(Window.getWindows().length == windows, "No extra windows created");
                panel.listener.frame(new java.awt.image.BufferedImage(640, 360, java.awt.image.BufferedImage.TYPE_INT_RGB));
                panel.listener.status("Hold your ID steady — scanning automatically.");
                SeatLayoutUiTest.render(panel, "embedded-camera-panel");
                panel.listener.result("2025-1058827");
                check(id.getText().equals("2025-1058827") && id.isEditable(), "ID filled and editable");
                check(field(panel, "cameraPanel") == null, "Form restored");
                scan.doClick(0);
                var old = panel.listener;
                ((JButton) field(panel, "back")).doClick(0);
                old.result("202511062");
                check(id.getText().equals("2025-1058827"), "Cancelled scan cannot overwrite field");
                check(((JButton) field(panel, "next")).isEnabled(), "Navigation restored");
                System.out.println("PASS: inline camera, no windows, auto-fill, editable ID, cancellation and stale callback protection.");
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });
    }
}
