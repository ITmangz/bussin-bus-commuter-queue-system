package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AdminSidebarPanel extends JPanel {

    private JButton dashboardBtn;
    private JButton queueBtn;
    private JButton busBtn;
    private JButton routeBtn;
    private JButton revenueBtn;
    private JButton accountsBtn;
    private JButton logoutBtn;

    public AdminSidebarPanel(AdminDashboard dashboard) {

        setPreferredSize(new Dimension(220, 700));
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(30, 20, 0, 20));

        JLabel logo = new JLabel();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(180, 70));
        logo.setMaximumSize(new Dimension(180, 70));
        logo.setMinimumSize(new Dimension(180, 70));
        ImageIcon logoImage = new ImageIcon("resources/icons/bussinlogokiosk.png");
        if(logoImage.getIconWidth() > 0) {

            int width = 140;
            int height = width * logoImage.getIconHeight() / logoImage.getIconWidth();
            logo.setIcon(new ImageIcon(logoImage.getImage()
                    .getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        } else {
            logo.setText("Bussin");
            logo.setForeground(Color.WHITE);
            logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        }
        logo.getAccessibleContext().setAccessibleName("Bussin");
        menuPanel.add(logo);
        menuPanel.add(Box.createVerticalStrut(24));

        dashboardBtn = createButton("Dashboard");
        queueBtn = createButton("Queue<br>Management");
        busBtn = createButton("Bus<br>Management");
        routeBtn = createButton("Route &amp; Schedule<br>Management");
        revenueBtn = createButton("Revenue<br>Management");
        accountsBtn = createButton("Manage<br>Accounts");
        addMenuIcon(dashboardBtn,"dashboardicon");
        addMenuIcon(queueBtn,"queueicon");
        addMenuIcon(busBtn,"busicon");
        addMenuIcon(routeBtn,"routeicon");
        addMenuIcon(revenueBtn,"revenueicon");
        addMenuIcon(accountsBtn,"manageaccsicon");

        dashboardBtn.addActionListener(e -> dashboard.showPage("dashboard"));
        queueBtn.addActionListener(e -> dashboard.showPage("queue"));
        busBtn.addActionListener(e -> dashboard.showPage("bus"));
        routeBtn.addActionListener(e -> dashboard.showPage("route"));
        revenueBtn.addActionListener(e -> dashboard.showPage("revenue"));
        accountsBtn.addActionListener(e -> dashboard.showPage("accounts"));

        JButton[] buttons = {
                dashboardBtn, queueBtn, busBtn, routeBtn, revenueBtn, accountsBtn
        };
        for(int i = 0; i < buttons.length; i++) {
            menuPanel.add(buttons[i]);
            if(i < buttons.length - 1) {

                menuPanel.add(Box.createVerticalStrut(8));
            }
        }
        add(menuPanel, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(16, 20, 24, 20));
        logoutBtn = createButton("Log Out");

        addMenuIcon(logoutBtn,"logouticon");

        logoutBtn.addActionListener(e -> dashboard.logout());
        bottom.add(logoutBtn, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setSelectedPage("dashboard");
    }

    private static void addMenuIcon(JButton button,String name) {
        ImageIcon source=new ImageIcon("resources/icons/"+name+".png");
        JLabel icon=new JLabel(new ImageIcon(source.getImage().getScaledInstance(24,24,Image.SCALE_SMOOTH)));
        icon.setBounds(16,14,24,24);
        button.add(icon);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(240, 0, 55),
                0, getHeight(), new Color(82, 8, 30)));
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
        g.fillRect(0, 0, getWidth() / 2, getHeight());
        g.dispose();
    }

    public void setSelectedPage(String page) {

        dashboardBtn.setSelected("dashboard".equals(page));
        queueBtn.setSelected("queue".equals(page));
        busBtn.setSelected("bus".equals(page));
        routeBtn.setSelected("route".equals(page));
        revenueBtn.setSelected("revenue".equals(page));
        accountsBtn.setSelected("accounts".equals(page));

        repaint();
    }
    private JButton createButton(String text) {
        JButton btn = new JButton("<html>" + text + "</html>") {

            @Override
            protected void paintComponent(Graphics graphics) {

                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if(isSelected()) {

                    g.setColor(new Color(255,255,255,48));
                    g.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                    g.setColor(Color.WHITE);
                    g.fillRoundRect(7,12,4,getHeight()-24,4,4);

                } else if(getModel().isRollover() || hasFocus()) {

                    g.setColor(new Color(255,255,255,26));
                    g.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                }

                if(getModel().isPressed()) {

                    g.setColor(new Color(255,255,255,20));
                    g.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                }

                g.dispose();
                super.paintComponent(graphics);
            }
        };
        btn.setRolloverEnabled(true);
        btn.setLayout(null);
        btn.getModel().addChangeListener(e -> {
            btn.repaint();
        });
        btn.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                btn.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                btn.repaint();
            }
        });
        Dimension size = new Dimension(180, 52);
        btn.setMaximumSize(size);
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        // Leave space for the 24 x 24 logo labels on the left.
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(4, 52, 4, 4));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

}
