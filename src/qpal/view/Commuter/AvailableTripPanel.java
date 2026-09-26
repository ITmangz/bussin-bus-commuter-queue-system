package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AvailableTripPanel extends JPanel {
    private int loadVersion;
    private int currentPage;
    private static final int TRIPS_PER_PAGE = 2;
    private final java.util.List<TripCardPanel> tripCards = new java.util.ArrayList<>();
    private final JPanel tripcontainer = new JPanel(new GridLayout(1,2,16,14));
    private final JButton previousPage = new JButton("\u25b2");
    private final JButton nextPage = new JButton("\u25bc");
    private final JLabel pageLabel = new JLabel("Page 1 of 1",SwingConstants.CENTER);

    private void showTripPage() {
        int pages = Math.max(1,(tripCards.size()+TRIPS_PER_PAGE-1)/TRIPS_PER_PAGE);
        currentPage = Math.max(0,Math.min(currentPage,pages-1));
        tripcontainer.removeAll();
        int start = currentPage*TRIPS_PER_PAGE;
        for (int i=start;i<Math.min(start+TRIPS_PER_PAGE,tripCards.size());i++)
            tripcontainer.add(tripCards.get(i));
        if (tripcontainer.getComponentCount()==1) {
            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            tripcontainer.add(spacer);
        }
        previousPage.setEnabled(currentPage>0);
        nextPage.setEnabled(currentPage<pages-1);
        pageLabel.setText("Page " + (currentPage+1) + " of " + pages);
        tripcontainer.revalidate();
        tripcontainer.repaint();
    }

    private TripDetailsPanel tripDetailsPanel;
    private PassengerDetailsPanel passengerPanel;
    
    public AvailableTripPanel(TripDetailsPanel tripDetailsPanel, PassengerDetailsPanel passengerPanel) {

        this.tripDetailsPanel = tripDetailsPanel;
        this.passengerPanel = passengerPanel;

        setSize(1000,650);
        setLayout(null);

        JPanel availabletrippanel = new JPanel(null);
        availabletrippanel.setBounds(0,0,1000,650);
        availabletrippanel.setBackground(new Color(248,248,248));
        add(availabletrippanel);

        JPanel toppanel = new JPanel(null);
        toppanel.setBounds(0,0,1000,100);
        toppanel.setBackground(new Color(225,0,45));
        availabletrippanel.add(toppanel);

        JLabel bussinlogo = new JLabel();
        bussinlogo.setBounds(35,20,120,55);
        ImageIcon bussinpic = new ImageIcon("resources/icons/bussinlogokiosk.png");
        Image bussinimg = bussinpic.getImage().getScaledInstance(120,55,Image.SCALE_SMOOTH);
        bussinlogo.setIcon(new ImageIcon(bussinimg));
        toppanel.add(bussinlogo);

        JLabel datelabel = new JLabel();
        datelabel.setBounds(370,27,260,20);
        datelabel.setHorizontalAlignment(SwingConstants.CENTER);
        datelabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        datelabel.setForeground(Color.WHITE);
        toppanel.add(datelabel);

        JLabel timelabel = new JLabel();
        timelabel.setBounds(370,47,260,20);
        timelabel.setHorizontalAlignment(SwingConstants.CENTER);
        timelabel.setFont(new Font("Segoe UI",Font.BOLD,14));
        timelabel.setForeground(Color.WHITE);
        toppanel.add(timelabel);

        JLabel startover = new JLabel("Start Over");
        startover.setBounds(822,28,100,35);
        startover.setFont(new Font("Segoe UI",Font.BOLD,16));
        startover.setForeground(Color.WHITE);
        startover.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toppanel.add(startover);

        startover.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                Kiosk.startOver(AvailableTripPanel.this);
            }
        });

        JLabel refreshicon = new JLabel();
        refreshicon.setBounds(905,25,40,40);
        ImageIcon refreshpic = new ImageIcon("resources/icons/refresh.png");
        Image refreshimg = refreshpic.getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH);
        refreshicon.setIcon(new ImageIcon(refreshimg));
        refreshicon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toppanel.add(refreshicon);

        refreshicon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                Kiosk.startOver(AvailableTripPanel.this);
            }
        });

        JPanel stepspanel = KioskStepsPanel.create(1);
        stepspanel.setBounds(0,100,1000,75);
        availabletrippanel.add(stepspanel);

        JLabel title = new JLabel("Select Available Trip");
        title.setBounds(0,205,1000,50);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI",Font.BOLD,32));
        title.setForeground(new Color(225,0,45));
        availabletrippanel.add(title);

        JLabel subtext = new JLabel("Choose your preferred trip from the available schedules.");
        subtext.setBounds(0,245,1000,30);
        subtext.setHorizontalAlignment(SwingConstants.CENTER);
        subtext.setFont(new Font("Segoe UI",Font.PLAIN,18));
        subtext.setForeground(new Color(100,100,100));
        availabletrippanel.add(subtext);



        tripcontainer.setBackground(new Color(248,248,248));

        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                int version = ++loadVersion;
                qpal.model.TripSearch search = tripDetailsPanel.getSearch();
                subtext.setText("Finding schedules for your trip...");
                TripCardPanel.clearSelection();
                tripCards.clear();
                currentPage = 0;
                showTripPage();
                tripcontainer.removeAll();
                tripcontainer.add(new JLabel("Loading available trips..."));
                tripcontainer.revalidate();
                qpal.util.UiTask.run(() -> new qpal.dao.BookingDao().availableTrips(), trips -> {
                    if (version != loadVersion) return;
                    tripCards.clear();
                currentPage = 0;
                showTripPage();
                tripcontainer.removeAll();
                    var results = search.find(trips);
                    for (qpal.model.BookingData.TripOption trip : results) {
                        tripCards.add(new TripCardPanel(trip));
                    }
                    showTripPage();
                    if (results.isEmpty()) {
                        subtext.setText("No available schedules for this destination on " + search.date() + ".");
                        tripcontainer.add(new JLabel("Please go back and choose another date or destination."));
                    } else if (results.get(0).time().equals(search.time())) {
                        subtext.setText("Available trips at your requested time.");
                    } else {
                        subtext.setText("No exact schedule. Showing the nearest times on " + search.date() + ".");
                    }
                    tripcontainer.revalidate();
                    tripcontainer.repaint();
                }, ex -> {
                    if (version != loadVersion) return;
                    subtext.setText("Unable to load schedules. Please try again.");
                    tripCards.clear();
                currentPage = 0;
                showTripPage();
                tripcontainer.removeAll();
                    tripcontainer.add(new JLabel("Unable to load trips. Go back and try again."));
                    tripcontainer.revalidate();
                    tripcontainer.repaint();
                });
            }
            @Override public void componentHidden(ComponentEvent e) { loadVersion++; }
        });
        tripcontainer.setBounds(60,310,880,128);
        availabletrippanel.add(tripcontainer);
        previousPage.setBounds(350,485,55,42);
        pageLabel.setBounds(410,485,180,42);
        nextPage.setBounds(595,485,55,42);
        for (JButton button : new JButton[]{previousPage,nextPage}) {
            button.setFont(new Font("Segoe UI",Font.PLAIN,16));
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(60,60,60));
            button.setBorder(BorderFactory.createLineBorder(new Color(205,205,205)));
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            availabletrippanel.add(button);
        }
        previousPage.setToolTipText("Previous trip page");
        nextPage.setToolTipText("Next trip page");
        availabletrippanel.add(pageLabel);
        previousPage.addActionListener(e -> { currentPage--; showTripPage(); });
        nextPage.addActionListener(e -> { currentPage++; showTripPage(); });
        showTripPage();
        JPanel bottompanel = new JPanel(null);
        bottompanel.setBounds(0,590,1000,60);
        bottompanel.setBackground(new Color(240,243,245));
        availabletrippanel.add(bottompanel);

        JButton backbtn = new JButton("Back");
        backbtn.setBounds(0,0,500,60);
        backbtn.setFont(new Font("Segoe UI",Font.PLAIN,16));
        backbtn.setForeground(Color.BLACK);
        backbtn.setBackground(new Color(240,243,245));
        backbtn.setBorderPainted(false);
        backbtn.setFocusPainted(false);
        backbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottompanel.add(backbtn);

        ActionListener backaction = new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to go back?", "Confirmation",
                        JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {

                        CardLayout cardlayout = (CardLayout) getParent().getLayout();
                        cardlayout.show(getParent(), "TripDetails");

                    } else {
                        
                        JOptionPane.showMessageDialog(null, "You chose not to proceed.");

                    }
            }
            
        };

        backbtn.addActionListener(backaction);

        JButton continuebtn = new JButton("Continue  >");
        continuebtn.setBounds(500,0,500,60);
        continuebtn.setFont(new Font("Segoe UI",Font.BOLD,16));
        continuebtn.setForeground(Color.WHITE);
        continuebtn.setBackground(new Color(225,0,45));
        continuebtn.setBorderPainted(false);
        continuebtn.setFocusPainted(false);
        continuebtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottompanel.add(continuebtn);

        ActionListener continueaction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (TripCardPanel.getSelectedCard() == null) {

                    JOptionPane.showMessageDialog(
                            null,
                            "Please select an available trip first.",
                            "No Trip Selected",
                            JOptionPane.WARNING_MESSAGE);

                    return;
                }

                CardLayout cardlayout = (CardLayout) getParent().getLayout();
                cardlayout.show(getParent(), "PassengerDetails");

            }

        };

        continuebtn.addActionListener(continueaction);

        Timer timeTimer = new Timer(1000,new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e){

                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                datelabel.setText(
                        now.format(
                                java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));

                timelabel.setText(
                        now.format(
                                java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));

            }

        });

        timeTimer.start();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        datelabel.setText(
                now.format(
                        java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        timelabel.setText(
                now.format(
                        java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));

    }
}

