package qpal.view.Login;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ResetPassword {

    public ResetPassword(String email, String token) {

        JFrame rpage = new JFrame();
        rpage.setSize(850, 550);
        rpage.setResizable(false);
        rpage.setLocationRelativeTo(null);

        rpage.setLayout(null);
        rpage.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel leftpanel = new JPanel(null);
        leftpanel.setBounds(0, 0, 425, 550);
        leftpanel.setBackground(Color.WHITE);
        rpage.add(leftpanel);

        JPanel rightpanel = new JPanel(null);
        rightpanel.setBounds(425, 0, 425, 550);
        rightpanel.setBackground(new Color(220, 0, 50));
        rpage.add(rightpanel);

        JLabel rightbg = new JLabel();
        rightbg.setBounds(0, 0, 425, 550);

        String[] rightimages = {
            "resources/icons/RCommuteSmarter.png",
            "resources/icons/RJoinQueue.png",
            "resources/icons/RTrackQueue.png",
            "resources/icons/RKnowGo.png",
            "resources/icons/RBussinExp.png"
        };

        ImageIcon[] backgrounds = new ImageIcon[rightimages.length];

        for (int i = 0; i < rightimages.length; i++) {
            ImageIcon rightpic = new ImageIcon(rightimages[i]);
            Image img = rightpic.getImage().getScaledInstance(425, 550, Image.SCALE_SMOOTH);
            backgrounds[i] = new ImageIcon(img);
        }

        rightbg.setIcon(backgrounds[0]);
        rightpanel.add(rightbg);

        int[] currentImage = {0};

        Timer imageTimer =
                new Timer(
                        5000,
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                currentImage[0]++;

                                if (currentImage[0] >= backgrounds.length) {
                                    currentImage[0] = 0;
                                }

                                rightbg.setIcon(backgrounds[currentImage[0]]);
                            }
                        });

        imageTimer.start();
        rpage.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                imageTimer.stop();
            }
        });
        JLabel label1 = new JLabel("Reset Password");
        label1.setBounds(110, 70, 345, 45);
        label1.setFont(new Font("Segoe UI", Font.BOLD, 30));
        label1.setForeground(Color.BLACK);
        leftpanel.add(label1);

        JLabel subtext1 = new JLabel("Enter and confirm your new password below.");
        subtext1.setBounds(60, 115, 345, 30);
        subtext1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtext1.setForeground(new Color(100, 100, 100));
        leftpanel.add(subtext1);

        JLabel newpasstitle = new JLabel("New Password");
        newpasstitle.setBounds(50, 170, 345, 25);
        newpasstitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        newpasstitle.setForeground(Color.BLACK);
        leftpanel.add(newpasstitle);

        JPasswordField newpasstxt = LoginFieldStyle.passwordField("Enter your New Password");
        newpasstxt.setBounds(50, 200, 345, 40);
        newpasstxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newpasstxt.setForeground(new Color(80, 80, 80));
        newpasstxt.setBackground(new Color(235, 235, 235));
        newpasstxt.setBorder(BorderFactory.createEmptyBorder(5, 42, 5, 45));
        newpasstxt.setEchoChar('•');
        LoginFieldStyle.styleLoginField(newpasstxt);
        leftpanel.add(newpasstxt);

        newpasstxt.setDocument(
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
                            newpasstxt.setText("");
                            Toolkit.getDefaultToolkit().beep();
                            qpal.components.AppDialogs.showMessageDialog(
                                    null,
                                    "Password must not exceed 100 characters.",
                                    "Warning!",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });

        JLabel passicon1 = new JLabel();
        passicon1.setBounds(10, 0, 40, 40);
        ImageIcon lockicon1 = new ImageIcon("resources/icons/lockicon.png");
        Image lcic1 = lockicon1.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        passicon1.setIcon(new ImageIcon(lcic1));
        passicon1.setVisible(true);
        newpasstxt.add(passicon1);

        JLabel eyeopen1 = new JLabel();
        eyeopen1.setBounds(305, 0, 40, 40);
        ImageIcon eyesocic1 = new ImageIcon("resources/icons/opeyeicon.png");
        Image eyop1 = eyesocic1.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeopen1.setIcon(new ImageIcon(eyop1));
        eyeopen1.setVisible(true);
        newpasstxt.add(eyeopen1);

        JLabel eyeclose1 = new JLabel();
        eyeclose1.setBounds(305, 0, 40, 40);
        ImageIcon eyescic1 = new ImageIcon("resources/icons/cleyeicon.png");
        Image eyclo1 = eyescic1.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeclose1.setIcon(new ImageIcon(eyclo1));
        eyeclose1.setVisible(false);
        newpasstxt.add(eyeclose1);

        eyeopen1.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        newpasstxt.setEchoChar((char) 0);

                        eyeopen1.setVisible(false);
                        eyeclose1.setVisible(true);
                    }
                });

        eyeclose1.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        newpasstxt.setEchoChar('•');

                        eyeopen1.setVisible(true);
                        eyeclose1.setVisible(false);
                    }
                });

        JLabel confirmpasstitle = new JLabel("Confirm Password");
        confirmpasstitle.setBounds(50, 250, 345, 25);
        confirmpasstitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        confirmpasstitle.setForeground(Color.BLACK);
        leftpanel.add(confirmpasstitle);

        JPasswordField confirmpasstxt = LoginFieldStyle.passwordField("Confirm your Password");
        confirmpasstxt.setBounds(50, 280, 345, 40);
        confirmpasstxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmpasstxt.setForeground(new Color(80, 80, 80));
        confirmpasstxt.setBackground(new Color(235, 235, 235));
        confirmpasstxt.setBorder(BorderFactory.createEmptyBorder(5, 42, 5, 45));
        confirmpasstxt.setEchoChar('•');
        LoginFieldStyle.styleLoginField(confirmpasstxt);
        leftpanel.add(confirmpasstxt);

        confirmpasstxt.setDocument(
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
                            confirmpasstxt.setText("");
                            Toolkit.getDefaultToolkit().beep();
                            qpal.components.AppDialogs.showMessageDialog(
                                    null,
                                    "Password must not exceed 100 characters.",
                                    "Warning!",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });

        JLabel passicon2 = new JLabel();
        passicon2.setBounds(10, 0, 40, 40);
        ImageIcon lockicon2 = new ImageIcon("resources/icons/lockicon.png");
        Image lcic2 = lockicon2.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        passicon2.setIcon(new ImageIcon(lcic2));
        passicon2.setVisible(true);
        confirmpasstxt.add(passicon2);

        JLabel eyeopen2 = new JLabel();
        eyeopen2.setBounds(305, 0, 40, 40);
        ImageIcon eyesocic2 = new ImageIcon("resources/icons/opeyeicon.png");
        Image eyop2 = eyesocic2.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeopen2.setIcon(new ImageIcon(eyop2));
        eyeopen2.setVisible(true);
        confirmpasstxt.add(eyeopen2);

        JLabel eyeclose2 = new JLabel();
        eyeclose2.setBounds(305, 0, 40, 40);
        ImageIcon eyescic2 = new ImageIcon("resources/icons/cleyeicon.png");
        Image eyclo2 = eyescic2.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeclose2.setIcon(new ImageIcon(eyclo2));
        eyeclose2.setVisible(false);
        confirmpasstxt.add(eyeclose2);

        eyeopen2.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        confirmpasstxt.setEchoChar((char) 0);

                        eyeopen2.setVisible(false);
                        eyeclose2.setVisible(true);
                    }
                });

        eyeclose2.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        confirmpasstxt.setEchoChar('•');

                        eyeopen2.setVisible(true);
                        eyeclose2.setVisible(false);
                    }
                });

        JButton changebtn = new JButton("Change Password");
        changebtn.setBounds(50, 345, 345, 40);
        changebtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        changebtn.setForeground(Color.WHITE);
        changebtn.setBackground(new Color(220, 0, 50));
        changebtn.setFocusPainted(false);
        changebtn.setBorderPainted(false);
        leftpanel.add(changebtn);

        ActionListener btnaction1 =
                e -> {
                    String password = new String(newpasstxt.getPassword()).trim();
                    String confirmation = new String(confirmpasstxt.getPassword()).trim();
                    if (password.isBlank() || confirmation.isBlank()) {
                        qpal.components.AppDialogs.showMessageDialog(
                                rpage,
                                "New Password and Confirm Password are required.",
                                "Reset Password",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!password.equals(confirmation)) {
                        qpal.components.AppDialogs.showMessageDialog(
                                rpage,
                                "Passwords do not match.",
                                "Reset Password",
                                JOptionPane.ERROR_MESSAGE);
                        confirmpasstxt.setText("");
                        return;
                    }
                    if (!changebtn.isEnabled()) return;
                    changebtn.setEnabled(false);
                    qpal.util.UiTask.run(
                            () -> {
                                qpal.util.PasswordResetService.reset(email, token, password);
                                return true;
                            },
                            saved -> {
                                if (!rpage.isDisplayable()) return;
                                qpal.components.AppDialogs.showMessageDialog(
                                        rpage,
                                        "Password changed successfully. You can now log in.",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                                rpage.dispose();
                                new LoginPage();
                            },
                            ex -> {
                                if (!rpage.isDisplayable()) return;
                                changebtn.setEnabled(true);
                                qpal.components.AppDialogs.showMessageDialog(
                                        rpage,
                                        qpal.util.PasswordResetService.errorMessage(ex),
                                        "Reset Password",
                                        JOptionPane.ERROR_MESSAGE);
                            });
                };
        changebtn.addActionListener(btnaction1);

        newpasstxt.addKeyListener(
                new KeyListener() {

                    @Override
                    public void keyPressed(KeyEvent e) {

                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            changebtn.doClick();
                        }
                    }

                    @Override
                    public void keyTyped(KeyEvent e) {}

                    @Override
                    public void keyReleased(KeyEvent e) {}
                });

        confirmpasstxt.addKeyListener(
                new KeyListener() {

                    @Override
                    public void keyPressed(KeyEvent e) {

                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            changebtn.doClick();
                        }
                    }

                    @Override
                    public void keyTyped(KeyEvent e) {}

                    @Override
                    public void keyReleased(KeyEvent e) {}
                });

        JSeparator line1 = new JSeparator();
        line1.setBounds(50, 415, 155, 2);
        line1.setForeground(new Color(150, 150, 150));
        leftpanel.add(line1);

        JLabel ortxt = new JLabel("or");
        ortxt.setBounds(205, 402, 35, 25);
        ortxt.setHorizontalAlignment(SwingConstants.CENTER);
        ortxt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        ortxt.setForeground(new Color(100, 100, 100));
        leftpanel.add(ortxt);

        JSeparator line2 = new JSeparator();
        line2.setBounds(240, 415, 155, 2);
        line2.setForeground(new Color(150, 150, 150));
        leftpanel.add(line2);

        JLabel remembertxt = new JLabel("Remember your password?");
        remembertxt.setBounds(105, 440, 220, 30);
        remembertxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        remembertxt.setForeground(new Color(100, 100, 100));
        leftpanel.add(remembertxt);

        JLabel logintxt = new JLabel("Login");
        logintxt.setBounds(285, 440, 70, 30);
        logintxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logintxt.setForeground(new Color(220, 0, 50));
        logintxt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftpanel.add(logintxt);

        logintxt.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {

                        logintxt.setForeground(new Color(180, 0, 40));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                        logintxt.setForeground(new Color(220, 0, 50));
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        rpage.dispose();
                        new LoginPage();
                    }
                });

        rpage.setVisible(true);
    }
}
