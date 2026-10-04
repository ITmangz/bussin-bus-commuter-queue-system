import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import qpal.model.BookingData.QueueRow;
import qpal.view.Admin.QueueMonitor;

public class GateMonitorQueueTest {
    static String labels(Component component) {
        String result=component instanceof JLabel label ? label.getText()+"\n" : "";
        if(component instanceof Container container)
            for(Component child:container.getComponents()) result+=labels(child);
        return result;
    }
    static void check(boolean value,String message) {
        if(!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                QueueMonitor monitor=new QueueMonitor(true);
                Method render=QueueMonitor.class.getDeclaredMethod("render",java.util.List.class,Map.class,Map.class,Map.class);
                render.setAccessible(true);
                java.util.List<QueueRow> rows=new ArrayList<>();
                for(int i=1;i<=4;i++) rows.add(new QueueRow(i,i,i,"Origin - Destination","Bus0"+i,
                        "2026-10-05 12:00:00","Passenger","Paid","Boarding",1));
                render.invoke(monitor,rows,Map.of(1,1),Map.of(1,10,2,10,3,20,4,30),Map.of(1,10,2,20));
                Field field=QueueMonitor.class.getDeclaredField("upcoming"); field.setAccessible(true);
                JPanel upcoming=(JPanel)field.get(monitor);
                check(upcoming.getComponentCount()==2,"Two gate queue halves");
                String left=labels(upcoming.getComponent(0)),right=labels(upcoming.getComponent(1));
                check(left.contains("B-002") && !left.contains("B-001") && !left.contains("B-003") && !left.contains("B-004"),"Left includes only gate 1 waiting queues");
                check(right.contains("B-003") && !right.contains("B-002") && !right.contains("B-004"),"Right includes only gate 2 waiting queues");
                render.invoke(monitor,rows,Map.of(),Map.of(1,10,2,10,3,20,4,30),Map.of());
                check(labels(upcoming).contains("Awaiting next trip") && !labels(upcoming).contains("B-"),"Unassigned trips are not shown under a gate");
            } catch(ReflectiveOperationException ex) { throw new RuntimeException(ex); }
        });
        System.out.println("PASS: separate gate queues, active call exclusion and empty gates.");
    }
}
