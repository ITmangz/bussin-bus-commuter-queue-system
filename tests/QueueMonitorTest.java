import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;
import qpal.view.Admin.*;

public class QueueMonitorTest {
    static Object get(Object o, String name) throws Exception {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        AdminQueuePanel panel = new AdminQueuePanel();
                        var buttons = (java.util.List<JButton>) get(panel, "actionButtons");
                        check(
                                buttons.stream().noneMatch(JButton::isEnabled),
                                "Actions require station selection");
                        JComboBox<?> counter = (JComboBox<?>) get(panel, "counter");
                        counter.setSelectedIndex(1);
                        check(
                                buttons.stream()
                                        .filter(b -> b.getText().equals("Call Next Queue"))
                                        .findFirst()
                                        .get()
                                        .isEnabled(),
                                "Counter unlocks actions");
                        JButton ticket = (JButton) get(panel, "ticketButton");
                        Component[] siblings = ticket.getParent().getComponents();
                        check(
                                ((JButton) siblings[1]).getText().equals("Recall")
                                        && siblings[2] == ticket
                                        && ((JButton) siblings[3]).getText().equals("Complete"),
                                "Ticket positioned between recall and complete");
                        ((JTabbedPane) get(panel, "queues")).setSelectedIndex(1);
                        check(!ticket.isVisible(), "Ticket hidden in boarding");
                        check(
                                buttons.stream().noneMatch(JButton::isEnabled),
                                "Gate selection required separately");
                        counter.setSelectedIndex(0);
                        ((JTabbedPane) get(panel, "queues")).setSelectedIndex(0);
                        check(
                                buttons.stream().noneMatch(JButton::isEnabled),
                                "Clearing counter locks actions again");
                        new QueueMonitor(false);
                        new QueueMonitor(true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        System.out.println(
                "PASS: station locking, ticket placement, tab switching and monitor construction");
    }
}
