package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class HomePanel extends JPanel {

    public HomePanel() {

        setSize(1000, 650);
        setLayout(null);

        JPanel homepanel = new JPanel(null);
        homepanel.setBounds(0, 0, 1000, 650);
        homepanel.setBackground(new Color(190, 0, 45));
        add(homepanel);

        JLabel background = new JLabel();
        background.setBounds(0, 0, 1000, 650);
        ImageIcon bgpic = new ImageIcon("resources/icons/KioskBg.png");
        Image bgimg = bgpic.getImage().getScaledInstance(1000, 650, Image.SCALE_SMOOTH);
        background.setIcon(new ImageIcon(bgimg));
        homepanel.add(background);

        JLabel bussinlogo = new JLabel();
        bussinlogo.setBounds(65, 30, 120, 55);
        ImageIcon bussinpic = new ImageIcon("resources/icons/bussinlogokiosk.png");
        Image bussinimg = bussinpic.getImage().getScaledInstance(120, 55, Image.SCALE_SMOOTH);
        bussinlogo.setIcon(new ImageIcon(bussinimg));
        homepanel.add(bussinlogo);

        JLabel datelabel = new JLabel();
        datelabel.setBounds(370, 30, 260, 25);
        datelabel.setHorizontalAlignment(SwingConstants.CENTER);
        datelabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        datelabel.setForeground(Color.WHITE);
        homepanel.add(datelabel);

        JLabel timelabel = new JLabel();
        timelabel.setBounds(370, 52, 260, 25);
        timelabel.setHorizontalAlignment(SwingConstants.CENTER);
        timelabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        timelabel.setForeground(Color.WHITE);
        homepanel.add(timelabel);

        JLabel languageicon = new JLabel();
        languageicon.setBounds(832, 28, 40, 40);
        ImageIcon languagepic = new ImageIcon("resources/icons/Language.png");
        Image languageimg = languagepic.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        languageicon.setIcon(new ImageIcon(languageimg));
        homepanel.add(languageicon);

        JLabel languagelabel = new JLabel("ENG");
        languagelabel.setBounds(870, 30, 60, 30);
        languagelabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        languagelabel.setForeground(Color.WHITE);
        homepanel.add(languagelabel);

        JLabel title1 = new JLabel("Smart Queueing");
        title1.setBounds(80, 140, 550, 65);
        title1.setFont(new Font("Segoe UI", Font.BOLD, 50));
        title1.setForeground(new Color(255, 210, 0));
        homepanel.add(title1);

        JLabel title2 = new JLabel("for Every Journey");
        title2.setBounds(80, 190, 550, 65);
        title2.setFont(new Font("Segoe UI", Font.BOLD, 50));
        title2.setForeground(Color.WHITE);

        homepanel.add(title2);

        JLabel title3 = new JLabel("<html>with <font color='#FFD200'>Bussin</font></html>");
        title3.setBounds(80, 240, 550, 65);
        title3.setFont(new Font("Segoe UI", Font.BOLD, 50));
        title3.setForeground(Color.WHITE);
        homepanel.add(title3);

        JLabel bussintext =
                new JLabel(
                        "<html>Designed for the Parañaque Integrated Terminal Exchange<br>"
                                + "(PITX), Bussin streamlines boarding across all CALABARZON<br>"
                                + "routes through a First-In, First-Out (FIFO) queue system,<br>"
                                + "enabling faster, more organized, and efficient terminal<br>"
                                + "operations.</html>");
        bussintext.setBounds(80, 310, 435, 130);
        bussintext.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        bussintext.setForeground(Color.WHITE);
        homepanel.add(bussintext);

        JButton startbtn = new JButton("Start Transaction");
        startbtn.setBounds(80, 450, 435, 55);
        startbtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startbtn.setForeground(Color.WHITE);
        startbtn.setBackground(new Color(255, 204, 0));
        startbtn.setFocusPainted(false);
        startbtn.setBorderPainted(false);
        startbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homepanel.add(startbtn);

        ActionListener btnaction1 =
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        CardLayout cardlayout = (CardLayout) getParent().getLayout();
                        cardlayout.show(getParent(), "TripDetails");
                    }
                };

        startbtn.addActionListener(btnaction1);

        JLabel homeimage = new JLabel();
        homeimage.setBounds(625, 145, 300, 270);
        ImageIcon imagepic = new ImageIcon("resources/icons/homeimage.png");
        Image imageimg = imagepic.getImage().getScaledInstance(300, 270, Image.SCALE_SMOOTH);
        homeimage.setIcon(new ImageIcon(imageimg));
        homepanel.add(homeimage);

        Timer timeTimer =
                new Timer(
                        1000,
                        new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {

                                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                                datelabel.setText(
                                        now.format(
                                                java.time.format.DateTimeFormatter.ofPattern(
                                                        "MMMM d, yyyy")));

                                timelabel.setText(
                                        now.format(
                                                java.time.format.DateTimeFormatter.ofPattern(
                                                        "hh:mm a")));
                            }
                        });

        timeTimer.start();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        datelabel.setText(now.format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        timelabel.setText(now.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));

        startbtn.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        CardLayout cardlayout = (CardLayout) getParent().getLayout();

                        cardlayout.show(getParent(), "TripDetails");
                    }
                });

        homepanel.setComponentZOrder(background, homepanel.getComponentCount() - 1);

        homepanel.setComponentZOrder(background, homepanel.getComponentCount() - 1);
    }
}
