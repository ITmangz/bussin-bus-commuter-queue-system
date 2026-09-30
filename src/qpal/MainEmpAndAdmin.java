package qpal;

import javax.swing.SwingUtilities;
import qpal.view.Login.LoginPage;


public class MainEmpAndAdmin {
    public static void main(String[] args) {
        qpal.util.DepartureService.start();
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
