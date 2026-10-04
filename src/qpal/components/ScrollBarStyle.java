package qpal.components;
import java.awt.*;
import javax.swing.*;

/** Shared slim scrollbar with a rounded thumb and no arrow buttons. */
public final class ScrollBarStyle {
    private ScrollBarStyle() {}
    public static void apply(JScrollPane scroll, Color trackColor) {
        style(scroll.getVerticalScrollBar(), trackColor);
        style(scroll.getHorizontalScrollBar(), trackColor);
    }

    private static void style(JScrollBar bar, Color trackColor) {
        boolean vertical = bar.getOrientation() == JScrollBar.VERTICAL;
        bar.setPreferredSize(vertical ? new Dimension(12, 0) : new Dimension(0, 12));
        bar.setBackground(trackColor);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
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
            @Override protected Dimension getMinimumThumbSize() {
                return vertical ? new Dimension(12, 44) : new Dimension(44, 12);
            }

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
                int insetX = vertical ? 3 : 2;
                int insetY = vertical ? 2 : 3;
                g.fillRoundRect(bounds.x + insetX, bounds.y + insetY,
                        bounds.width - insetX * 2, bounds.height - insetY * 2, 6, 6);
                g.dispose();
            }
        });
        bar.setUnitIncrement(24);
        bar.setBlockIncrement(180);
    }
}
