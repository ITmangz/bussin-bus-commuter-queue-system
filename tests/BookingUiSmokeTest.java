import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import qpal.model.BookingData.*;
import qpal.view.Commuter.*;
import qpal.view.Admin.*;

public class BookingUiSmokeTest {
    static Object field(Object object, Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name); f.setAccessible(true); return f.get(object);
    }
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void run(TripOption trip, Receipt receipt) throws Exception {
        AtomicReference<Throwable> asyncFailure = new AtomicReference<>();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> asyncFailure.set(error));
        JPanel[] panels = new JPanel[3];
        SwingUtilities.invokeAndWait(() -> {
            TripDetailsPanel details = new TripDetailsPanel();
            PassengerDetailsPanel passengers = new PassengerDetailsPanel(details);
            TripCardPanel card = new TripCardPanel(trip);
            for (MouseListener listener : card.getMouseListeners()) listener.mouseClicked(
                    new MouseEvent(card, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 10, 10, 1, false));
            SelectSeatsPanel seats = new SelectSeatsPanel(details, passengers);
            PaymentPanel payment = new PaymentPanel(details, passengers);
            JPanel container = new JPanel(new CardLayout());
            container.add(details, "TripDetails");
            container.add(new AvailableTripPanel(details, passengers), "AvailableTrip");
            container.add(passengers, "PassengerDetails");
            container.add(seats, "SelectSeats");
            container.add(new ConfirmTripDetailsPanel(details, passengers, payment), "ConfirmTripDetails");
            container.add(payment, "Payment");
            PrintTicketPanel ticket = new PrintTicketPanel();
            ticket.showReceipt(receipt);
            container.add(ticket, "PrintTicket");
            AdminQueuePanel queue = new AdminQueuePanel();
            AdminRevenuePanel revenue = new AdminRevenuePanel();
            queue.refreshData(); revenue.refreshData();
            panels[0] = seats; panels[1] = queue; panels[2] = revenue;
        });
        long deadline = System.currentTimeMillis() + 10000;
        boolean[] done = {false};
        while (!done[0] && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
            SwingUtilities.invokeAndWait(() -> {
                try {
                    done[0] = (boolean)field(panels[0], SeatSelectionPanel.class, "databaseSeatsLoaded")
                            && !(boolean)field(panels[1], AdminQueuePanel.class, "loading")
                            && !(boolean)field(panels[2], AdminRevenuePanel.class, "loading");
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }
        check(done[0], "Screens must finish loading database records");
        SwingUtilities.invokeAndWait(() -> {
            try {
                JTable queues = (JTable)field(panels[1], AdminQueuePanel.class, "table");
                JTable revenue = (JTable)field(panels[2], AdminRevenuePanel.class, "table");
                check(queues.getRowCount() == 4, "Queue screen must show bookings");
                check(revenue.getRowCount() == 4, "Revenue screen must show payments");
                java.util.List<?> buttons = (java.util.List<?>)field(panels[1], AdminQueuePanel.class, "actionButtons");
                check(buttons.size() == 7, "Queue actions must be connected after removing Undo Call");
                AdminQueuePanel queuePanel = (AdminQueuePanel) panels[1];
                queues.setRowSelectionInterval(0,0);
                JTabbedPane tabs = (JTabbedPane)field(queuePanel,AdminQueuePanel.class,"queues");
                tabs.setSelectedIndex(1);
                check(((JLabel)field(queuePanel,AdminQueuePanel.class,"detailTitle")).getText().equals("Currently Boarding"),"Boarding heading switches");
                JButton complete = (JButton)field(queuePanel,AdminQueuePanel.class,"completeButton");
                check(complete.getText().equals("Complete Boarding") && !complete.isEnabled(),"Empty boarding selection cannot complete a waiting queue");
                check(((JPanel)field(queuePanel,AdminQueuePanel.class,"queueActions")).isVisible(),"Boarding queue actions visible");
                check(tabs.getTitleAt(0).equals("Payment Queue"),"Payment tab renamed");
                java.util.List<?> sideActions = (java.util.List<?>)field(queuePanel,AdminQueuePanel.class,"sideActions");
                check(((JButton)sideActions.get(0)).getParent().getComponentCount()==3,"Gate selector and two boarding queue actions");
                check(((JButton)sideActions.get(1)).getText().equals("Skip Queue"),"Boarding skip action switches");
                tabs.setSelectedIndex(0);
                check(complete.getText().equals("Complete"),"Payment actions restored");
                check(((JButton)sideActions.get(2)).getText().equals("Payment"),"Payment side action restored");
                java.util.List<?> seats = (java.util.List<?>)field(panels[0], SeatSelectionPanel.class, "seats");
                check(seats.size() == 20, "Seat screen uses bus capacity");
                long occupied = seats.stream().map(s -> (SeatSelectionPanel.SeatData)s)
                        .filter(s -> s.getStatus().equals("Reserved")).count();
                check(occupied == 5, "Seat screen shows database reservations");
                for (JPanel panel : panels) { panel.setSize(1100,650); panel.doLayout(); }
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        if (asyncFailure.get() != null) throw new AssertionError("Swing event failure", asyncFailure.get());
        System.out.println("PASS: Swing panels construct and load live seats, queue rows, revenue and action listeners");
    }
}

