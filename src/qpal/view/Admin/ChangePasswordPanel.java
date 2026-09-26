package qpal.view.Admin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChangePasswordPanel {

    public ChangePasswordPanel(JDialog owner, java.util.function.Consumer<String> onPasswordChanged) {

        JDialog rpage = new JDialog(owner,"Change Password",Dialog.ModalityType.APPLICATION_MODAL);
        rpage.setSize(360,330);
        rpage.setUndecorated(true);
        rpage.setResizable(false);
        rpage.setLocationRelativeTo(owner);
        rpage.setLayout(null);
        rpage.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel leftpanel = new JPanel(null);
        leftpanel.setBounds(0,0,360,330);
        leftpanel.setBackground(Color.WHITE);
        rpage.add(leftpanel);

        JLabel label1 = new JLabel("Change Password");
        label1.setBounds(30,25,300,25);
        label1.setFont(new Font("SansSerif",Font.BOLD,18));
        label1.setForeground(new Color(225,29,72));
        leftpanel.add(label1);

        JLabel subtext1 = new JLabel("<html>Enter and confirm your new password below.</html>");
        subtext1.setBounds(30,55,300,32);
        subtext1.setFont(new Font("SansSerif",Font.PLAIN,13));
        subtext1.setForeground(new Color(100, 100, 100));
        leftpanel.add(subtext1);

        JLabel newpasstitle = new JLabel("New Password");
        newpasstitle.setBounds(30,100,300,18);
        newpasstitle.setFont(new Font("SansSerif",Font.BOLD,13));
        newpasstitle.setForeground(Color.BLACK);
        leftpanel.add(newpasstitle);

        JPasswordField newpasstxt = new JPasswordField();
        newpasstxt.setBounds(30,125,300,38);
        newpasstxt.setFont(new Font("SansSerif",Font.PLAIN,13));
        newpasstxt.setForeground(new Color(80, 80, 80));
        newpasstxt.setBackground(new Color(220,220,220));
        newpasstxt.setBorder(BorderFactory.createEmptyBorder(5, 42, 5, 45));
        newpasstxt.setEchoChar('•');
        leftpanel.add(newpasstxt);

            newpasstxt.setDocument(new javax.swing.text.PlainDocument() {

                @Override
                public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {

                    if (str == null) {
                        return;
                    }

                    if (getLength() + str.length() <= 100) {
                        super.insertString(offs, str, a);

                    } else {

                        Toolkit.getDefaultToolkit().beep();
                        qpal.components.AppDialogs.showMessageDialog(rpage, "Password must not exceed 100 characters.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });

        JLabel passicon1 = new JLabel();
        passicon1.setBounds(10,0,30,38);
        ImageIcon lockicon1 = new ImageIcon("resources/icons/lockicon.png");
        Image lcic1 = lockicon1.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        passicon1.setIcon(new ImageIcon(lcic1));
        passicon1.setVisible(true);
        newpasstxt.add(passicon1);

        JLabel eyeopen1 = new JLabel();
        eyeopen1.setBounds(265,0,30,38);
        ImageIcon eyesocic1 = new ImageIcon("resources/icons/opeyeicon.png");
        Image eyop1 = eyesocic1.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeopen1.setIcon(new ImageIcon(eyop1));
        eyeopen1.setVisible(true);
        newpasstxt.add(eyeopen1);

        JLabel eyeclose1 = new JLabel();
        eyeclose1.setBounds(265,0,30,38);
        ImageIcon eyescic1 = new ImageIcon("resources/icons/cleyeicon.png");
        Image eyclo1 = eyescic1.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeclose1.setIcon(new ImageIcon(eyclo1));
        eyeclose1.setVisible(false);
        newpasstxt.add(eyeclose1);

            eyeopen1.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    newpasstxt.setEchoChar((char) 0);

                    eyeopen1.setVisible(false);
                    eyeclose1.setVisible(true);
                }
            });

            eyeclose1.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    newpasstxt.setEchoChar('•');

                    eyeopen1.setVisible(true);
                    eyeclose1.setVisible(false);
                }
            });

        JLabel confirmpasstitle = new JLabel("Confirm Password");
        confirmpasstitle.setBounds(30,180,300,18);
        confirmpasstitle.setFont(new Font("SansSerif",Font.BOLD,13));
        confirmpasstitle.setForeground(Color.BLACK);
        leftpanel.add(confirmpasstitle);

        JPasswordField confirmpasstxt = new JPasswordField();
        confirmpasstxt.setBounds(30,205,300,38);
        confirmpasstxt.setFont(new Font("SansSerif",Font.PLAIN,13));
        confirmpasstxt.setForeground(new Color(80, 80, 80));
        confirmpasstxt.setBackground(new Color(220,220,220));
        confirmpasstxt.setBorder(BorderFactory.createEmptyBorder(5, 42, 5, 45));
        confirmpasstxt.setEchoChar('•');
        leftpanel.add(confirmpasstxt);

            confirmpasstxt.setDocument(new javax.swing.text.PlainDocument() {

                @Override
                public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {

                    if (str == null) {
                        return;
                    }

                    if (getLength() + str.length() <= 100) {
                        super.insertString(offs, str, a);

                    } else {

                        Toolkit.getDefaultToolkit().beep();
                        qpal.components.AppDialogs.showMessageDialog(rpage, "Password must not exceed 100 characters.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });

        JLabel passicon2 = new JLabel();
        passicon2.setBounds(10,0,30,38);
        ImageIcon lockicon2 = new ImageIcon("resources/icons/lockicon.png");
        Image lcic2 = lockicon2.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        passicon2.setIcon(new ImageIcon(lcic2));
        passicon2.setVisible(true);
        confirmpasstxt.add(passicon2);

        JLabel eyeopen2 = new JLabel();
        eyeopen2.setBounds(265,0,30,38);
        ImageIcon eyesocic2 = new ImageIcon("resources/icons/opeyeicon.png");
        Image eyop2 = eyesocic2.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeopen2.setIcon(new ImageIcon(eyop2));
        eyeopen2.setVisible(true);
        confirmpasstxt.add(eyeopen2);

        JLabel eyeclose2 = new JLabel();
        eyeclose2.setBounds(265,0,30,38);
        ImageIcon eyescic2 = new ImageIcon("resources/icons/cleyeicon.png");
        Image eyclo2 = eyescic2.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeclose2.setIcon(new ImageIcon(eyclo2));
        eyeclose2.setVisible(false);
        confirmpasstxt.add(eyeclose2);

            eyeopen2.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    confirmpasstxt.setEchoChar((char) 0);

                    eyeopen2.setVisible(false);
                    eyeclose2.setVisible(true);
                }
            });

            eyeclose2.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    confirmpasstxt.setEchoChar('•');

                    eyeopen2.setVisible(true);
                    eyeclose2.setVisible(false);
                }
            });

        JButton changebtn = new JButton("Change Password");
        changebtn.setBounds(30,269,144,36);
        changebtn.setFont(new Font("SansSerif",Font.BOLD,12));
        changebtn.setForeground(Color.WHITE);
        changebtn.setBackground(new Color(0,190,100));
        changebtn.setFocusPainted(false);
        changebtn.setBorderPainted(false);
        leftpanel.add(changebtn);

        ActionListener btnaction1 = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String newpassword = new String(newpasstxt.getPassword()).trim();
                String confirmpassword = new String(confirmpasstxt.getPassword()).trim();

                if (newpassword.isEmpty() && confirmpassword.isEmpty()) {

                    qpal.components.AppDialogs.showMessageDialog(rpage, "New Password and Confirm Password are required.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    return;

                } else if (newpassword.isEmpty()) {

                    qpal.components.AppDialogs.showMessageDialog(rpage, "New Password is required.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    return;

                } else if (confirmpassword.isEmpty()) {

                    qpal.components.AppDialogs.showMessageDialog(rpage, "Confirm Password is required.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    return;

                } else if (!newpassword.equals(confirmpassword)) {

                    qpal.components.AppDialogs.showMessageDialog(rpage, "Passwords do not match.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    confirmpasstxt.setText("");
                    return;
                }

                onPasswordChanged.accept(newpassword);
                rpage.dispose();
            }
        };

        changebtn.addActionListener(btnaction1);

        newpasstxt.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    changebtn.doClick();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        confirmpasstxt.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    changebtn.doClick();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

JButton cancelbtn = new JButton("Cancel");
        cancelbtn.setBounds(186,269,144,36);
        cancelbtn.setBackground(new Color(240,0,55));
        cancelbtn.setForeground(Color.WHITE);
        cancelbtn.setFont(new Font("SansSerif",Font.BOLD,12));
        cancelbtn.setFocusPainted(false);
        cancelbtn.setBorderPainted(false);
        cancelbtn.addActionListener(e -> {
            rpage.dispose();
        });
        leftpanel.add(cancelbtn);

        rpage.getRootPane().setDefaultButton(changebtn);
        rpage.setVisible(true);
    }
}