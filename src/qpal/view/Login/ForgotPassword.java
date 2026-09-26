package qpal.view.Login;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ForgotPassword {

    public ForgotPassword() { //palitan mo na lang ng "public static void main (String[] args) {"

        JFrame fpage = new JFrame();
        fpage.setSize(850, 550);
        fpage.setResizable(false);
        fpage.setLocationRelativeTo(null);
        fpage.setLayout(null);
        fpage.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel rightpanel = new JPanel(null);
        rightpanel.setBounds(425, 0, 425, 550);
        rightpanel.setBackground(Color.RED);
        fpage.add(rightpanel);

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

        Timer imageTimer = new Timer(5000, new ActionListener() {
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

        JPanel leftpanel = new JPanel(null);
        leftpanel.setBounds(0, 0, 425, 550);
        leftpanel.setBackground(Color.WHITE);
        fpage.add(leftpanel);

        JLabel label1 = new JLabel("Forgot Password");
        label1.setBounds(30, 85, 375, 45);
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        label1.setFont(new Font("Segoe UI", Font.BOLD, 30));
        label1.setForeground(Color.BLACK);
        leftpanel.add(label1);

        JLabel subtext1 = new JLabel("Enter your registered email address to");
        subtext1.setBounds(30, 125, 375, 25);
        subtext1.setHorizontalAlignment(SwingConstants.CENTER);
        subtext1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtext1.setForeground(new Color(100, 100, 100));

        leftpanel.add(subtext1);

        JLabel subtext2 = new JLabel("reset your password.");
        subtext2.setBounds(30, 145, 375, 25);
        subtext2.setHorizontalAlignment(SwingConstants.CENTER);
        subtext2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtext2.setForeground(new Color(100, 100, 100));
        leftpanel.add(subtext2);

        JLabel emaillbl = new JLabel("Email");
        emaillbl.setBounds(45, 185, 345, 25);
        emaillbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        emaillbl.setForeground(Color.BLACK);
        leftpanel.add(emaillbl);

        JTextField emailtxt = new JTextField();
        emailtxt.setBounds(45, 215, 345, 38);
        emailtxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailtxt.setForeground(new Color(80, 80, 80));
        emailtxt.setBackground(new Color(235, 235, 235));
        emailtxt.setBorder(BorderFactory.createEmptyBorder(5, 42, 5, 10));
        leftpanel.add(emailtxt);

         emailtxt.setDocument(new javax.swing.text.PlainDocument() {

                @Override
                public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {

                    if (str == null) {
                        return;
                    }

                    if (getLength() + str.length() <= 100) {
                        super.insertString(offs, str, a);

                    } else {
                        emailtxt.setText("");
                        Toolkit.getDefaultToolkit().beep();
                        qpal.components.AppDialogs.showMessageDialog(null, "Email must not exceed 100 characters.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });

        JLabel emailicon = new JLabel();
        emailicon.setBounds(10, 0, 40, 40);
        ImageIcon emailicn = new ImageIcon("resources/icons/adminicon.png");
        Image emailic = emailicn.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        emailicon.setIcon(new ImageIcon(emailic));
        emailicon.setVisible(true);
        emailtxt.add(emailicon);

        JButton sendcodebtn = new JButton("Send Verification Code");
        sendcodebtn.setBounds(45, 275, 345, 40);
        sendcodebtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendcodebtn.setForeground(Color.WHITE);
        sendcodebtn.setBackground(new Color(220, 0, 50));
        sendcodebtn.setFocusPainted(false);
        sendcodebtn.setBorderPainted(false);
        leftpanel.add(sendcodebtn);

        ActionListener btnaction1 = new ActionListener() {
    
            @Override
            public void actionPerformed(ActionEvent e) {

                String email = emailtxt.getText().trim();

                if(email.isEmpty()) {
                    qpal.components.AppDialogs.showMessageDialog(null, "Email is required.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    return;

                }
                qpal.components.AppDialogs.showMessageDialog(null, "Verification code sent successfully.", "Success!", JOptionPane.INFORMATION_MESSAGE);
                fpage.dispose();
                new CodeVerification();
            }
        };

        sendcodebtn.addActionListener(btnaction1);

        JLabel line1 = new JLabel();
        line1.setBounds(45, 340, 145, 1);
        line1.setOpaque(true);
        line1.setBackground(new Color(180, 180, 180));
        leftpanel.add(line1);

        JLabel orlbl = new JLabel("or");
        orlbl.setBounds(195, 325, 50, 25);
        orlbl.setHorizontalAlignment(SwingConstants.CENTER);
        orlbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orlbl.setForeground(new Color(100, 100, 100));
        leftpanel.add(orlbl);

        JLabel line2 = new JLabel();
        line2.setBounds(250, 340, 140, 1);
        line2.setOpaque(true);
        line2.setBackground(new Color(180, 180, 180));
        leftpanel.add(line2);

        JLabel remember = new JLabel("Remember your password? ");
        remember.setBounds(105, 350, 190, 25);
        remember.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        remember.setForeground(new Color(100, 100, 100));
        leftpanel.add(remember);

        JLabel login = new JLabel("Login");
        login.setBounds(285, 350, 50, 25);
        login.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        login.setForeground(new Color(220, 0, 50));
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftpanel.add(login);

        login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                login.setForeground(new Color(180, 0, 40));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                login.setForeground(new Color(220, 0, 50));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                fpage.dispose();
                new LoginPage();
            }
        });

        fpage.setVisible(true);
    }
}