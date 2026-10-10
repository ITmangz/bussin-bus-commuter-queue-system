package qpal.view.Employee;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.DashboardDao;
import qpal.dao.EmployeeDashboardDao;
import qpal.dao.EmployeeStationDao;
import qpal.model.Account;
import qpal.view.Admin.AdminCard;
import qpal.view.Admin.AdminDashboardPanel;
import qpal.util.UiTask;

public final class EmployeeDashboardPanel extends JPanel {
    private final Account account;
    private final EmployeeStationDao.Session session;
    private final JLabel greeting = new JLabel(),
            commuters = new JLabel("—"),
            collected = new JLabel("—"),
            transactions = new JLabel("—");
    private final JPanel active = new JPanel(), queue = new JPanel();
    private final JLabel status = new JLabel("Loading your station…");
    // Add ImageIcons when the dashboard images are ready, as in the admin dashboard.
    private final JLabel lblCommutersImage = new JLabel();
    private final JLabel lblCollectedImage = new JLabel();
    private final JLabel lblQueueImage = new JLabel();
    private boolean loading;

    public EmployeeDashboardPanel(
            Account account,
            EmployeeStationDao.Session session,
            Runnable openQueue,
            Runnable openSchedules) {
        this.account = account;
        this.session = session;
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        add(label("Dashboard", 30, true, new Color(228, 0, 70)), BorderLayout.NORTH);
        JPanel body = transparent(new BorderLayout(0, 16));
        JPanel profile = new AdminCard(18);
        profile.setLayout(new BorderLayout(16, 0));
        profile.setPreferredSize(new Dimension(0, 100));
        JPanel welcome = transparent(new GridLayout(2, 1));
        greeting.setFont(new Font("SansSerif", Font.BOLD, 25));
        greeting.setForeground(Color.BLACK);
        updateProfile();
        welcome.add(greeting);
        welcome.add(label("Here's what's happening at your station.", 14, false, Color.GRAY));
        profile.add(welcome, BorderLayout.CENTER);
        body.add(profile, BorderLayout.NORTH);
        JPanel details = transparent(new BorderLayout(0, 16));
        JPanel stats = transparent(new GridLayout(1, 3, 14, 0));
        stats.setPreferredSize(new Dimension(0, 180));
        stats.add(
                AdminDashboardPanel.stat(
                        "TOTAL NUMBER OF COMMUTERS SERVED",
                        commuters,
                        lblCommutersImage,
                        "Your completed services today"));
        stats.add(
                AdminDashboardPanel.stat(
                        "TOTAL AMOUNT COLLECTED",
                        collected,
                        lblCollectedImage,
                        "Fares you collected today"));
        stats.add(
                AdminDashboardPanel.stat(
                        "TOTAL NUMBER OF TRANSACTIONS MADE",
                        transactions,
                        lblQueueImage,
                        "Your completed tickets today"));
        transactions.setToolTipText(
                "Each completed booking counts as one transaction, regardless of passenger count.");
        details.add(stats, BorderLayout.NORTH);
        JPanel bottom = transparent(new GridLayout(1, 2, 16, 0));
        bottom.add(
                AdminDashboardPanel.section(
                        "Active Boarding", active, "View schedules", openSchedules));
        bottom.add(AdminDashboardPanel.section("Queue Status", queue, "Manage queues", openQueue));
        details.add(bottom, BorderLayout.CENTER);
        body.add(details, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        status.setForeground(Color.GRAY);
        status.setVisible(false);
        add(status, BorderLayout.SOUTH);
        showMessage(active, "Loading boarding trips...");
        showMessage(queue, "Loading queue status...");
        Timer timer = new Timer(5000, e -> refreshData());
        addHierarchyListener(
                e -> {
                    if (isShowing()) {
                        timer.start();
                        refreshData();
                    } else timer.stop();
                });
    }

    public void updateProfile() {
        greeting.setText("Good to see you, " + account.getName() + "!");
    }

    public void refreshData() {
        if (loading) return;
        loading = true;
        UiTask.run(
                () -> new EmployeeDashboardDao().load(session),
                data -> {
                    loading = false;
                    showSummary(data);
                    status.setVisible(false);
                },
                ex -> {
                    loading = false;
                    status.setText("Unable to refresh. Last values may be out of date. Retrying…");
                    status.setVisible(true);
                    revalidate();
                });
    }

    public void showSummary(EmployeeDashboardDao.Summary data) {
        commuters.setText(String.valueOf(data.commuters()));
        collected.setText(String.format(java.util.Locale.ENGLISH, "PHP %,.2f", data.collected()));
        transactions.setText(String.valueOf(data.transactions()));
        active.removeAll();
        if (data.activeBoarding().isEmpty())
            showMessage(active, "No trips are currently boarding.");
        for (var trip : data.activeBoarding()) {
            active.add(AdminDashboardPanel.boardingCard(trip));
            active.add(Box.createVerticalStrut(10));
        }
        queue.removeAll();
        String stationName =
                (session.station().boarding() ? "Boarding Gate " : "Payment Counter ")
                        + session.station().number();
        queue.add(
                AdminDashboardPanel.stationCard(
                        new DashboardDao.Station(stationName, data.current(), data.stationTrip())));
        queue.add(Box.createVerticalStrut(10));
        revalidate();
        repaint();
    }

    private static void showMessage(JPanel panel, String text) {
        panel.removeAll();
        panel.add(label(text, 13, false, Color.GRAY));
    }

    private static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }
}
