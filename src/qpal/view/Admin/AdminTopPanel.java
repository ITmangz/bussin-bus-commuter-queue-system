package qpal.view.Admin;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.model.Account;

/** Persistent clock and account controls for admin pages. */
public class AdminTopPanel extends JPanel {
    private final JLabel photo = new JLabel();
    private final JLabel name = new JLabel();
    private final JLabel role = new JLabel();
    private final JLabel currentPage = new JLabel();

    public AdminTopPanel(AdminDashboard dashboard, JLabel clock) {
        this(dashboard.getCurrentAccount(), clock, dashboard::showPage);
    }

    public AdminTopPanel(
            Account currentAccount, JLabel clock, java.util.function.Consumer<String> navigate) {
        setLayout(new BorderLayout(16, 0));
        setBackground(new Color(248, 249, 251));
        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 228, 234)),
                        new EmptyBorder(10, 25, 10, 20)));
        setPreferredSize(new Dimension(0, 64));

        JButton home = new JButton("Home >");
        home.setFont(new Font("SansSerif", Font.PLAIN, 12));
        home.setForeground(new Color(110, 115, 125));
        home.setBorder(BorderFactory.createEmptyBorder());
        home.setContentAreaFilled(false);
        home.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        home.addActionListener(e -> navigate.accept("dashboard"));

        JPanel breadcrumb = new JPanel(new BorderLayout(8, 0));
        breadcrumb.setOpaque(false);
        breadcrumb.add(home, BorderLayout.WEST);
        currentPage.setFont(new Font("SansSerif", Font.BOLD, 12));
        currentPage.setForeground(new Color(228, 0, 70));
        breadcrumb.add(currentPage, BorderLayout.CENTER);
        add(breadcrumb, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(20, 0));
        right.setOpaque(false);
        right.add(clock, BorderLayout.WEST);

        JPanel account = new JPanel(new BorderLayout(12, 0));
        account.setOpaque(false);
        account.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(225, 228, 234)),
                        new EmptyBorder(0, 20, 0, 0)));
        account.add(photo, BorderLayout.WEST);
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 2));
        labels.setOpaque(false);
        labels.setBorder(new EmptyBorder(3, 0, 3, 12));
        name.setFont(new Font("SansSerif", Font.BOLD, 12));
        name.setForeground(new Color(45, 50, 60));
        role.setFont(new Font("SansSerif", Font.PLAIN, 11));
        role.setForeground(new Color(120, 125, 135));
        labels.add(name);
        labels.add(role);
        account.add(labels, BorderLayout.CENTER);

        JButton edit = new JButton("\u2304");
        edit.setFont(new Font("SansSerif", Font.PLAIN, 20));
        edit.setForeground(new Color(70, 75, 85));
        edit.setContentAreaFilled(false);
        edit.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        edit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        edit.getAccessibleContext().setAccessibleName("Edit profile");
        edit.addActionListener(e -> navigate.accept("profile"));
        account.add(edit, BorderLayout.EAST);
        right.add(account, BorderLayout.EAST);
        add(right, BorderLayout.EAST);
        updateProfile(currentAccount);
    }

    public void setSelectedPage(String page) {

        String title = "";

        if ("queue".equals(page)) {
            title = "Queue Management";
        } else if ("bus".equals(page)) {
            title = "Bus Management";
        } else if ("route".equals(page)) {
            title = "Route & Schedule Management";
        } else if ("revenue".equals(page)) {
            title = "Revenue Management";
        } else if ("accounts".equals(page)) {
            title = "Manage Accounts";
        } else if ("activity".equals(page)) {
            title = "Activity Log";
        }

        currentPage.setText(title);
    }

    public void updateProfile(Account account) {
        String displayName = account == null ? null : account.getName();
        if (displayName == null || displayName.trim().isEmpty()) displayName = "Admin";
        name.setText(displayName);
        String displayRole = account == null ? null : account.getRole();
        role.setText(
                displayRole != null && "employee".equalsIgnoreCase(displayRole.trim())
                        ? "Employee"
                        : "Admin");
        BufferedImage image = new BufferedImage(36, 36, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setClip(new Ellipse2D.Double(0, 0, 36, 36));
        g.setColor(new Color(255, 230, 237));
        g.fillRect(0, 0, 36, 36);
        ImageIcon source =
                account == null || account.getProfileImage() == null
                        ? null
                        : new ImageIcon(account.getProfileImage());
        if (source != null && source.getIconWidth() > 0 && source.getIconHeight() > 0) {
            double scale = Math.max(36.0 / source.getIconWidth(), 36.0 / source.getIconHeight());
            int width = (int) Math.ceil(source.getIconWidth() * scale);
            int height = (int) Math.ceil(source.getIconHeight() * scale);
            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(
                    source.getImage(), (36 - width) / 2, (36 - height) / 2, width, height, null);
        } else {
            g.setColor(new Color(190, 0, 50));
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            String initial = displayName.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(
                    initial,
                    (36 - metrics.stringWidth(initial)) / 2,
                    (36 - metrics.getHeight()) / 2 + metrics.getAscent());
        }
        g.dispose();
        photo.setIcon(new ImageIcon(image));
        photo.getAccessibleContext().setAccessibleName(displayName + " profile picture");
    }
}
