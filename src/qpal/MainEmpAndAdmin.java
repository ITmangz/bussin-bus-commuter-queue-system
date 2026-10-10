package qpal;

import javax.swing.SwingUtilities;
import qpal.view.Login.LoginPage;

public class MainEmpAndAdmin {
    public static void main(String[] args) {
        javax.swing.ToolTipManager.sharedInstance().setEnabled(false);
        qpal.util.DepartureService.start();
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
    