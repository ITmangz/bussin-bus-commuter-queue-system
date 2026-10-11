package qpal.view.Commuter;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/** Three picture labels moving around an oval using one animation timer. */
public final class HomeCarousel extends JLayeredPane {
    private static final int HOLD_MS = 4500; // Pause before switching (milliseconds).
    private static final int SWAP_MS = 1000; // Duration of the turning animation.
    private final JLabel[] pictures = new JLabel[3];
    private final Timer timer;
    private int front;
    private long swapStarted;
    private boolean swapping;

    public HomeCarousel() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, 406));
        String[] files = {"buspic1", "buspic2", "buspic3"};
        for (int i = 0; i < files.length; i++) {
            ImageIcon image = new ImageIcon("resources/icons/" + files[i] + ".png");
            pictures[i] = picture(image);
            pictures[i].getAccessibleContext().setAccessibleName("Travel illustration: " + files[i]);
            add(pictures[i], Integer.valueOf(0));
        }
        JPanel dots = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < 3; i++) {
                    g.setColor(i == front ? new Color(255, 210, 0) : new Color(255, 255, 255, 140));
                    g.fillRoundRect(170 + i * 22, 6, i == front ? 14 : 8, 8, 8, 8);
                }
                g.dispose();
            }
        };
        dots.setOpaque(false);
        dots.setBounds(0, 382, 400, 22);
        add(dots, Integer.valueOf(4));
        timer = new Timer(HOLD_MS, e -> animate());
        timer.setInitialDelay(HOLD_MS);
        place(0);
        addHierarchyListener(e -> {
            if (isShowing()) {
                if (!timer.isRunning()) timer.start();
            } else {
                timer.stop();
                swapping = false;
                timer.setDelay(HOLD_MS);
                place(0);
            }
        });
    }

    private void animate() {
        if (!swapping) {
            swapping = true;
            swapStarted = System.nanoTime();
            timer.setDelay(16);
        }
        double progress = Math.min(1, (System.nanoTime() - swapStarted) / (SWAP_MS * 1_000_000.0));
        place(progress * progress * (3 - 2 * progress));
        if (progress >= 1) {
            front = (front + 1) % pictures.length;
            swapping = false;
            timer.setDelay(HOLD_MS);
            place(0);
        }
    }

    private void place(double progress) {
        double[] depths = new double[pictures.length];
        for (int i = 0; i < pictures.length; i++) {
            int slot = (i - front + pictures.length) % pictures.length;
            double angle = (slot - progress) * Math.PI * 2 / 3;
            double depth = Math.cos(angle);
            depths[i] = depth;
            // Larger at the front, smaller at the back: a simple turning effect.
            int width = (int) Math.round(252 + 30 * depth);
            int height = (int) Math.round(336 + 40 * depth);
            int x = (int) Math.round(200 + 84 * Math.sin(angle) - width / 2.0);
            int y = (int) Math.round((1 - depth) * 56 / 3);
            pictures[i].setBounds(x, y, width, height);
        }
        for (int i = 0; i < pictures.length; i++) {
            int layer = 1;
            for (int j = 0; j < pictures.length; j++) {
                if (depths[j] < depths[i] || (depths[j] == depths[i] && j < i)) layer++;
            }
            setLayer(pictures[i], layer);
        }
        repaint();
    }

    private static JLabel picture(ImageIcon image) {
        return new JLabel() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int w = getWidth() - 12, h = getHeight() - 16;
                for (int spread = 6; spread >= 1; spread--) {
                    g.setColor(new Color(30, 0, 15, 9));
                    g.fillRoundRect(6 - spread, 8 - spread, w + spread * 2, h + spread * 2, 24, 24);
                }
                Shape frame = new RoundRectangle2D.Double(6, 2, w, h, 20, 20);
                g.setColor(new Color(250, 246, 238));
                g.fill(frame);
                g.clip(frame);
                if (image.getIconWidth() > 0) {
                    double scale = Math.max((double) w / image.getIconWidth(), (double) h / image.getIconHeight());
                    int iw = (int) Math.ceil(image.getIconWidth() * scale);
                    int ih = (int) Math.ceil(image.getIconHeight() * scale);
                    g.drawImage(image.getImage(), 6 + (w - iw) / 2, 2 + (h - ih) / 2, iw, ih, this);
                }
                g.setClip(null);
                g.setColor(new Color(255, 255, 255, 220));
                g.setStroke(new BasicStroke(2));
                g.draw(frame);
                g.dispose();
            }
        };
    }
}
