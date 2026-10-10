package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Shared subtle outline for admin content cards. */
public final class AdminCard extends JPanel {
    public AdminCard(int padding) {
        setOpaque(false);
        setBorder(new EmptyBorder(padding, padding, padding, padding));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
        g.setColor(new Color(232, 236, 242));
        g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
        g.dispose();
    }

    public static JScrollPane scrollPage(JPanel page, int minimumHeight) {
        JPanel canvas =
                new JPanel(new BorderLayout()) {
                    @Override
                    public Dimension getPreferredSize() {
                        Container viewport = getParent();
                        int width = viewport == null ? 900 : viewport.getWidth();
                        int height = viewport == null ? minimumHeight : viewport.getHeight();
                        return new Dimension(width, Math.max(minimumHeight, height));
                    }
                };
        canvas.setBackground(page.getBackground());
        canvas.add(page, BorderLayout.CENTER);
        JScrollPane scroll =
                new JScrollPane(
                        canvas,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        styleScrollBar(scroll, page.getBackground());
        scroll.setBackground(page.getBackground());
        scroll.getViewport().setBackground(page.getBackground());
        return scroll;
    }

    static void styleScrollBar(JScrollPane scroll, Color trackColor) {
        qpal.components.ScrollBarStyle.apply(scroll, trackColor);
    }
}
