package qpal.view.Login;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CodeVerification {

    public CodeVerification(String email) {

        JFrame cpage = new JFrame();
        cpage.setSize(850, 550);
        cpage.setResizable(false);
        cpage.setLocationRelativeTo(null);
        cpage.setLayout(null);
        cpage.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel rightpanel = new JPanel(null);
        rightpanel.setBounds(425, 0, 425, 550);
        rightpanel.setBackground(Color.RED);
        cpage.add(rightpanel);

        JLabel rightbg = new JLabel();
        rightbg.setBounds(0, 0, 425, 550);
        ImageIcon rightpic = new ImageIcon("resources/icons/rightbg.png");
        Image img = rightpic.getImage().getScaledInstance(425, 550, Image.SCALE_SMOOTH);
        rightbg.setIcon(new ImageIcon(img));
        rightpanel.add(rightbg);

        JPanel leftpanel = new JPanel(null);
        leftpanel.setBounds(0, 0, 425, 550);
        leftpanel.setBackground(Color.WHITE);
        cpage.add(leftpanel);

        JLabel label1 = new JLabel("Verify your Email");
        label1.setBounds(30, 75, 375, 45);
        label1.setHorizontalAlignment(SwingConstants.CENTER);
        label1.setFont(new Font("Segoe UI", Font.BOLD, 30));
        label1.setForeground(Color.BLACK);
        leftpanel.add(label1);

        JLabel subtext1 = new JLabel("Enter the 6-digit verification code sent to your");
        subtext1.setBounds(30, 125, 375, 25);
        subtext1.setHorizontalAlignment(SwingConstants.CENTER);
        subtext1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtext1.setForeground(new Color(100, 100, 100));
        leftpanel.add(subtext1);

        JLabel subtext2 = new JLabel("registered email address.");
        subtext2.setBounds(30, 145, 375, 25);
        subtext2.setHorizontalAlignment(SwingConstants.CENTER);
        subtext2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtext2.setForeground(new Color(100, 100, 100));
        leftpanel.add(subtext2);

        JLabel codelbl = new JLabel("6-digit Confirmation Code");
        codelbl.setBounds(50, 195, 345, 25);
        codelbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        codelbl.setForeground(Color.BLACK);
        leftpanel.add(codelbl);

        JTextField code1txt = new JTextField();
        code1txt.setBounds(50, 230, 50, 65);
        code1txt.setHorizontalAlignment(SwingConstants.CENTER);
        code1txt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        code1txt.setBackground(new Color(235, 235, 235));
        code1txt.setBorder(BorderFactory.createEmptyBorder());
        leftpanel.add(code1txt);

        JTextField code2txt = new JTextField();
        code2txt.setBounds(109, 230, 50, 65);
        code2txt.setHorizontalAlignment(SwingConstants.CENTER);
        code2txt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        code2txt.setBackground(new Color(235, 235, 235));
        code2txt.setBorder(BorderFactory.createEmptyBorder());
        leftpanel.add(code2txt);

        JTextField code3txt = new JTextField();
        code3txt.setBounds(168, 230, 50, 65);
        code3txt.setHorizontalAlignment(SwingConstants.CENTER);
        code3txt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        code3txt.setBackground(new Color(235, 235, 235));
        code3txt.setBorder(BorderFactory.createEmptyBorder());
        leftpanel.add(code3txt);

        JTextField code4txt = new JTextField();
        code4txt.setBounds(227, 230, 50, 65);
        code4txt.setHorizontalAlignment(SwingConstants.CENTER);
        code4txt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        code4txt.setBackground(new Color(235, 235, 235));
        code4txt.setBorder(BorderFactory.createEmptyBorder());
        leftpanel.add(code4txt);

        JTextField code5txt = new JTextField();
        code5txt.setBounds(286, 230, 50, 65);
        code5txt.setHorizontalAlignment(SwingConstants.CENTER);
        code5txt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        code5txt.setBackground(new Color(235, 235, 235));
        code5txt.setBorder(BorderFactory.createEmptyBorder());
        leftpanel.add(code5txt);

        JTextField code6txt = new JTextField();
        code6txt.setBounds(345, 230, 50, 65);
        code6txt.setHorizontalAlignment(SwingConstants.CENTER);
        code6txt.setFont(new Font("Segoe UI", Font.BOLD, 24));
        code6txt.setBackground(new Color(235, 235, 235));
        code6txt.setBorder(BorderFactory.createEmptyBorder());
        leftpanel.add(code6txt);

        code1txt.addKeyListener(
                new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        if (!Character.isDigit(e.getKeyChar())
                                || code1txt.getText().length() >= 1) {
                            e.consume();
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                        if (Character.isDigit(e.getKeyChar()) && code1txt.getText().length() == 1) {
                            code2txt.requestFocus();
                        }
                    }
                });

        code2txt.addKeyListener(
                new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        if (!Character.isDigit(e.getKeyChar())
                                || code2txt.getText().length() >= 1) {
                            e.consume();
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                        if (Character.isDigit(e.getKeyChar()) && code2txt.getText().length() == 1) {
                            code3txt.requestFocus();
                        }
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                                && code2txt.getText().isEmpty()) {
                            code1txt.requestFocus();
                            code1txt.setText("");
                        }
                    }
                });

        code3txt.addKeyListener(
                new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        if (!Character.isDigit(e.getKeyChar())
                                || code3txt.getText().length() >= 1) {
                            e.consume();
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                        if (Character.isDigit(e.getKeyChar()) && code3txt.getText().length() == 1) {
                            code4txt.requestFocus();
                        }
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                                && code3txt.getText().isEmpty()) {
                            code2txt.requestFocus();
                            code2txt.setText("");
                        }
                    }
                });

        code4txt.addKeyListener(
                new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        if (!Character.isDigit(e.getKeyChar())
                                || code4txt.getText().length() >= 1) {
                            e.consume();
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                        if (Character.isDigit(e.getKeyChar()) && code4txt.getText().length() == 1) {
                            code5txt.requestFocus();
                        }
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                                && code4txt.getText().isEmpty()) {
                            code3txt.requestFocus();
                            code3txt.setText("");
                        }
                    }
                });

        code5txt.addKeyListener(
                new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        if (!Character.isDigit(e.getKeyChar())
                                || code5txt.getText().length() >= 1) {
                            e.consume();
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                        if (Character.isDigit(e.getKeyChar()) && code5txt.getText().length() == 1) {
                            code6txt.requestFocus();
                        }
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                                && code5txt.getText().isEmpty()) {
                            code4txt.requestFocus();
                            code4txt.setText("");
                        }
                    }
                });

        code6txt.addKeyListener(
                new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        if (!Character.isDigit(e.getKeyChar())
                                || code6txt.getText().length() >= 1) {
                            e.consume();
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                                && code6txt.getText().isEmpty()) {
                            code5txt.requestFocus();
                            code5txt.setText("");
                        }
                    }
                });

        JButton confirmbtn = new JButton("Confirm Code");
        confirmbtn.setBounds(50, 320, 345, 40);
        confirmbtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmbtn.setForeground(Color.WHITE);
        confirmbtn.setBackground(new Color(220, 0, 50));
        confirmbtn.setFocusPainted(false);
        confirmbtn.setBorderPainted(false);
        leftpanel.add(confirmbtn);

        JLabel resendlbl = new JLabel("Didn't receive a code?");
        resendlbl.setBounds(100, 380, 160, 25);
        resendlbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resendlbl.setForeground(new Color(100, 100, 100));
        leftpanel.add(resendlbl);

        JLabel resend = new JLabel("Resend code");
        resend.setBounds(245, 380, 100, 25);
        resend.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resend.setForeground(new Color(220, 0, 50));
        resend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftpanel.add(resend);

        JTextField[] codeFields = {code1txt, code2txt, code3txt, code4txt, code5txt, code6txt};
        for (JTextField field : codeFields) {
            ((javax.swing.text.AbstractDocument) field.getDocument())
                    .setDocumentFilter(
                            new javax.swing.text.DocumentFilter() {
                                @Override
                                public void insertString(
                                        FilterBypass fb,
                                        int offset,
                                        String text,
                                        javax.swing.text.AttributeSet attrs)
                                        throws javax.swing.text.BadLocationException {
                                    replace(fb, offset, 0, text, attrs);
                                }

                                @Override
                                public void replace(
                                        FilterBypass fb,
                                        int offset,
                                        int length,
                                        String text,
                                        javax.swing.text.AttributeSet attrs)
                                        throws javax.swing.text.BadLocationException {
                                    String value =
                                            fb.getDocument()
                                                    .getText(0, fb.getDocument().getLength());
                                    String replacement =
                                            value.substring(0, offset)
                                                    + (text == null ? "" : text)
                                                    + value.substring(offset + length);
                                    if (replacement.matches("[0-9]?"))
                                        super.replace(fb, offset, length, text, attrs);
                                }
                            });
            field.addActionListener(e -> confirmbtn.doClick());
        }
        confirmbtn.addActionListener(
                e -> {
                    if (!confirmbtn.isEnabled()) return;
                    StringBuilder entered = new StringBuilder();
                    for (JTextField field : codeFields) entered.append(field.getText().trim());
                    if (!entered.toString().matches("[0-9]{6}")) {
                        qpal.components.AppDialogs.showMessageDialog(
                                cpage,
                                "Enter the six-digit code.",
                                "Verify Code",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    confirmbtn.setEnabled(false);
                    resend.setEnabled(false);
                    qpal.util.UiTask.run(
                            () -> qpal.util.PasswordResetService.verify(email, entered.toString()),
                            token -> {
                                if (!cpage.isDisplayable()) return;
                                cpage.dispose();
                                new ResetPassword(email, token);
                            },
                            ex -> {
                                if (!cpage.isDisplayable()) return;
                                confirmbtn.setEnabled(true);
                                resend.setEnabled(true);
                                qpal.components.AppDialogs.showMessageDialog(
                                        cpage,
                                        qpal.util.PasswordResetService.errorMessage(ex),
                                        "Verify Code",
                                        JOptionPane.ERROR_MESSAGE);
                            });
                });
        resend.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        resend.setForeground(new Color(180, 0, 40));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        resend.setForeground(new Color(220, 0, 50));
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!resend.isEnabled()) return;
                        resend.setEnabled(false);
                        confirmbtn.setEnabled(false);
                        resend.setText("Sending...");
                        qpal.util.UiTask.run(
                                () -> {
                                    qpal.util.PasswordResetService.send(email);
                                    return true;
                                },
                                sent -> {
                                    if (!cpage.isDisplayable()) return;
                                    resend.setEnabled(true);
                                    confirmbtn.setEnabled(true);
                                    resend.setText("Resend code");
                                    for (JTextField field : codeFields) field.setText("");
                                    code1txt.requestFocusInWindow();
                                    qpal.components.AppDialogs.showMessageDialog(
                                            cpage,
                                            "If the account is active, a new code has been sent."
                                                + " Use the latest code within five minutes.",
                                            "Verify Code",
                                            JOptionPane.INFORMATION_MESSAGE);
                                },
                                ex -> {
                                    if (!cpage.isDisplayable()) return;
                                    resend.setEnabled(true);
                                    confirmbtn.setEnabled(true);
                                    resend.setText("Resend code");
                                    qpal.components.AppDialogs.showMessageDialog(
                                            cpage,
                                            qpal.util.PasswordResetService.errorMessage(ex),
                                            "Verify Code",
                                            JOptionPane.ERROR_MESSAGE);
                                });
                    }
                });

        JLabel line1 = new JLabel();
        line1.setBounds(50, 420, 145, 1);
        line1.setOpaque(true);
        line1.setBackground(new Color(180, 180, 180));
        leftpanel.add(line1);

        JLabel orlbl = new JLabel("or");
        orlbl.setBounds(195, 405, 50, 25);
        orlbl.setHorizontalAlignment(SwingConstants.CENTER);
        orlbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orlbl.setForeground(new Color(100, 100, 100));
        leftpanel.add(orlbl);

        JLabel line2 = new JLabel();
        line2.setBounds(245, 420, 140, 1);
        line2.setOpaque(true);
        line2.setBackground(new Color(180, 180, 180));
        leftpanel.add(line2);

        JLabel remember = new JLabel("Remember your password?");
        remember.setBounds(105, 440, 180, 25);
        remember.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        remember.setForeground(new Color(100, 100, 100));
        leftpanel.add(remember);

        JLabel login = new JLabel("Login");
        login.setBounds(285, 440, 50, 25);
        login.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        login.setForeground(new Color(220, 0, 50));
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftpanel.add(login);

        login.addMouseListener(
                new MouseAdapter() {
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
                        cpage.dispose();
                        new LoginPage();
                    }
                });

        cpage.setVisible(true);
    }
}
