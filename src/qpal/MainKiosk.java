package qpal;

import javax.swing.SwingUtilities;
import qpal.view.Commuter.Kiosk;

public class MainKiosk {

    public static void main(String[] args) {
        javax.swing.ToolTipManager.sharedInstance().setEnabled(false);
        qpal.util.DepartureService.start();
        SwingUtilities.invokeLater(Kiosk::new);
    }
}
