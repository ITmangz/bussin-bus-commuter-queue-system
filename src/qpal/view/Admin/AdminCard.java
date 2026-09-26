package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Shared subtle outline for admin content cards. */
final class AdminCard extends JPanel {
    AdminCard(int padding) {
        setOpaque(false);
        setBorder(new EmptyBorder(padding, padding, padding, padding));
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
        g.setColor(new Color(232, 236, 242));
        g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
        g.dispose();
    }

    static JScrollPane scrollPage(JPanel page, int minimumHeight) {
        JPanel canvas = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                Container viewport = getParent();
                int width = viewport == null ? 900 : viewport.getWidth();
                int height = viewport == null ? minimumHeight : viewport.getHeight();
                return new Dimension(width, Math.max(minimumHeight, height));
            }
        };
        canvas.setBackground(page.getBackground());
        canvas.add(page, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(canvas,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        styleScrollBar(scroll, page.getBackground());
        scroll.setBackground(page.getBackground());
        scroll.getViewport().setBackground(page.getBackground());
        return scroll;
    }

    static void styleScrollBar(JScrollPane scroll, Color trackColor) {
        JScrollBar vertical = scroll.getVerticalScrollBar();
        vertical.setPreferredSize(new Dimension(12, 0));
        vertical.setBackground(trackColor);
        vertical.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            private JButton hiddenArrow() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                button.setFocusable(false);
                return button;
            }

            @Override protected JButton createDecreaseButton(int orientation) { return hiddenArrow(); }
            @Override protected JButton createIncreaseButton(int orientation) { return hiddenArrow(); }
            @Override protected Dimension getMinimumThumbSize() { return new Dimension(12, 44); }

            @Override protected void paintTrack(Graphics graphics, JComponent component, Rectangle bounds) {
                graphics.setColor(trackColor);
                graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override protected void paintThumb(Graphics graphics, JComponent component, Rectangle bounds) {
                if (bounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(isDragging ? new Color(131, 143, 161)
                        : isThumbRollover() ? new Color(158, 169, 185) : new Color(192, 201, 213));
                g.fillRoundRect(bounds.x + 3, bounds.y + 2, bounds.width - 6, bounds.height - 4, 6, 6);
                g.dispose();
            }
        });
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getVerticalScrollBar().setBlockIncrement(180);
    }
}
