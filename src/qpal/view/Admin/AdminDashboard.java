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

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private AdminSidebarPanel sidebar;

    public AdminDashboard() {

        this(null);
    }

    public AdminDashboard(qpal.model.Account account) {
        this.currentAccount = account;
        qpal.util.DepartureService.start();

        dashpage = new JFrame("QPAL - Admin Dashboard");

        dashpage.setSize(1200, 700);
        dashpage.setResizable(false);
        dashpage.setLocationRelativeTo(null);
        dashpage.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        dashpage.setLayout(new BorderLayout());

        sidebar = new AdminSidebarPanel(this);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        dashboardPanel = new AdminDashboardPanel(this);
        profilePanel = new EditProfilePanel(this);
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(AdminCard.scrollPage(new AdminQueuePanel(), 880), "queue");
        contentPanel.add(new AdminBusPanel(), "bus");
        contentPanel.add(new AdminRouteSchedPanel(), "route");
        contentPanel.add(new AdminRevenuePanel(), "revenue");
        contentPanel.add(new AdminManageAccountsPanel(), "accounts");

        AdminFormStyle.styleInputs(contentPanel);
        cardLayout.show(contentPanel, "dashboard");

        dashpage.add(sidebar, BorderLayout.WEST);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new java.awt.Color(245,245,245));

        javax.swing.JLabel lblDateTime = new javax.swing.JLabel();
        lblDateTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDateTime.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,12));
        lblDateTime.setForeground(new java.awt.Color(110,110,110));
        lblDateTime.setBorder(new javax.swing.border.EmptyBorder(12,25,0,25));

        java.time.format.DateTimeFormatter dateFormat =
                java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy  |  hh:mm:ss a",java.util.Locale.ENGLISH);
        lblDateTime.setText(java.time.LocalDateTime.now().format(dateFormat));

        javax.swing.Timer clockTimer = new javax.swing.Timer(1000,e -> {
            lblDateTime.setText(java.time.LocalDateTime.now().format(dateFormat));
        });
        clockTimer.start();

        dashpage.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                clockTimer.stop();
            }
        });

        mainPanel.add(lblDateTime,BorderLayout.NORTH);
        mainPanel.add(contentPanel,BorderLayout.CENTER);
        dashpage.add(mainPanel,BorderLayout.CENTER);

        dashpage.setVisible(true);
        dashboardPanel.refreshData();
    }

    public void logout() {
        int choice = JOptionPane.showConfirmDialog(
                dashpage,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            currentAccount = null;
            dashpage.dispose();
            new LoginPage();
        }
    }

    public qpal.model.Account getCurrentAccount() {

        return currentAccount;
    }

    public void showPage(String page) {
        if("profile".equals(page)) {

            profilePanel.showDialog(contentPanel);
            return;
        }
        if("dashboard".equals(page)) {

            dashboardPanel.refreshData();
        }
        cardLayout.show(contentPanel, page);
        sidebar.setSelectedPage(page);
    }

    public static void main(String[] args) {
        new AdminDashboard();
    }
}
