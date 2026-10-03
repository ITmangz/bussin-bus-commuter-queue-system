package qpal.view.Login;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import qpal.dao.AccountDao;
import qpal.model.Account;
import qpal.view.Admin.AdminDashboard;
import qpal.view.Employee.EmployeeDashboard;

public class LoginPage{

    public LoginPage() {

        AccountDao accountDao = new AccountDao();
        
        JFrame lpage = new JFrame();
        lpage.setSize(850,550);
        lpage.setResizable(false);
        lpage.setLocationRelativeTo(null);
        lpage.setLayout(null);
        lpage.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel leftpanel = new JPanel(null);
        leftpanel.setBounds(0, 0, 425, 550);
        leftpanel.setBackground(Color.RED);
        lpage.add(leftpanel);

        JLabel leftbg = new JLabel();
        leftbg.setBounds(0, 0, 425, 550);

        String[] leftimages = {
            "resources/icons/LCommuteSmarter.png",
            "resources/icons/LJoinQueue.png",
            "resources/icons/LTrackQueue.png",
            "resources/icons/LKnowGo.png",
            "resources/icons/LBussinExp.png"
        };

        ImageIcon[] backgrounds = new ImageIcon[leftimages.length];

        for (int i = 0; i < leftimages.length; i++) {
            ImageIcon leftpic = new ImageIcon(leftimages[i]);
            Image img = leftpic.getImage().getScaledInstance(425, 550, Image.SCALE_SMOOTH);
            backgrounds[i] = new ImageIcon(img);
        }

        leftbg.setIcon(backgrounds[0]);
        leftpanel.add(leftbg);

        int[] currentImage = {0};

        Timer imageTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentImage[0]++;

                if (currentImage[0] >= backgrounds.length) {
                    currentImage[0] = 0;
                }

                leftbg.setIcon(backgrounds[currentImage[0]]);
            }
        });

        imageTimer.start();

        JPanel rightpanel = new JPanel(null);
        rightpanel.setBounds(425, 0, 425, 550);
        rightpanel.setBackground(Color.WHITE);
        lpage.add(rightpanel);

        JLabel label1 = new JLabel("Welcome Back!");
        label1.setBounds(10, 75, 375, 45);
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        label1.setFont(new Font("Segoe UI", Font.BOLD, 30));
        label1.setForeground(Color.BLACK);
        rightpanel.add(label1);

        JLabel subtext1 = new JLabel("We're so excited to see you again!");
        subtext1.setBounds(10, 120, 375, 30);
        subtext1.setHorizontalAlignment(SwingConstants.CENTER);
        subtext1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtext1.setForeground(new Color(100, 100, 100));
        rightpanel.add(subtext1);

        JLabel emailtitle = new JLabel("Email");
        emailtitle.setBounds(25, 170, 345, 25);
        emailtitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        emailtitle.setForeground(Color.BLACK);
        rightpanel.add(emailtitle);

        JTextField emailtxt = new JTextField();
        emailtxt.setBounds(25, 200, 345, 38);
        emailtxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailtxt.setForeground(new Color(80, 80, 80));
        emailtxt.setBackground(new Color(235, 235, 235));
        emailtxt.setBorder(BorderFactory.createEmptyBorder(5, 42, 5, 20));
        rightpanel.add(emailtxt);

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

        JLabel adicon = new JLabel();
        adicon.setBounds(10, 0, 40, 40);
        ImageIcon admicon = new ImageIcon("resources/icons/adminicon.png");
        Image adic = admicon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        adicon.setIcon(new ImageIcon(adic));
        adicon.setVisible(true);
        emailtxt.add(adicon);

        JLabel passtitle = new JLabel("Password");
        passtitle.setBounds(25, 250, 345, 25);
        passtitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        passtitle.setForeground(Color.BLACK);
        rightpanel.add(passtitle);

        JPasswordField passtxt = new JPasswordField();
        passtxt.setBounds(25, 280, 345, 40);
        passtxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passtxt.setForeground(new Color(80, 80, 80));
        passtxt.setBackground(new Color(235, 235, 235));
        passtxt.setBorder(BorderFactory.createEmptyBorder(5, 42, 5, 45));
        passtxt.setEchoChar('•');
        rightpanel.add(passtxt);

            passtxt.setDocument(new javax.swing.text.PlainDocument() {
                @Override
                public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {

                    if (str == null) {
                        return;
                    }

                    if (getLength() + str.length() <= 100) {
                        super.insertString(offs, str, a);
                    } else {
                        passtxt.setText("");
                        Toolkit.getDefaultToolkit().beep();
                        qpal.components.AppDialogs.showMessageDialog(null, "Password must not exceed 100 characters.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });

        JLabel passicon = new JLabel();
        passicon.setBounds(10, 0, 40, 40);
        ImageIcon lockicon = new ImageIcon("resources/icons/lockicon.png");
        Image lcic = lockicon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        passicon.setIcon(new ImageIcon(lcic));
        passicon.setVisible(true);
        passtxt.add(passicon);

        JLabel eyeopen = new JLabel();
        eyeopen.setBounds(305, 0, 40, 40);
        ImageIcon eyesocic = new ImageIcon("resources/icons/opeyeicon.png");
        Image eyop = eyesocic.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeopen.setIcon(new ImageIcon(eyop));
        eyeopen.setVisible(true);
        passtxt.add(eyeopen);

        JLabel eyeclose = new JLabel();
        eyeclose.setBounds(305, 0, 40, 40);
        ImageIcon eyescic = new ImageIcon("resources/icons/cleyeicon.png");
        Image eyclo = eyescic.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        eyeclose.setIcon(new ImageIcon(eyclo));
        eyeclose.setVisible(false);
        passtxt.add(eyeclose);

            eyeopen.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    passtxt.setEchoChar((char) 0);

                    eyeopen.setVisible(false);
                    eyeclose.setVisible(true);
                }
            });

            eyeclose.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    passtxt.setEchoChar('•');

                    eyeopen.setVisible(true);
                    eyeclose.setVisible(false);
                }
            });

        JLabel forgot = new JLabel("Forgot Password?");
        forgot.setBounds(25, 325, 200, 25);
        forgot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        forgot.setForeground(new Color(220, 0, 50));
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightpanel.add(forgot);

        forgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgot.setForeground(new Color(180, 0, 40));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgot.setForeground(new Color(220, 0, 50));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                lpage.dispose();
                new ForgotPassword();
            }
        });

        JButton loginbtn = new JButton("Login");
        loginbtn.setBounds(25, 360, 345, 40);
        loginbtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginbtn.setForeground(Color.WHITE);
        loginbtn.setBackground(new Color(220, 0, 50));
        loginbtn.setFocusPainted(false);
        loginbtn.setBorderPainted(false);
        rightpanel.add(loginbtn);

        ActionListener btnaction1 = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String email = emailtxt.getText().trim();
                String password = new String(passtxt.getPassword()).trim();

                if (email.isEmpty() && password.isEmpty()) {

                    qpal.components.AppDialogs.showMessageDialog(null, "Email and Password are required.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    emailtxt.setText("");
                    passtxt.setText("");
                    return;

                } else if (email.isEmpty()) {

                    qpal.components.AppDialogs.showMessageDialog(null,"Email is required.","Warning!",JOptionPane.WARNING_MESSAGE);
                    emailtxt.setText("");
                    passtxt.setText("");
                    return;

                } else if (password.isEmpty()) {

                    qpal.components.AppDialogs.showMessageDialog(null,"Password is required.","Warning!", JOptionPane.WARNING_MESSAGE);
                    emailtxt.setText("");
                    passtxt.setText("");
                    return;
                }
          
                Account account = accountDao.Login(email, password);

                if (account != null) {

                    qpal.components.AppDialogs.showMessageDialog(null,"Login successful!","Success!", JOptionPane.INFORMATION_MESSAGE);

                    if (account.getRole().equalsIgnoreCase("admin")) {
                        qpal.dao.ActivityLogDao.setCurrentAccount(account);
                        qpal.dao.ActivityLogDao.recordActivity("Authentication", "Login", "User logged in to the system.");

                        lpage.dispose();
                        new AdminDashboard(account);

                    } else if (account.getRole().equalsIgnoreCase("employee")) {
                        qpal.dao.ActivityLogDao.setCurrentAccount(account);
                        qpal.dao.ActivityLogDao.recordActivity("Authentication", "Login", "User logged in to the system.");
                        
                        lpage.dispose();
                        new EmployeeDashboard(account);

                    } else {

                        qpal.components.AppDialogs.showMessageDialog(null,"Unknown account role.","Error", JOptionPane.ERROR_MESSAGE);
                        emailtxt.setText("");
                        passtxt.setText("");
                        return;
                    }

                    lpage.dispose();

                } else if (accountDao.CheckInactive(email)) {

                    qpal.components.AppDialogs.showMessageDialog(null, "This account has been inactive. Please contact the admin.","Account Inactive",JOptionPane.WARNING_MESSAGE);
                    emailtxt.setText("");
                    passtxt.setText("");

                } else if (accountDao.CheckEmail(email)) {

                    qpal.components.AppDialogs.showMessageDialog(null,"Invalid Credentials.","Login Failed!",JOptionPane.ERROR_MESSAGE);
                    emailtxt.setText("");
                    passtxt.setText("");

                } else {

                    qpal.components.AppDialogs.showMessageDialog(null,"Invalid Credentials.","Login Failed!", JOptionPane.ERROR_MESSAGE);
                    emailtxt.setText("");
                    passtxt.setText("");

                }
            }
        };

        loginbtn.addActionListener(btnaction1);

        emailtxt.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginbtn.doClick();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        passtxt.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginbtn.doClick();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        JLabel subtext2 = new JLabel("Terms of use | Privacy Policy");
        subtext2.setBounds(10, 455, 375, 25);
        subtext2.setHorizontalAlignment(SwingConstants.CENTER);
        subtext2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtext2.setForeground(new Color(100, 100, 100));
        rightpanel.add(subtext2);

        lpage.setVisible(true);

    }
}
