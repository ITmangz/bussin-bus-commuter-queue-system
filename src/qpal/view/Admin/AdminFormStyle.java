package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import qpal.components.FormInputStyle;

/** Single dashboard-style outline around forms. */
public final class AdminFormStyle {
    private AdminFormStyle() {}
    static void limitCharacters(JTextField field,int limit,String name) {
        field.setDocument(new javax.swing.text.PlainDocument() {
            @Override public void insertString(int offset,String text,javax.swing.text.AttributeSet attributes)
                    throws javax.swing.text.BadLocationException {
                if (text==null) return;
                if (getLength()+text.length()<=limit) super.insertString(offset,text,attributes);
                else {
                    field.setText("");
                    Toolkit.getDefaultToolkit().beep();
                    qpal.components.AppDialogs.showMessageDialog(field,
                            name+" must not exceed "+limit+" characters.","Warning!",JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }
    static boolean validateSearch(JTextField field) {
        if (!field.getText().trim().isEmpty()) return true;
        qpal.components.AppDialogs.showMessageDialog(field, "Please enter a search term.",
                "Empty Search", JOptionPane.WARNING_MESSAGE);
        field.requestFocusInWindow();
        return false;
    }

    static void searchOnEnter(JTextField field, JButton button) {
        java.awt.event.KeyListener listener = new java.awt.event.KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    button.doClick();
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
            }
        };
        field.addKeyListener(listener);
        button.addKeyListener(listener);
    }

    static void tableFilter(JComboBox<?> combo) {
        combo.putClientProperty("adminTableFilter", Boolean.TRUE);
        Dimension size = new Dimension(140, 34);
        combo.setPreferredSize(size);
        combo.setMinimumSize(size);
        combo.setMaximumSize(size);
    }

    static JPanel frame(JPanel form) {
        styleInputs(form);
        styleActions(form);
        form.setOpaque(false);
        if (form.getLayout() instanceof BoxLayout) form.setPreferredSize(null);
        AdminCard card = new AdminCard(2);
        card.setLayout(new BorderLayout());
        card.add(form);
        card.addHierarchyListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(card);
            if (window instanceof JDialog dialog && dialog.isUndecorated()
                    && !Boolean.TRUE.equals(card.getClientProperty("shaped"))
                    && window.getGraphicsConfiguration().getDevice().isWindowTranslucencySupported(
                            GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
                card.putClientProperty("shaped", true);
                window.addComponentListener(new java.awt.event.ComponentAdapter() {
                    @Override public void componentResized(java.awt.event.ComponentEvent event) {
                        window.setShape(new java.awt.geom.RoundRectangle2D.Double(
                                0, 0, window.getWidth(), window.getHeight(), 14, 14));
                    }
                });
            }
        });
        return card;
    }
    public static void styleInputs(Component component) {
        if (component instanceof JDateChooser chooser) {
            sizeInput(chooser);
            FormInputStyle.styleCalendar(chooser);
            return;
        }
        if (component instanceof JComboBox<?> combo) {
            // Table filters keep their compact size when dashboard forms are styled.
            if (!Boolean.TRUE.equals(combo.getClientProperty("adminTableFilter"))) sizeInput(combo);
            FormInputStyle.styleCombo(combo);
            return;
        }
        if (component instanceof javax.swing.text.JTextComponent field
                && field.getMaximumSize().width == 300) sizeInput(field);
        if (component instanceof Container container)
            for (Component child : container.getComponents()) styleInputs(child);
    }
    private static void sizeInput(JComponent input) {
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        boolean flexible = input.getMaximumSize().width == Integer.MAX_VALUE;
        int width = flexible ? input.getPreferredSize().width : 300;
        input.setPreferredSize(new Dimension(width, 38));
        input.setMinimumSize(new Dimension(width, 38));
        input.setMaximumSize(new Dimension(flexible ? Integer.MAX_VALUE : width, 38));
    }

    private static void styleActions(Container container) {
        // Fixed button sizes also hold in the wider queue, revenue and profile forms.
        if (container instanceof JPanel row && row.getLayout() instanceof GridLayout
                && !Boolean.TRUE.equals(row.getClientProperty("fullWidthActions"))
                && row.getComponentCount() == 2
                && row.getComponent(0) instanceof JButton && row.getComponent(1) instanceof JButton) {
            row.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            Component second = row.getComponent(1);
            row.remove(second);
            row.add(Box.createHorizontalStrut(12));
            row.add(second);
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            Dimension size = new Dimension(300, 38);
            row.setMinimumSize(size);
            row.setPreferredSize(size);
            row.setMaximumSize(size);
            for (Component child : row.getComponents()) {
                if (child instanceof JButton button) {
                    button.setPreferredSize(new Dimension(144, 38));
                    button.setMinimumSize(new Dimension(144, 38));
                    button.setMaximumSize(new Dimension(144, 38));
                }
            }
        }
        for (Component child : container.getComponents()) {
            if (child instanceof JButton button) {
                button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
                button.setOpaque(true);
                button.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                button.setBorderPainted(false);
                button.setFocusPainted(false);
            } else if (child instanceof Container nested
                    && !(child instanceof JComboBox<?>) && !(child instanceof JDateChooser)) {
                styleActions(nested);
            }
        }
    }
}
