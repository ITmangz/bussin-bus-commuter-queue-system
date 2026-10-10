package qpal.view.Login;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

final class LoginFieldStyle {
    private LoginFieldStyle() {}

    static JTextField textField(String placeholder) {
        return new JTextField() {
            @Override
            protected void paintComponent(Graphics graphics) {
                paintFieldBackground(this, graphics);
                super.paintComponent(graphics);
                paintPlaceholder(this, graphics, placeholder);
            }
        };
    }

    static JPasswordField passwordField(String placeholder) {
        return new JPasswordField() {
            @Override
            protected void paintComponent(Graphics graphics) {
                paintFieldBackground(this, graphics);
                super.paintComponent(graphics);
                paintPlaceholder(this, graphics, placeholder);
            }
        };
    }
    static void styleLoginField(JTextField field) {
        field.setOpaque(false);
        field.setBackground(new Color(248, 249, 251));
        field.setCaretColor(new Color(220, 0, 50));
        field.setSelectionColor(new Color(255, 220, 230));
        field.setSelectedTextColor(new Color(50, 50, 50));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.repaint();
            }
        });
    }

    static void paintFieldBackground(JTextField field, Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(field.getBackground());
        g.fillRect(0, 0, field.getWidth() - 1, field.getHeight() - 1);
        g.setColor(field.hasFocus() ? new Color(220, 0, 50) : new Color(220, 224, 230));
        g.drawRect(0, 0, field.getWidth() - 1, field.getHeight() - 1);
        g.dispose();
    }

    static void paintPlaceholder(JTextField field, Graphics graphics, String placeholder) {
        // Paint the hint only: it is never stored as input or submitted during login.
        if (field.getDocument().getLength() != 0) {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(field.getFont());
        g.setColor(new Color(140, 145, 155));
        FontMetrics metrics = g.getFontMetrics();
        int y = (field.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        int x = field.getHorizontalAlignment() == JTextField.CENTER
                ? (field.getWidth() - metrics.stringWidth(placeholder)) / 2 : field.getInsets().left;
        g.drawString(placeholder, x, y);
        g.dispose();
    }
}
