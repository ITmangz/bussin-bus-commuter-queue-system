package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.AccountDao;
import qpal.model.Account;

public class AddAccountPanel {

    private AccountDao accountDao;
    private AdminManageAccountsPanel parent;

    public AddAccountPanel(AccountDao accountDao, AdminManageAccountsPanel parent) {

        this.accountDao = accountDao;
        this.parent = parent;
    }


    public void showDialog() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25,30,25,30));
        panel.setPreferredSize(new Dimension(360,454));


        JLabel title = new JLabel("Add Account");
        title.setFont(new Font("SansSerif",Font.BOLD,18));
        title.setForeground(new Color(225,29,72));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(7));

        JLabel lblSubtitle = new JLabel("Enter the details for a new account.");
        lblSubtitle.setFont(new Font("SansSerif",Font.PLAIN,12));
        lblSubtitle.setForeground(new Color(100,100,100));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblSubtitle);
        panel.add(Box.createVerticalStrut(20));

        JLabel lblName = new JLabel("Name");
        lblName.setFont(new Font("SansSerif",Font.BOLD,13));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblName);
        panel.add(Box.createVerticalStrut(7));

        JTextField txtName = new JTextField();
        txtName.setBackground(new Color(220,220,220));
        txtName.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        txtName.setMaximumSize(new Dimension(300,38));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtName);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("SansSerif",Font.BOLD,13));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblEmail);
        panel.add(Box.createVerticalStrut(7));

        JTextField txtEmail = new JTextField();
        txtEmail.setBackground(new Color(220,220,220));
        txtEmail.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        txtEmail.setMaximumSize(new Dimension(300,38));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtEmail);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("SansSerif",Font.BOLD,13));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(7));

        JPasswordField txtPassword =new JPasswordField();
        txtPassword.setBackground(new Color(220,220,220));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        txtPassword.setMaximumSize(new Dimension(300,38));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(14));

        JLabel lblRole = new JLabel("Role");
        lblRole.setFont(new Font("SansSerif",Font.BOLD,13));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblRole);
        panel.add(Box.createVerticalStrut(7));

        JComboBox<String> cmbRole = new JComboBox<>(
                new String[] {"Admin","Employee"});

        cmbRole.setBackground(new Color(220,220,220));
        cmbRole.setMaximumSize(new Dimension(300,38));
        cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cmbRole);
        panel.add(Box.createVerticalStrut(25));

        JButton btnAdd = new JButton("Add Account");
        btnAdd.setBackground(new Color(0,190,100));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setBackground(new Color(240,0,55));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);

        JPanel buttons = new JPanel(new GridLayout(1,2,12,0));
        buttons.setOpaque(false);
        buttons.setMaximumSize(new Dimension(300,42));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.add(btnAdd);
        buttons.add(btnCancel);
        panel.add(buttons);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),"Add Account", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setContentPane(AdminFormStyle.frame(panel));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);

        btnCancel.addActionListener(e -> {

            dialog.dispose();
        });

        btnAdd.addActionListener(e -> {

            String name = txtName.getText().trim();

            String email = txtEmail.getText().trim();

            String password = new String(txtPassword.getPassword()).trim();

            String role = cmbRole.getSelectedItem().toString();

            if(name.isEmpty()) {

                JOptionPane.showMessageDialog(dialog,"Name needs an input.","Warning",JOptionPane.WARNING_MESSAGE);
                return;
            }


            if(email.isEmpty()) {

                JOptionPane.showMessageDialog(dialog,"Email needs an input.","Warning",JOptionPane.WARNING_MESSAGE);
                return;
            }


            if(password.isEmpty()) {

                JOptionPane.showMessageDialog(dialog,"Password needs an input.","Warning",JOptionPane.WARNING_MESSAGE);
                return;
            }


            if(accountDao.CheckEmail(email)) {

                JOptionPane.showMessageDialog(dialog,"Email already exists.","Warning",JOptionPane.WARNING_MESSAGE);
                return;
            }


            Account account = new Account();

            account.setName(name);
            account.setEmail(email);
            account.setPassword(password);
            account.setRole(role);
            account.setStatus("Active");


            boolean success = accountDao.addAccount(account);

            if(success) {

                JOptionPane.showMessageDialog(dialog,"Account added successfully.","Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                parent.loadAccounts();

            } else {

                JOptionPane.showMessageDialog(dialog,"Failed to add account.","Error",JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}