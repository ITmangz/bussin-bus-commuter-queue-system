package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.DashboardDao;
import qpal.model.Account;

public class AdminDashboardPanel extends JPanel {
    private final AdminDashboard dashboard;
    private final JLabel greeting = label("", 25, Color.BLACK, true);
    private final JLabel avatar = label("", 24, new Color(180,0,50), true);
    private final JLabel available = label("—", 32, new Color(170,0,45), true);
    private final JPanel departures = new JPanel();
    private final JPanel fleet = new JPanel();
    private boolean loading;
    private final JLabel commuters = label("—",32,new Color(170,0,45),true);
    private final JLabel currentQueue = label("— | —",32,new Color(170,0,45),true);

    // Add your ImageIcons to these labels when the images are ready.
    private JLabel lblCommutersImage = new JLabel();
    private JLabel lblBusesImage = new JLabel();
    private JLabel lblQueueImage = new JLabel();

    public AdminDashboardPanel() {

        this(null);
    }

    public AdminDashboardPanel(AdminDashboard dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(245,245,245));
        setBorder(new EmptyBorder(20,25,20,25));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(label("Dashboard", 30, new Color(228,0,70), true), BorderLayout.WEST);
        add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0,16));
        body.setOpaque(false);
        JPanel profile = card();
        profile.setLayout(new BorderLayout(16,0));
        profile.setPreferredSize(new Dimension(0,100));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(255,235,240));
        avatar.setPreferredSize(new Dimension(64,64));
        profile.add(avatar, BorderLayout.WEST);
        JPanel welcome = new JPanel(new GridLayout(2,1));
        welcome.setOpaque(false);
        welcome.add(greeting);
        welcome.add(label("Here's what's happening with your system.",14,Color.GRAY,false));
        profile.add(welcome, BorderLayout.CENTER);
        JButton edit = new JButton("›");
        edit.setFont(new Font("SansSerif",Font.PLAIN,38));
        edit.setContentAreaFilled(false);
        edit.setBorderPainted(false);
        edit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        edit.setToolTipText("Edit profile");
        edit.getAccessibleContext().setAccessibleName("Edit profile");
        edit.addActionListener(e -> {
            if(dashboard != null) {

                dashboard.showPage("profile");
            }
        });
        profile.add(edit, BorderLayout.EAST);
        body.add(profile, BorderLayout.NORTH);

        JPanel details = new JPanel(new BorderLayout(0,16));
        details.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(1,3,14,0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0,180));
        stats.add(stat("TOTAL NUMBER OF COMMUTERS", commuters, lblCommutersImage, "Today's booked passengers"));
        stats.add(stat("NUMBER OF AVAILABLE BUSES", available, lblBusesImage, "Ready for assignment"));
        stats.add(stat("CURRENT QUEUE NUMBER", currentQueue, lblQueueImage, "Counter 1 | Counter 2"));
        details.add(stats, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new GridLayout(1,2,16,0));
        bottom.setOpaque(false);
        bottom.add(section("Upcoming Departures", departures, "View schedules", "route"));
        bottom.add(section("Fleet Status", fleet, "Manage buses", "bus"));
        details.add(bottom, BorderLayout.CENTER);
        body.add(details, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        Timer timer = new Timer(5000, e -> refreshData());
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) { timer.start(); refreshData(); }
                else timer.stop();
            }
        });
        updateProfile();
        showMessage(departures, "Loading upcoming trips...");
        showMessage(fleet, "Loading fleet status...");
    }

    public void updateProfile() {
        Account account = dashboard == null ? null : dashboard.getCurrentAccount();
        String name = account == null ? "Admin" : account.getName();
        if(name == null || name.trim().isEmpty()) {

            name = "Admin";
        }
        greeting.setText("Hi, " + name + "!");
        avatar.setIcon(null);
        avatar.setText(name.substring(0,1).toUpperCase());
        if(account != null && account.getProfileImage() != null
                && !account.getProfileImage().isEmpty()) {
            ImageIcon photo = new ImageIcon(account.getProfileImage());
            if(photo.getIconWidth() > 0) {

                avatar.setIcon(new ImageIcon(photo.getImage()
                        .getScaledInstance(64,64,Image.SCALE_SMOOTH)));
                avatar.setText("");
            }
        }
    }

    public void refreshData() {
        updateProfile();
        if(loading) {

            return;
        }
        loading = true;
        new SwingWorker<DashboardDao.Summary,Void>() {
            protected DashboardDao.Summary doInBackground() throws Exception {
                return new DashboardDao().loadSummary();
            }
            protected void done() {
                try {
                    DashboardDao.Summary summary = get();
                    available.setText(String.valueOf(summary.available));
                    commuters.setText(String.valueOf(summary.commuters));
                    currentQueue.setText(summary.currentQueue);
                    departures.removeAll();
                    if(summary.departures.isEmpty()) {

                        showMessage(departures, "No upcoming departures.");
                    }
                    for(String[] trip : summary.departures.subList(0, Math.min(3, summary.departures.size()))) {
                        departures.add(label(trip[0],14,new Color(55,55,55),true));
                        departures.add(label(trip[1],12,Color.GRAY,false));
                        departures.add(Box.createVerticalStrut(12));
                    }
                    fleet.removeAll();
                    fleet.add(label("Available                 " + summary.available,16,new Color(0,145,85),true));
                    fleet.add(Box.createVerticalStrut(14));
                    fleet.add(label("Maintenance           " + summary.maintenance,16,new Color(175,120,0),true));
                    fleet.add(Box.createVerticalStrut(14));
                    fleet.add(label("Inactive                    " + summary.inactive,16,Color.GRAY,true));
                    fleet.add(Box.createVerticalStrut(14));
                    fleet.add(label("Total fleet: " + summary.total,13,Color.GRAY,false));
                } catch(Exception ex) {
                    available.setText("—");
                    showMessage(departures, "Unable to load departures.");
                    showMessage(fleet, "Unable to load fleet status.");
                } finally {
                    loading = false;
                    revalidate();
                    repaint();
                }
            }
        }.execute();
    }

    private JPanel stat(String title, JLabel value, JLabel lblImage, String caption) {

        JPanel panel = card();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        Dimension imageSize = new Dimension(44,44);
        lblImage.setPreferredSize(imageSize);
        lblImage.setMinimumSize(imageSize);
        lblImage.setMaximumSize(imageSize);
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);
        lblImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblImage);
        panel.add(Box.createVerticalStrut(10));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif",Font.PLAIN,9));
        lblTitle.setForeground(new Color(100,100,100));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(8));

        value.setFont(new Font("SansSerif",Font.BOLD,32));
        value.setForeground(new Color(170,0,45));
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(value);
        panel.add(Box.createVerticalGlue());

        return panel;
    }
    private JPanel section(String title, JPanel content, String action, String page) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0,14));
        panel.add(label(title,19,new Color(55,55,55),true),BorderLayout.NORTH);
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        AdminCard.styleScrollBar(scroll, Color.WHITE);
        panel.add(scroll,BorderLayout.CENTER);
        JButton button = new JButton(action);
        button.setBackground(new Color(240,0,55));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif",Font.BOLD,13));
        button.setPreferredSize(new Dimension(0,36));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> {

            if(dashboard != null) {

                dashboard.showPage(page);
            }
        });
        panel.add(button,BorderLayout.SOUTH);
        return panel;
    }

    private void showMessage(JPanel panel, String message) {
        panel.removeAll();
        panel.add(label(message,13,Color.GRAY,false));
    }

    static JLabel label(String text, int size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif",bold ? Font.BOLD : Font.PLAIN,size));
        label.setForeground(color);
        return label;
    }

    static JPanel card() { return new AdminCard(18); }
}
