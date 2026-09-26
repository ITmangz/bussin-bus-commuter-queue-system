package qpal;

import qpal.view.Login.*;


public class MainEmpAndAdmin {
    public static void main(String[] args) {
        qpal.util.DepartureService.start();
        new LoginPage();
    }
}