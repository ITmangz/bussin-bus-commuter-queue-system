import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.*;
import javax.swing.*;
import qpal.model.BookingData.TripOption;
import qpal.view.Commuter.*;

public class PassengerCapacityTest {
    static void select(int available) {
        TripCardPanel card = new TripCardPanel(new TripOption(1,"Bus","A","B",
                LocalDate.now(),LocalTime.NOON,BigDecimal.TEN,40,available));
        card.getActionMap().get("selectTrip").actionPerformed(null);
    }
    static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                select(20);
                PassengerDetailsPanel panel = new PassengerDetailsPanel(null);
                Field f = PassengerDetailsPanel.class.getDeclaredField("plusbtn");
                f.setAccessible(true);
                JButton plus = (JButton)f.get(panel);
                Method refresh = PassengerDetailsPanel.class.getDeclaredMethod("updatePassengerCounter");
                refresh.setAccessible(true);
                for (int i=0;i<15;i++) plus.doClick(0);
                check(panel.getPassengerCount()==10 && !plus.isEnabled());
                select(4);
                refresh.invoke(panel);
                check(panel.getPassengerCount()==4 && !plus.isEnabled());
                plus.doClick(0);
                check(panel.getPassengerCount()==4);
                select(1);
                refresh.invoke(panel);
                check(panel.getPassengerCount()==1 && !plus.isEnabled());
                select(0);
                refresh.invoke(panel);
                check(panel.getPassengerCount()==0 && !plus.isEnabled());
                select(4);
                panel.resetInputs();
                check(panel.getPassengerCount()==1 && plus.isEnabled());
                for (int i=0;i<10;i++) plus.doClick(0);
                check(panel.getPassengerCount()==4);
                TripCardPanel.clearSelection();
                panel.resetInputs();
                check(!plus.isEnabled());
                System.out.println("Passenger capacity checks passed");
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });
        System.exit(0);
    }
}
