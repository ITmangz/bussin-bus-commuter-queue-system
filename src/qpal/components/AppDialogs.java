package qpal.components;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Locale;
import javax.swing.*;


/** Shared modal alerts. Return values remain compatible with JOptionPane. */
public final class AppDialogs {
    private AppDialogs() {}

    public static void showMessageDialog(Component parent, Object message) {
        String text = String.valueOf(message).toLowerCase(Locale.ROOT);
        boolean success = text.contains("success") || text.startsWith("booking added");
        int type = text.contains("failed") || text.contains("unable") || text.contains("error")
                ? JOptionPane.ERROR_MESSAGE : text.startsWith("please") || text.startsWith("select")
                || text.startsWith("enter") ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        showMessageDialog(parent, message, success ? "Success" : type == JOptionPane.ERROR_MESSAGE
                ? "Error" : type == JOptionPane.WARNING_MESSAGE ? "Check Details" : "Information", type);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int type) {
        show(parent, message, title, type, JOptionPane.DEFAULT_OPTION);
    }

    public static void showDetailsDialog(Component parent, String message, String title) {
        show(parent, message, title, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, true);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int options) {
        return showConfirmDialog(parent, message, title, options, JOptionPane.QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int options, int type) {
        return show(parent, message, title, type, options);
    }

    private static int show(Component parent, Object message, String title, int type, int options) {
        return show(parent, message, title, type, options, false);
    }

    private static int show(Component parent, Object message, String title, int type, int options, boolean details) {
        if (!SwingUtilities.isEventDispatchThread()) {
            int[] result = {JOptionPane.CLOSED_OPTION};
            try { SwingUtilities.invokeAndWait(() -> result[0] = show(parent, message, title, type, options, details)); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            catch (java.lang.reflect.InvocationTargetException e) { throw new IllegalStateException(e.getCause()); }
            return result[0];
        }
        Window owner = parent instanceof Window window ? window : parent == null
                ? KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow()
                : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        int[] result = {JOptionPane.CLOSED_OPTION};
        JPanel card = buildCard(message, title, type, options, value -> {
            result[0] = value;
            dialog.dispose();
        }, details);
        dialog.setContentPane(card);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setResizable(false);
        if (dialog.getGraphicsConfiguration().getDevice().isWindowTranslucencySupported(
                GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 16, 16));
        }
        dialog.setLocationRelativeTo(owner);
        RootPaneContainer root = owner instanceof RootPaneContainer container ? container : null;
        Component previous = root == null ? null : root.getGlassPane();
        boolean visible = previous != null && previous.isVisible();

        if (root != null) {
            JPanel shade = new JPanel() {
                @Override protected void paintComponent(Graphics graphics) {
                    graphics.setColor(new Color(20, 28, 45, 110));
                    graphics.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            shade.setOpaque(false);
            root.setGlassPane(shade);
            shade.setVisible(true);
        }

        try { dialog.setVisible(true); }
        
        finally {
            dialog.dispose();
            if (root != null) { root.setGlassPane(previous); previous.setVisible(visible); }
        }
        return result[0];
    }

    static JPanel buildCard(Object message, String title, int type, int options,
            java.util.function.IntConsumer choose) {
        return buildCard(message, title, type, options, choose, false);
    }

    static JPanel buildCard(Object message, String title, int type, int options,
            java.util.function.IntConsumer choose, boolean details) {
        boolean success = type == JOptionPane.INFORMATION_MESSAGE
                && title.toLowerCase(Locale.ROOT).contains("success");
        Color accent = success ? new Color(0, 160, 95) : type == JOptionPane.WARNING_MESSAGE
                ? new Color(184, 113, 10) : new Color(225, 29, 48);
        String symbol = success ? "\u2713" : type == JOptionPane.ERROR_MESSAGE ? "\u00d7"
                : type == JOptionPane.WARNING_MESSAGE ? "!" : type == JOptionPane.QUESTION_MESSAGE ? "?" : "i";
        JPanel card = new JPanel(new BorderLayout(0, 22));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(20, 16, 16, 16));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel badge = new JLabel(symbol, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g.fillOval(0, 0, 48, 48);
                if (success) {
                    g.setColor(accent);
                    g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(15, 24, 21, 30); g.drawLine(21, 30, 33, 18);
                }
                g.dispose();
                if (!success) super.paintComponent(graphics);
            }
        };
        badge.setForeground(accent);
        badge.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        badge.setPreferredSize(new Dimension(48, 48)); badge.setMaximumSize(new Dimension(48, 48));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(badge); content.add(Box.createVerticalStrut(18));
        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(new Color(20, 28, 45)); heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(heading); content.add(Box.createVerticalStrut(10));
        JTextPane body = new JTextPane() { @Override public boolean getScrollableTracksViewportWidth() { return true; } };
        body.setEditable(false); body.setFocusable(false); body.setOpaque(false);


        body.setMargin(new Insets(3, 8, 3, 8));
        body.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        body.setForeground(new Color(105, 115, 135));
        body.setText(message == null ? "" : String.valueOf(message));
        javax.swing.text.SimpleAttributeSet alignment = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(alignment, details
                ? javax.swing.text.StyleConstants.ALIGN_LEFT : javax.swing.text.StyleConstants.ALIGN_CENTER);
        body.getStyledDocument().setParagraphAttributes(0, body.getDocument().getLength(), alignment, false);
        // Alerts expand to fit wrapped text; only detailed record views scroll.
        int textWidth = details ? 448 - 12 : 448;
        body.setSize(textWidth, Short.MAX_VALUE);
        int height = details ? Math.min(220, body.getPreferredSize().height + 6)
                : body.getPreferredSize().height + 6;
        JScrollPane scroll = new JScrollPane(body, details ? JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                : JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.setOpaque(false);
        scroll.getViewport().setBackground(Color.WHITE);
        ScrollBarStyle.apply(scroll, Color.WHITE);
        scroll.setPreferredSize(new Dimension(448, height));
        scroll.setMinimumSize(new Dimension(448, height));
        scroll.setMaximumSize(new Dimension(448, height));
        content.add(scroll); card.add(content, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, 0, 12, 0)); actions.setOpaque(false);
        if (options == JOptionPane.DEFAULT_OPTION) addButton(actions, "OK", JOptionPane.OK_OPTION, accent, choose);
        else {
            addButton(actions, options == JOptionPane.OK_CANCEL_OPTION ? "OK" : "Yes",
                    JOptionPane.YES_OPTION, accent, choose);
            if (options != JOptionPane.OK_CANCEL_OPTION)
                addButton(actions, "No", JOptionPane.NO_OPTION, new Color(100, 110, 128), choose);
            if (options == JOptionPane.YES_NO_CANCEL_OPTION || options == JOptionPane.OK_CANCEL_OPTION)
                addButton(actions, "Cancel", JOptionPane.CANCEL_OPTION, new Color(100, 110, 128), choose);
        }
        actions.setPreferredSize(new Dimension(448, 38)); card.add(actions, BorderLayout.SOUTH);
        card.addHierarchyListener(e -> {
            JRootPane root = SwingUtilities.getRootPane(card);
            if (root != null) root.setDefaultButton((JButton)actions.getComponent(0));
        });
        return card;
    }

    private static void addButton(JPanel actions, String label, int value, Color color,
            java.util.function.IntConsumer choose) {
        JButton button = new JButton(label) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getModel().isPressed() ? color.darker() : color);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.dispose(); super.paintComponent(graphics);
            }
        };
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setContentAreaFilled(false);
        button.setBackground(color); button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> choose.accept(value)); actions.add(button);
    }
}


