package qpal.view.Employee;

import qpal.model.Account;

public final class EmployeeActivityLogPanel extends qpal.view.Admin.AdminActivityLogPanel {
    public EmployeeActivityLogPanel(Account account) { super(employee(account)); }
    private static Account employee(Account account) {
        if (account==null || !"Employee".equalsIgnoreCase(account.getRole())) throw new IllegalArgumentException("Employee account required.");
        return account;
    }
}
