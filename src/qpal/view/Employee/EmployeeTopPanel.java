package qpal.view.Employee;

import javax.swing.JLabel;
import qpal.model.Account;

public final class EmployeeTopPanel extends qpal.view.Admin.AdminTopPanel {
    public EmployeeTopPanel(Account account,JLabel clock,java.util.function.Consumer<String> navigate) { super(account,clock,navigate); }
}
