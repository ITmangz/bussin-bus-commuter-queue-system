package qpal.components;

import java.awt.*;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.swing.*;
import javax.swing.text.*;

/** Shared page picker; reads the current filtered page count when opened. */
public final class PagePicker {
    private PagePicker() {}

    public static JButton create(IntSupplier currentPage, IntSupplier pageCount, IntConsumer selectPage) {
        JButton button = new JButton("…");
        button.setPreferredSize(new Dimension(34, 30));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(80, 80, 80));
        button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Go to page");
        button.getAccessibleContext().setAccessibleName("Go to page");
        button.addActionListener(event -> {
            int pages = Math.max(1, pageCount.getAsInt());
            JPopupMenu popup = new JPopupMenu();
            popup.setBorder(BorderFactory.createLineBorder(new Color(224, 229, 236)));
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));
            content.setBackground(Color.WHITE);
            JLabel title = new JLabel("Go to page");
            title.setFont(new Font("Segoe UI", Font.BOLD, 15));
            title.setForeground(new Color(35, 43, 55));
            content.add(title, BorderLayout.NORTH);
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            controls.setOpaque(false);
            JTextField page = new JTextField();
            page.setDocument(new javax.swing.text.PlainDocument() {
                @Override
                public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {
                    if (str == null) {
                        return;
                    }
                    if (getLength() + str.length() <= 5) {
                        super.insertString(offs, str, a);
                    } else {
                        page.setText("");
                        Toolkit.getDefaultToolkit().beep();
                        qpal.components.AppDialogs.showMessageDialog(null,
                                "Page number must not exceed 5 characters.", "Warning!", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            page.setText(String.valueOf(Math.max(1, Math.min(currentPage.getAsInt(), pages))));
            page.setPreferredSize(new Dimension(76, 38));
            page.setHorizontalAlignment(JTextField.CENTER);
            page.setFont(new Font("Segoe UI", Font.BOLD, 15));
            page.setForeground(new Color(35, 43, 55));
            page.setBackground(new Color(248, 249, 251));
            page.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(215, 222, 231), 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            title.setLabelFor(page);
            controls.add(page);
            JLabel total = new JLabel("of " + pages);
            total.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            total.setForeground(new Color(105, 115, 135));
            controls.add(total);
            JButton go = new JButton("Go") {
                @Override protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(getModel().isPressed() ? getBackground().darker() : getBackground());
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g.dispose();
                    super.paintComponent(graphics);
                }
            };
            go.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            go.setContentAreaFilled(false);
            go.setBorderPainted(false);
            go.setPreferredSize(new Dimension(68, 38));
            go.setFont(new Font("Segoe UI", Font.BOLD, 14));
            go.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            go.setBackground(new Color(225, 29, 72));
            go.setForeground(Color.WHITE);
            go.addActionListener(e -> {
                int selected;
                try {
                    selected = Integer.parseInt(page.getText().trim());
                    if (selected < 1 || selected > pages) throw new NumberFormatException();
                } catch (NumberFormatException invalid) {
                    AppDialogs.showMessageDialog(button, "Please enter a page from 1 to " + pages + ".",
                            "Go to Page", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                popup.setVisible(false);
                selectPage.accept(Math.max(1, Math.min(selected, Math.max(1, pageCount.getAsInt()))));
            });
            page.addActionListener(e -> go.doClick());
            controls.add(go);
            content.add(controls, BorderLayout.CENTER);
            popup.add(content);
            popup.show(button, button.getWidth() - popup.getPreferredSize().width, -popup.getPreferredSize().height);
            page.requestFocusInWindow();
            page.selectAll();
        });
        return button;
    }
}
