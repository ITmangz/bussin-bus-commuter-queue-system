package qpal.view.Commuter;

import java.awt.*;
import javax.swing.*;

public class Kiosk {

    public static void startOver(JPanel page) {
        int choice = qpal.components.AppDialogs.showConfirmDialog(page,
                "Are you sure you want to start over? All transaction data will be cleared.",
                "Start Over",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);

        if(choice == JOptionPane.YES_OPTION) {
            Container mainpanel = page.getParent();

            if(mainpanel == null) {
                return;
            }

            reset((JPanel)mainpanel,"TripDetails");
        }
    }

    public static void reset(JPanel mainpanel,String nextPage) {
        TripCardPanel.clearSelection();

        for(Component component : mainpanel.getComponents()) {
            if(component instanceof TripDetailsPanel) {
                ((TripDetailsPanel)component).resetInputs();
            } else if(component instanceof PassengerDetailsPanel) {
                ((PassengerDetailsPanel)component).resetInputs();
            } else if(component instanceof SelectSeatsPanel) {
                ((SelectSeatsPanel)component).resetInputs();
            } else if(component instanceof PaymentPanel) {
                ((PaymentPanel)component).resetInputs();
            } else if(component instanceof PrintTicketPanel) {
                ((PrintTicketPanel)component).showReceipt(null);
            }
        }

        CardLayout cardlayout = (CardLayout)mainpanel.getLayout();
        cardlayout.show(mainpanel,nextPage);
    }

    public Kiosk() {
        JFrame kpage = new JFrame();

        kpage.setResizable(false);
        kpage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainpanel = new JPanel(new CardLayout());
        mainpanel.setPreferredSize(new Dimension(1000,650));

        TripDetailsPanel tripDetails = new TripDetailsPanel();
        PassengerDetailsPanel passengerPanel = new PassengerDetailsPanel(tripDetails);
        PaymentPanel paymentPanel = new PaymentPanel(tripDetails,passengerPanel);
        ConfirmTripDetailsPanel confirmTripDetailsPanel = new ConfirmTripDetailsPanel(tripDetails,passengerPanel,paymentPanel);
        SelectSeatsPanel selectSeatsPanel = new SelectSeatsPanel(tripDetails,passengerPanel);

        mainpanel.add(new HomePanel(),"Home");
        mainpanel.add(tripDetails,"TripDetails");
        mainpanel.add(new DropPointPanel(tripDetails),"DropPoint");
        mainpanel.add(new AvailableTripPanel(tripDetails,passengerPanel),"AvailableTrip");
        mainpanel.add(passengerPanel,"PassengerDetails");
        mainpanel.add(selectSeatsPanel,"SelectSeats");
        mainpanel.add(confirmTripDetailsPanel,"ConfirmTripDetails");
        mainpanel.add(paymentPanel,"Payment");
        mainpanel.add(new PrintTicketPanel(),"PrintTicket");

        kpage.add(mainpanel);
        kpage.pack();
        kpage.setLocationRelativeTo(null);
        kpage.setVisible(true);
    }

    public static void main(String[] args) {
        new Kiosk();
    }
}
