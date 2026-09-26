package qpal;

import qpal.view.Commuter.Kiosk;

public class MainKiosk {

    public static void main(String[] args) {
        qpal.util.DepartureService.start();

        Kiosk.main(args);

    }
}
