package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.AccountDao;
import qpal.model.Account;

public class EditAccountPanel {

    private AccountDao accountDao;
    private AdminManageAccountsPanel parent;
    private int id;

    public EditAccountPanel(AccountDao accountDao, AdminManageAccountsPanel parent, int id) {

        this.accountDao = accountDao;
        this.parent = parent;
        this.id = id;
    }

    public void showDialog() {

        Account account = accountDao.getAccount(id);

        if (account == null) {

            qpal.components.AppDialogs.showMessageDialog(
                    parent, "Account not found.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        panel.setPreferredSize(new Dimension(360, 524));

        JLabel title = new JLabel("Edit Account Details");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(225, 29, 72));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(7));

        JLabel lblSubtitle = new JLabel("Update the selected account details.");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 100, 100));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblSubtitle);
        panel.add(Box.createVerticalStrut(20));

        JLabel lblName = new JLabel("Name");
        lblName.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblName);
        panel.add(Box.createVerticalStrut(7));

        JTextField txtName = new JTextField();

        txtName.setDocument(
                new javax.swing.text.PlainDocument() {

                    @Override
                    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                            throws javax.swing.text.BadLocationException {

                        if (str == null) {
                            return;
                        }

                        if (getLength() + str.length() <= 50) {
                            super.insertString(offs, str, a);

                        } else {
                            txtName.setText("");
                            Toolkit.getDefaultToolkit().beep();
                            qpal.components.AppDialogs.showMessageDialog(
                                    null,
                                    "Name must not exceed 50 characters.",
                                    "Warning!",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
        txtName.setText(account.getName());
        txtName.setBackground(new Color(220, 220, 220));
        txtName.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        txtName.setMaximumSize(new Dimension(300, 38));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtName);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblEmail);
        panel.add(Box.createVerticalStrut(7));

        JTextField txtEmail = new JTextField();

        txtEmail.setDocument(
                new javax.swing.text.PlainDocument() {

                    @Override
                    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                            throws javax.swing.text.BadLocationException {

                        if (str == null) {
                            return;
                        }

                        if (getLength() + str.length() <= 100) {
                            super.insertString(offs, str, a);

                        } else {
                            txtEmail.setText("");
                            Toolkit.getDefaultToolkit().beep();
                            qpal.components.AppDialogs.showMessageDialog(
                                    null,
                                    "Email must not exceed 100 characters.",
                                    "Warning!",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
        txtEmail.setText(account.getEmail());
        txtEmail.setBackground(new Color(220, 220, 220));
        txtEmail.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        txtEmail.setMaximumSize(new Dimension(300, 38));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(7));

        JPasswordField txtPassword = new JPasswordField();

        txtPassword.setDocument(
                new javax.swing.text.PlainDocument() {

                    @Override
                    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                            throws javax.swing.text.BadLocationException {

                        if (str == null) {
                            return;
                        }

                        if (getLength() + str.length() <= 100) {
                            super.insertString(offs, str, a);

                        } else {
                            txtPassword.setText("");
                            Toolkit.getDefaultToolkit().beep();
                            qpal.components.AppDialogs.showMessageDialog(
                                    null,
                                    "Password must not exceed 100 characters.",
                                    "Warning!",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
        txtPassword.setBackground(new Color(220, 220, 220));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        txtPassword.setMaximumSize(new Dimension(300, 38));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblRole = new JLabel("Role");
        lblRole.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblRole);
        panel.add(Box.createVerticalStrut(7));

        JComboBox<String> cmbRole = new JComboBox<>(new String[] {"Admin", "Employee"});

        cmbRole.setBackground(new Color(220, 220, 220));
        cmbRole.setMaximumSize(new Dimension(300, 38));
        cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbRole.setSelectedItem(account.getRole());
        panel.add(cmbRole);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblStatus = new JLabel("Status");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblStatus);
        panel.add(Box.createVerticalStrut(7));

        JComboBox<String> cmbStatus = new JComboBox<>(new String[] {"Active", "Inactive"});

        cmbStatus.setBackground(new Color(220, 220, 220));
        cmbStatus.setMaximumSize(new Dimension(300, 38));
        cmbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbStatus.setSelectedItem(account.getStatus());
        panel.add(cmbStatus);
        panel.add(Box.createVerticalStrut(25));

        JButton btnSave = new JButton("Save Changes");
        btnSave.setBackground(new Color(50, 170, 230));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBackground(new Color(240, 0, 55));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.setMaximumSize(new Dimension(300, 42));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(btnSave);
        buttons.add(btnCancel);
        panel.add(buttons);

        JDialog dialog =
                new JDialog(
                        SwingUtilities.getWindowAncestor(parent),
                        "Edit Account",
                        Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setContentPane(AdminFormStyle.frame(panel));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);

        btnCancel.addActionListener(
                e -> {
                    dialog.dispose();
                });

        btnSave.addActionListener(
                e -> {
                    String name = txtName.getText().trim();

                    String email = txtEmail.getText().trim();

                    String password = new String(txtPassword.getPassword()).trim();

                    String role = cmbRole.getSelectedItem().toString();

                    String status = cmbStatus.getSelectedItem().toString();

                    if (name.isEmpty()) {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Name needs an input.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (!qpal.util.EmailSender.validEmail(email)) {
                        qpal.components.AppDialogs.showMessageDialog(
                                txtEmail,
                                "Enter a valid email address.",
                                "Invalid Email",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (email.isEmpty()) {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Email needs an input.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    boolean duplicate;
                    try {
                        duplicate = accountDao.emailExists(email, account.getID());
                    } catch (java.sql.SQLException ex) {
                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Unable to check the email. Please check the database connection"
                                    + " and retry.",
                                "Manage Accounts",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (duplicate) {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Email already exists.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    account.setName(name);
                    account.setEmail(email);
                    account.setRole(role);
                    account.setStatus(status);

                    if (!password.isEmpty()) {

                        account.setPassword(password);
                    }

                    boolean success = accountDao.updateAccount(account);

                    if (success) {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Account updated successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        parent.loadAccounts();

                    } else {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Failed to update account.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

        dialog.setVisible(true);
    }
}
