package qpal;

import javax.swing.SwingUtilities;
import qpal.view.Commuter.Kiosk;

public class MainKiosk {

    public static void main(String[] args) {
        qpal.util.DepartureService.start();
        SwingUtilities.invokeLater(Kiosk::new);
    }
}
