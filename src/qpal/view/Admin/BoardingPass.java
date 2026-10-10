package qpal.view.Admin;

import java.awt.*;
import qpal.model.BookingData.Receipt;

/** Shared artwork for the printed pass and the animated paper. */
final class BoardingPass {
    private static final Image LOGO =
            new javax.swing.ImageIcon("resources/icons/bussinlogokiosk.png").getImage();

    static void draw(Graphics2D g, Receipt receipt, String seat, double width, double height) {
        double scale = Math.min(width / 700, height / 260);
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 700, 260);
        g.setPaint(new GradientPaint(0, 0, new Color(235, 0, 62), 0, 50, new Color(162, 13, 47)));
        g.fillRect(0, 0, 700, 50);
        g.fillRect(0, 210, 700, 50);
        logo(g, 14, 8, 78, 34);
        text(g, "Bus Ticket", 103, 33, 18, Color.WHITE, true);
        text(g, "Bus Ticket", 552, 33, 18, Color.WHITE, true);
        text(g, "Boarding Pass", 18, 242, 23, Color.WHITE, true);
        logo(g, 553, 218, 88, 34);
        g.setColor(new Color(218, 218, 218));
        g.drawLine(82, 68, 82, 192);
        g.drawLine(110, 132, 464, 132);
        g.drawLine(205, 70, 205, 115);
        g.drawLine(347, 70, 347, 115);
        g.drawLine(205, 146, 205, 192);
        g.drawLine(347, 146, 347, 192);
        g.setStroke(new BasicStroke(1, 0, 0, 10, new float[] {4, 4}, 0));
        g.drawLine(493, 50, 493, 260);
        Graphics2D ref = (Graphics2D) g.create();
        ref.translate(19, 195);
        ref.rotate(-Math.PI / 2);
        barcode(ref, receipt.reference(), 125, 30);
        centered(ref, receipt.reference(), 0, 34, 125, 14, 10, Color.DARK_GRAY, false);
        ref.dispose();
        String[] route = receipt.route().split(" - ", 2);
        String[] schedule = receipt.schedule().split("\\s*\\|\\s*", 2);
        field(g, "Bus No.", receipt.bus(), 110, 78, 90);
        field(g, "Origin", route[0], 218, 78, 120);
        field(g, "Destination", route.length > 1 ? route[1] : receipt.route(), 360, 78, 120);
        field(g, "Date", schedule[0], 110, 158, 90);
        field(g, "Departure Time", schedule.length > 1 ? schedule[1] : "", 218, 158, 120);
        field(g, "Seat", seat, 360, 158, 120);
        centered(g, "Boarding Queue", 503, 62, 187, 27, 15, Color.DARK_GRAY, true);
        g.setColor(new Color(224, 0, 53));
        g.fillRect(540, 95, 115, 49);
        centered(
                g,
                String.format("B%03d", receipt.queueNumber()),
                540,
                95,
                115,
                49,
                29,
                Color.WHITE,
                true);
        centered(g, "PLEASE PROCEED TO THE", 503, 153, 187, 15, 9, Color.GRAY, false);
        centered(g, "BOARDING GATE WHEN YOUR", 503, 168, 187, 15, 9, Color.GRAY, false);
        centered(g, "NUMBER IS CALLED", 503, 183, 187, 15, 9, Color.GRAY, false);
    }

    private static void logo(Graphics2D g, int x, int y, int width, int height) {
        int iw = LOGO.getWidth(null), ih = LOGO.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.min(width / (double) iw, height / (double) ih);
        int w = (int) Math.round(iw * scale), h = (int) Math.round(ih * scale);
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(LOGO, x + (width - w) / 2, y + (height - h) / 2, w, h, null);
    }

    private static void centered(
            Graphics2D g,
            String value,
            int x,
            int y,
            int width,
            int height,
            int size,
            Color color,
            boolean bold) {
        Font font = new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size);
        while (g.getFontMetrics(font).stringWidth(value) > width - 4 && size > 5)
            font = font.deriveFont((float) --size);
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(
                value,
                x + (width - metrics.stringWidth(value)) / 2,
                y + (height - metrics.getHeight()) / 2 + metrics.getAscent());
    }

    private static void field(Graphics2D g, String title, String value, int x, int y, int width) {
        text(g, title, x, y, 13, Color.GRAY, false);
        fit(g, value, x, y + 24, width, 16, Color.BLACK);
    }

    /** Code 39: start/stop characters and quiet zones are included. */
    private static void barcode(Graphics2D g, String reference, int width, int height) {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%";
        int[] patterns = {
            0x034, 0x121, 0x061, 0x160, 0x031, 0x130, 0x070, 0x025, 0x124, 0x064, 0x109, 0x049,
            0x148, 0x019, 0x118, 0x058, 0x00D, 0x10C, 0x04C, 0x01C, 0x103, 0x043, 0x142, 0x013,
            0x112, 0x052, 0x007, 0x106, 0x046, 0x016, 0x181, 0x0C1, 0x1C0, 0x091, 0x190, 0x0D0,
            0x085, 0x184, 0x0C4, 0x0A8, 0x0A2, 0x08A, 0x02A
        };
        String value = reference.toUpperCase(java.util.Locale.ROOT);
        for (char c : value.toCharArray())
            if (alphabet.indexOf(c) < 0) {
                fit(g, reference, 0, height, width, 9, Color.BLACK);
                return;
            }
        String encoded = "*" + value + "*";
        double unit = width / (double) (encoded.length() * 13 - 1 + 20);
        double x = 10 * unit;
        g.setColor(Color.BLACK);
        for (char c : encoded.toCharArray()) {
            int pattern = c == '*' ? 0x094 : patterns[alphabet.indexOf(c)];
            for (int i = 0; i < 9; i++) {
                double barWidth = ((pattern & (1 << (8 - i))) != 0 ? 2 : 1) * unit;
                if (i % 2 == 0)
                    g.fill(new java.awt.geom.Rectangle2D.Double(x, 0, barWidth, height));
                x += barWidth;
            }
            x += unit;
        }
    }

    private static void fit(
            Graphics2D g, String value, int x, int y, int width, int size, Color color) {
        g.setFont(new Font("SansSerif", Font.PLAIN, size));
        while (g.getFontMetrics().stringWidth(value) > width && size > 5)
            g.setFont(new Font("SansSerif", Font.PLAIN, --size));
        g.setColor(color);
        g.drawString(value, x, y);
    }

    private static void text(
            Graphics2D g, String text, int x, int y, int size, Color color, boolean bold) {
        g.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        g.setColor(color);
        g.drawString(text, x, y);
    }
}
