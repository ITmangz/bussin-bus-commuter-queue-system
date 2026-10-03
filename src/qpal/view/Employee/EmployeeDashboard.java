package qpal.view.Employee;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import qpal.components.AppDialogs;
import qpal.dao.*;
import qpal.model.Account;
import qpal.model.EmployeeStation;
import qpal.util.UiTask;
import qpal.view.Admin.*;
import qpal.view.Login.LoginPage;

public final class EmployeeDashboard extends JFrame {
    private final Account account;
    private final CardLayout pages=new CardLayout();
    private final JPanel content=new JPanel(pages);
    private final EmployeeSidebarPanel sidebar;
    private final EmployeeTopPanel top;
    private final EmployeeStationPanel selection;
    private final EditProfilePanel profile;
    private final Timer heartbeat, clock;
    private EmployeeStationDao.Session session;
    private EmployeeDashboardPanel dashboard;
    private EmployeeQueuePanel queue;
    private boolean assigning, renewing, closing;

    public EmployeeDashboard(Account account) {
        super("QPAL - Employee Dashboard");
        if(account==null || !"Employee".equalsIgnoreCase(account.getRole())) throw new IllegalArgumentException("Sign in with an employee account.");
        this.account=account; ActivityLogDao.setCurrentAccount(account); EmployeeStationDao.activate(null);
        qpal.util.DepartureService.start();
        setSize(1200,700); setMinimumSize(new Dimension(1100,700)); setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); setLayout(new BorderLayout());
        sidebar=new EmployeeSidebarPanel(this::showPage,() -> logout(false)); add(sidebar,BorderLayout.WEST);
        JLabel time=new JLabel(); time.setFont(new Font("SansSerif",Font.PLAIN,12)); time.setForeground(new Color(110,110,110));
        DateTimeFormatter format=DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy  |  hh:mm:ss a",java.util.Locale.ENGLISH);
        Runnable tick=() -> time.setText(LocalDateTime.now().format(format)); tick.run();
        clock=new Timer(1000,e -> tick.run()); clock.start();
        top=new EmployeeTopPanel(account,time,this::showPage);
        JPanel main=new JPanel(new BorderLayout()); main.add(top,BorderLayout.NORTH); main.add(content,BorderLayout.CENTER); add(main,BorderLayout.CENTER);
        profile=new EditProfilePanel(() -> this.account,() -> showPage("dashboard"));
        selection=new EmployeeStationPanel(this::assign); content.add(selection,"station");
        heartbeat=new Timer(15000,e -> renew());
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { logout(true); }
            @Override public void windowClosed(WindowEvent e) { heartbeat.stop(); clock.stop(); }
        });
        pages.show(content,"station"); sidebar.setSelectedPage(""); setVisible(true);
    }
    private void assign(EmployeeStation station) {
        if(assigning || closing) return; assigning=true; selection.setSubmitting(true);
        UiTask.run(() -> new EmployeeStationDao().claim(account,station), claimed -> {
            assigning=false; session=claimed; EmployeeStationDao.activate(claimed);
            dashboard=new EmployeeDashboardPanel(account,claimed,() -> showPage("queue"),() -> showPage("route"));
            queue=new EmployeeQueuePanel(station);
            content.add(AdminCard.scrollPage(dashboard,880),"dashboard");
            content.add(AdminCard.scrollPage(queue,880),"queue");
            content.add(AdminCard.scrollPage(new EmployeeBusPanel(),880),"bus");
            content.add(AdminCard.scrollPage(new EmployeeRouteSchedPanel(),880),"route");
            content.add(new EmployeeActivityLogPanel(account),"activity");
            AdminFormStyle.styleInputs(content); heartbeat.start(); showPage("dashboard");
            selection.setSubmitting(false);
        }, ex -> { assigning=false; selection.setSubmitting(false); AppDialogs.showMessageDialog(this,ex.getMessage(),"Station Assignment",JOptionPane.WARNING_MESSAGE); });
    }
    public void showPage(String page) {
        if(closing) return;
        if(session==null) { pages.show(content,"station"); return; }
        if("profile".equals(page)) { profile.showDialog(content); top.updateProfile(account); dashboard.updateProfile(); return; }
        if(!java.util.Set.of("dashboard","queue","bus","route","activity").contains(page)) return;
        pages.show(content,page); sidebar.setSelectedPage(page); top.setSelectedPage(page); top.updateProfile(account);
        if("dashboard".equals(page)) { dashboard.updateProfile(); dashboard.refreshData(); }
    }
    private void renew() {
        if(renewing || session==null || closing) return; renewing=true;
        var active=session;
        UiTask.run(() -> { new EmployeeStationDao().heartbeat(active); return true; }, ok -> renewing=false, ex -> {
            renewing=false; if(closing || session!=active) return;
            if(queue!=null && queue.isActionInProgress()) return;
            heartbeat.stop(); session=null; EmployeeStationDao.activate(null);
            content.removeAll(); content.add(selection,"station"); pages.show(content,"station");
            sidebar.setSelectedPage(""); top.setSelectedPage(""); content.revalidate(); content.repaint();
            UiTask.run(() -> { new EmployeeStationDao().release(active); return true; }, ok -> selection.refresh(), ignored -> selection.refresh());
            AppDialogs.showMessageDialog(this,"Your station session could not be renewed. Select your station again.","Station Session",JOptionPane.WARNING_MESSAGE);
        });
    }
    private void logout(boolean exit) {
        if(closing || assigning) return;
        if(queue!=null && queue.isActionInProgress()) { AppDialogs.showMessageDialog(this,"Finish the current queue action or close its dialog before logging out."); return; }
        if(AppDialogs.showConfirmDialog(this,"Are you sure you want to log out?","Confirm Logout",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)!=JOptionPane.YES_OPTION) return;
        closing=true; heartbeat.stop(); setEnabled(false);
        var active=session;
        UiTask.run(() -> {
            new EmployeeStationDao().release(active);
            new ActivityLogDao().addActivity(account,"Authentication","Logout","Employee logged out"+(active==null?".":" from "+active.station().title()+"."));
            return true;
        }, ok -> finishLogout(exit), ex -> {
            AppDialogs.showMessageDialog(this,"You have been logged out. The offline station reservation will expire automatically within 90 seconds.");
            finishLogout(exit);
        });
    }
    private void finishLogout(boolean exit) {
        EmployeeStationDao.activate(null); ActivityLogDao.setCurrentAccount(null); session=null; dispose();
        if(exit) System.exit(0); else new LoginPage();
    }
}
