package qpal.components;
import java.awt.*;
import javax.swing.*;

/** Shared slim scrollbar with a rounded thumb and no arrow buttons. */
public final class ScrollBarStyle {
    private ScrollBarStyle() {}
    public static void apply(JScrollPane scroll, Color trackColor) {
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
