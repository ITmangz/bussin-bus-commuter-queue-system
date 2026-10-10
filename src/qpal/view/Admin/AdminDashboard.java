package qpal.view.Admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import qpal.view.Login.LoginPage;

public class AdminDashboard {
    private JFrame dashpage;
    private qpal.model.Account currentAccount;
    private AdminDashboardPanel dashboardPanel;
    private EditProfilePanel profilePanel;
    private AdminQueuePanel queuePanel;
    private boolean closing;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private AdminSidebarPanel sidebar;
    private AdminTopPanel topPanel;

    public AdminDashboard() {

        this(null);
    }

    public AdminDashboard(qpal.model.Account account) {
        this.currentAccount = account;
        qpal.dao.ActivityLogDao.setCurrentAccount(account);
        qpal.util.DepartureService.start();

        dashpage = new JFrame("QPAL - Admin Dashboard");

        dashpage.setSize(1200, 700);
        dashpage.setResizable(false);
        dashpage.setLocationRelativeTo(null);
        dashpage.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dashpage.setLayout(new BorderLayout());

        sidebar = new AdminSidebarPanel(this);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        dashboardPanel = new AdminDashboardPanel(this);
        profilePanel = new EditProfilePanel(this);
        contentPanel.add(AdminCard.scrollPage(dashboardPanel, 880), "dashboard");
        queuePanel = new AdminQueuePanel();
        contentPanel.add(AdminCard.scrollPage(queuePanel, 880), "queue");
        contentPanel.add(AdminCard.scrollPage(new AdminBusPanel(), 880), "bus");
        contentPanel.add(AdminCard.scrollPage(new AdminRouteSchedPanel(), 880), "route");
        contentPanel.add(new AdminRevenuePanel(), "revenue");
        contentPanel.add(AdminCard.scrollPage(new AdminManageAccountsPanel(), 880), "accounts");
        contentPanel.add(new AdminActivityLogPanel(currentAccount), "activity");

        AdminFormStyle.styleInputs(contentPanel);
        cardLayout.show(contentPanel, "dashboard");

        dashpage.add(sidebar, BorderLayout.WEST);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new java.awt.Color(245, 245, 245));

        javax.swing.JLabel lblDateTime = new javax.swing.JLabel();
        lblDateTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDateTime.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        lblDateTime.setForeground(new java.awt.Color(110, 110, 110));

        java.time.format.DateTimeFormatter dateFormat =
                java.time.format.DateTimeFormatter.ofPattern(
                        "EEEE, MMMM d, yyyy  |  hh:mm:ss a", java.util.Locale.ENGLISH);
        lblDateTime.setText(java.time.LocalDateTime.now().format(dateFormat));

        javax.swing.Timer clockTimer =
                new javax.swing.Timer(
                        1000,
                        e -> {
                            lblDateTime.setText(java.time.LocalDateTime.now().format(dateFormat));
                        });
        clockTimer.start();

        dashpage.addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        logout(true);
                    }

                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        clockTimer.stop();
                    }
                });

        topPanel = new AdminTopPanel(this, lblDateTime);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        dashpage.add(mainPanel, BorderLayout.CENTER);

        dashpage.setVisible(true);
        dashboardPanel.refreshData();
    }

    public void logout() {
        logout(false);
    }

    private void logout(boolean exit) {
        if (closing) return;
        if (queuePanel.isActionInProgress()) {
            qpal.components.AppDialogs.showMessageDialog(
                    dashpage,
                    "Finish the current queue action or close its dialog before logging out.");
            return;
        }
        closing = true;
        int choice =
                qpal.components.AppDialogs.showConfirmDialog(
                        dashpage,
                        "Are you sure you want to log out?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            qpal.dao.ActivityLogDao.recordActivity(
                    "Authentication", "Logout", "User logged out of the system.");
            qpal.dao.ActivityLogDao.setCurrentAccount(null);
            currentAccount = null;
            dashpage.dispose();
            if (exit) System.exit(0);
            else new LoginPage();
        } else {
            closing = false;
        }
    }

    public qpal.model.Account getCurrentAccount() {

        return currentAccount;
    }

    public void showPage(String page) {
        if ("profile".equals(page)) {

            profilePanel.showDialog(contentPanel);
            topPanel.updateProfile(currentAccount);
            dashboardPanel.updateProfile();
            return;
        }
        if ("dashboard".equals(page)) {

            dashboardPanel.refreshData();
        }
        topPanel.updateProfile(currentAccount);
        cardLayout.show(contentPanel, page);
        sidebar.setSelectedPage(page);
        topPanel.setSelectedPage(page);
    }

    public static void main(String[] args) {
        new AdminDashboard();
    }
}
