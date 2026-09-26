package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class SeatSelectionPanel extends JPanel {

    // Seat records loaded from the selected trip.
    public static class SeatData {

        private String seatNumber;
        private String status;

        public SeatData(String seatNumber, String status) {

            this.seatNumber = seatNumber;
            this.status = status;
        }

        public String getSeatNumber() {
            return seatNumber;
        }

        public String getStatus() {
            return status;
        }
    }

    private TripDetailsPanel tripDetailsPanel;
    private PassengerDetailsPanel passengerPanel;
    private TripCardPanel selectedTrip;
    private int tripID;
    private int seatCapacity = 40;
    private int passengerCount = 1;
    private int currentPage = 1;
    private final int seatsPerPage = 16;
    private boolean databaseSeatsLoaded;

    private final List<SeatData> seats = new ArrayList<>();
    private final List<String> selectedSeats = new ArrayList<>();

    private JPanel seatsPanel;
    private JLabel lblBus;
    private JLabel lblPassengers;
    private JLabel lblInstruction;
    private JLabel lblSelectedCount;
    private JLabel lblPage;
    private JLabel lblDate;
    private JLabel lblTime;
    private JLabel[] lblAssignments = new JLabel[3];
    private JLabel[] lblPassengerNames = new JLabel[3];
    private JPanel[] passengerCards = new JPanel[3];
    private int passengerPage = 1;
    private JButton btnPassengerPrev;
    private JButton btnPassengerNext;
    private JButton btnPrev;
    private JButton btnNext;
    private Timer timeTimer;

    public SeatSelectionPanel(TripDetailsPanel tripDetailsPanel,
            PassengerDetailsPanel passengerPanel) {

        this.tripDetailsPanel = tripDetailsPanel;
        this.passengerPanel = passengerPanel;

        setLayout(null);
        setPreferredSize(new Dimension(1000,650));
        setBackground(new Color(248,248,248));

        createHeader();
        createSteps();
        createContent();
        createBottomPanel();
        seats.clear();

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {

                loadSelection();
            }
        });

        addHierarchyListener(e -> {

            if(isShowing()) {

                updateDateTime();
                timeTimer.start();

            } else {

                timeTimer.stop();
            }
        });

        loadSelection();
    }

    private void createHeader() {

        JPanel toppanel = new JPanel(null);
        toppanel.setBounds(0,0,1000,100);
        toppanel.setBackground(new Color(225,0,45));
        add(toppanel);

        JLabel bussinlogo = new JLabel();
        bussinlogo.setBounds(35,20,120,55);
        ImageIcon bussinpic = new ImageIcon("resources/icons/bussinlogokiosk.png");
        bussinlogo.setIcon(new ImageIcon(bussinpic.getImage()
                .getScaledInstance(120,55,Image.SCALE_SMOOTH)));
        toppanel.add(bussinlogo);

        lblDate = new JLabel("",SwingConstants.CENTER);
        lblDate.setBounds(370,27,260,20);
        lblDate.setFont(new Font("Segoe UI",Font.BOLD,14));
        lblDate.setForeground(Color.WHITE);
        toppanel.add(lblDate);

        lblTime = new JLabel("",SwingConstants.CENTER);
        lblTime.setBounds(370,47,260,20);
        lblTime.setFont(new Font("Segoe UI",Font.BOLD,14));
        lblTime.setForeground(Color.WHITE);
        toppanel.add(lblTime);

        JLabel startover = new JLabel("Start Over");
        startover.setBounds(822,28,100,35);
        startover.setFont(new Font("Segoe UI",Font.BOLD,16));
        startover.setForeground(Color.WHITE);
        startover.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toppanel.add(startover);

        JLabel refreshicon = new JLabel();
        refreshicon.setBounds(905,25,40,40);
        ImageIcon refreshpic = new ImageIcon("resources/icons/refresh.png");
        refreshicon.setIcon(new ImageIcon(refreshpic.getImage()
                .getScaledInstance(40,40,Image.SCALE_SMOOTH)));
        refreshicon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toppanel.add(refreshicon);

        MouseAdapter resetAction = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                Kiosk.startOver(SeatSelectionPanel.this);
            }
        };

        startover.addMouseListener(resetAction);
        refreshicon.addMouseListener(resetAction);

        timeTimer = new Timer(1000,e -> {
            updateDateTime();
        });
        updateDateTime();
    }

    private void updateDateTime() {

        LocalDateTime now = LocalDateTime.now();
        lblDate.setText(now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        lblTime.setText(now.format(DateTimeFormatter.ofPattern("hh:mm a")));
    }

    private void createSteps() {

        JPanel stepspanel = new JPanel(null);
        stepspanel.setBounds(0,100,1000,75);
        stepspanel.setBackground(new Color(240,243,245));
        add(stepspanel);

        String[] titles = {"Trip Details","Available Trips","Passengers",
                "Select Seats","Review Trip","Payment"};

        for(int i = 0; i < titles.length; i++) {

            int x = i * 1000 / 6;
            int width = (i + 1) * 1000 / 6 - x;

            JPanel step = new JPanel(null);
            step.setBounds(x,0,width,75);
            step.setBackground(new Color(240,243,245));
            step.setBorder(BorderFactory.createMatteBorder(0,0,0,1,new Color(220,220,220)));

            JLabel lblNumber = new JLabel("STEP 0" + (i + 1));
            lblNumber.setBounds(14,12,width-20,20);
            lblNumber.setFont(new Font("Segoe UI",Font.PLAIN,11));
            lblNumber.setForeground(Color.GRAY);
            step.add(lblNumber);

            JLabel lblTitle = new JLabel(titles[i]);
            lblTitle.setBounds(14,34,width-20,22);
            lblTitle.setFont(new Font("Segoe UI",Font.BOLD,13));
            step.add(lblTitle);

            if(i == 3) {

                step.setBackground(new Color(255,235,240));
                lblTitle.setForeground(new Color(225,0,45));

                JPanel activebar = new JPanel();
                activebar.setBounds(0,72,width,3);
                activebar.setBackground(new Color(225,0,45));
                step.add(activebar);
            }

            stepspanel.add(step);
        }
    }

    private void createContent() {

        JLabel title = new JLabel("Seat Selection",SwingConstants.CENTER);
        title.setBounds(0,205,1000,50);
        title.setForeground(new Color(225,0,45));
        title.setFont(new Font("Segoe UI",Font.BOLD,32));
        add(title);

        lblBus = new JLabel("",SwingConstants.CENTER);
        lblBus.setBounds(0,248,1000,24);
        lblBus.setFont(new Font("Segoe UI",Font.PLAIN,16));
        lblBus.setForeground(new Color(100,100,100));
        add(lblBus);

        JPanel summary = new JPanel(null);
        summary.setBounds(35,282,260,292);
        summary.setOpaque(false);

        add(summary);

        lblPassengers = new JLabel();
        lblPassengers.setBounds(16,12,228,22);
        lblPassengers.setFont(new Font("Segoe UI",Font.BOLD,16));
        summary.add(lblPassengers);

        lblInstruction = new JLabel();
        lblInstruction.setBounds(16,36,228,20);
        lblInstruction.setForeground(new Color(100,100,100));
        summary.add(lblInstruction);

        for(int i = 0; i < lblAssignments.length; i++) {

            JPanel card = new JPanel(null);
            card.setBounds(0,62+i*62,260,56);
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createLineBorder(new Color(215,215,215)));
            passengerCards[i] = card;

            lblPassengerNames[i] = new JLabel();
            lblPassengerNames[i].setBounds(14,16,160,24);
            lblPassengerNames[i].setFont(new Font("Segoe UI",Font.BOLD,14));
            card.add(lblPassengerNames[i]);

            lblAssignments[i] = new JLabel("",SwingConstants.RIGHT);
            lblAssignments[i].setBounds(174,14,70,28);
            lblAssignments[i].setFont(new Font("Segoe UI",Font.BOLD,22));
            lblAssignments[i].setForeground(new Color(225,0,45));
            card.add(lblAssignments[i]);
            summary.add(card);
        }

        btnPassengerPrev = createButton("<");
        btnPassengerPrev.setBounds(0,250,40,30);
        btnPassengerPrev.addActionListener(e -> {

            if(passengerPage > 1) {

                passengerPage--;
                loadSeatPage();
            }
        });
        summary.add(btnPassengerPrev);

        lblSelectedCount = new JLabel("",SwingConstants.CENTER);
        lblSelectedCount.setBounds(44,250,172,30);
        lblSelectedCount.setFont(new Font("Segoe UI",Font.PLAIN,12));
        lblSelectedCount.setForeground(new Color(100,100,100));
        summary.add(lblSelectedCount);

        btnPassengerNext = createButton(">");
        btnPassengerNext.setBounds(220,250,40,30);
        btnPassengerNext.addActionListener(e -> {

            if(passengerPage * 3 < passengerCount) {

                passengerPage++;
                loadSeatPage();
            }
        });
        summary.add(btnPassengerNext);
        JLabel lblFront = new JLabel("DRIVER",SwingConstants.LEFT);
        lblFront.setBounds(350,281,200,22);
        lblFront.setFont(new Font("Segoe UI",Font.BOLD,12));
        lblFront.setForeground(new Color(100,100,100));
        add(lblFront);

        JLabel lblEntrance = new JLabel("ENTRANCE",SwingConstants.RIGHT);
        lblEntrance.setBounds(670,281,200,22);
        lblEntrance.setFont(new Font("Segoe UI",Font.BOLD,12));
        lblEntrance.setForeground(new Color(100,100,100));
        add(lblEntrance);

        String[] columns = {"A","B","AISLE","C","D"};
        int[] positions = {350,460,565,670,780};

        for(int i = 0; i < columns.length; i++) {

            JLabel lblColumn = new JLabel(columns[i],SwingConstants.CENTER);
            lblColumn.setBounds(positions[i],305,90,18);
            lblColumn.setFont(new Font("Segoe UI",Font.PLAIN,12));
            lblColumn.setForeground(Color.GRAY);
            add(lblColumn);
        }

        seatsPanel = new JPanel(null);
        seatsPanel.setBounds(330,330,620,190);
        seatsPanel.setOpaque(false);
        add(seatsPanel);

        btnPrev = createButton("▲");
        btnPrev.setToolTipText("Previous seat page");
        btnPrev.setBounds(900,330,60,44);
        btnPrev.addActionListener(e -> {

            if(currentPage > 1) {

                currentPage--;
                loadSeatPage();
            }
        });
        add(btnPrev);

        lblPage = new JLabel("",SwingConstants.CENTER);
        lblPage.setBounds(350,523,520,28);
        add(lblPage);

        btnNext = createButton("▼");
        btnNext.setToolTipText("Next seat page");
        btnNext.setBounds(900,473,60,44);
        btnNext.addActionListener(e -> {

            if(currentPage < getTotalPages()) {

                currentPage++;
                loadSeatPage();
            }
        });
        add(btnNext);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,22,0));
        legendPanel.setBounds(350,559,520,24);
        legendPanel.setOpaque(false);
        add(legendPanel);

        String[] legendText = {"Available","Selected","Occupied / Reserved"};
        Color[] legendColors = {Color.WHITE,new Color(225,0,45),new Color(220,220,220)};

        for(int i = 0; i < legendText.length; i++) {

            JPanel legendItem = new JPanel(new FlowLayout(FlowLayout.LEFT,7,0));
            legendItem.setOpaque(false);
            JPanel swatch = new JPanel();
            swatch.setPreferredSize(new Dimension(16,16));
            swatch.setBackground(legendColors[i]);
            swatch.setBorder(BorderFactory.createLineBorder(new Color(190,190,190)));
            legendItem.add(swatch);

            JLabel lblLegend = new JLabel(legendText[i]);

            lblLegend.setFont(new Font("Segoe UI",Font.PLAIN,11));
            lblLegend.setForeground(new Color(90,90,90));
            legendItem.add(lblLegend);
            legendPanel.add(legendItem);
        }
    }
    private void createBottomPanel() {

        JPanel bottompanel = new JPanel(null);
        bottompanel.setBounds(0,590,1000,60);
        bottompanel.setBackground(new Color(240,243,245));
        add(bottompanel);

        JButton backbtn = new JButton("Back");
        backbtn.setBounds(0,0,500,60);
        backbtn.setFont(new Font("Segoe UI",Font.PLAIN,16));
        backbtn.setForeground(Color.BLACK);
        backbtn.setBackground(new Color(240,243,245));
        backbtn.setFocusPainted(false);
        backbtn.setBorderPainted(false);
        backbtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backbtn.addActionListener(e -> {

            int choice = qpal.components.AppDialogs.showConfirmDialog(this,"Are you sure you want to go back?",
                    "Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);

            if(choice == JOptionPane.YES_OPTION) {

                showPage("PassengerDetails");
            }
        });
        bottompanel.add(backbtn);

        JButton continuebtn = new JButton("Continue  >");
        continuebtn.setBounds(500,0,500,60);
        continuebtn.setFont(new Font("Segoe UI",Font.BOLD,16));
        continuebtn.setForeground(Color.WHITE);
        continuebtn.setBackground(new Color(235,25,35));
        continuebtn.setFocusPainted(false);
        continuebtn.setBorderPainted(false);
        continuebtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        continuebtn.addActionListener(e -> {

            if(selectedSeats.size() != passengerCount) {

                qpal.components.AppDialogs.showMessageDialog(this,"Please select one seat for each passenger.",
                        "Warning",JOptionPane.WARNING_MESSAGE);
                return;
            }

            if(selectedTrip == null || !databaseSeatsLoaded) {

                qpal.components.AppDialogs.showMessageDialog(this,"Please select a trip first.",
                        "Warning",JOptionPane.WARNING_MESSAGE);
                return;
            }

            showPage("ConfirmTripDetails");
        });
        bottompanel.add(continuebtn);
    }

    private JButton createButton(String text) {

        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI",Font.BOLD,13));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(60,60,60));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(210,210,210)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Call on the Swing event thread after retrieving records from your DAO.
    // Supply records in bus row order: 1A, 1B, 1C, 1D, 2A...
    public void setSeatData(int tripID, String busName, int capacity, List<SeatData> records) {

        if(records == null || capacity < 1 || records.size() != capacity) {
            throw new IllegalArgumentException("Provide one seat record for each seat in the bus.");
        }

        java.util.Set<String> numbers = new java.util.HashSet<>();

        for(SeatData seat : records) {

            if(seat == null || seat.getSeatNumber() == null || seat.getSeatNumber().trim().isEmpty()
                    || !numbers.add(seat.getSeatNumber())) {
                throw new IllegalArgumentException("Seat numbers must be nonempty and unique.");
            }
        }

        if(this.tripID != tripID) {

            selectedSeats.clear();
            currentPage = 1;
        }

        this.tripID = tripID;
        seatCapacity = capacity;
        databaseSeatsLoaded = true;
        selectedTrip = TripCardPanel.getSelectedCard();
        seats.clear();
        seats.addAll(records);
        selectedSeats.removeIf(number -> !isAvailable(number));
        lblBus.setText(busName);
        updateSelectionLabels();
    }

    private boolean isAvailable(String number) {

        for(SeatData seat : seats) {

            if(seat.getSeatNumber().equals(number)) {
                return "Available".equalsIgnoreCase(seat.getStatus());
            }
        }

        return false;
    }

    public void loadSelection() {
        TripCardPanel card = TripCardPanel.getSelectedCard();
        if (card == null || card.getTrip() == null) {
            resetInputs();
            lblBus.setText("Select a bus first");
            return;
        }
        if (selectedTrip != card) selectedSeats.clear();
        selectedTrip = card;
        databaseSeatsLoaded = false;
        seats.clear();
        lblBus.setText("Loading seats...");
        updateSelectionLabels();
        qpal.model.BookingData.TripOption trip = card.getTrip();
        qpal.util.UiTask.run(() -> new qpal.dao.BookingDao().occupiedSeats(trip.id()), occupied -> {
            if (TripCardPanel.getSelectedCard() != card) return;
            List<SeatData> records = new ArrayList<>();
            for (int i = 1; i <= trip.capacity(); i++)
                records.add(new SeatData(qpal.dao.BookingDao.seatLabel(i), occupied.contains(i) ? "Reserved" : "Available"));
            setSeatData(trip.id(), trip.bus(), trip.capacity(), records);
        }, ex -> {
            if (TripCardPanel.getSelectedCard() != card) return;
            selectedSeats.clear();
            lblBus.setText("Unable to load seats. Go back and try again.");
            updateSelectionLabels();
        });
    }

    private void updateSelectionLabels() {
        passengerCount = passengerPanel.getPassengerCount();
        while (selectedSeats.size() > passengerCount) selectedSeats.remove(selectedSeats.size() - 1);
        lblPassengers.setText("Passengers: " + passengerCount);
        lblInstruction.setText("Select " + passengerCount + " seats");
        currentPage = Math.min(currentPage, getTotalPages());
        loadSeatPage();
    }
    private int getTotalPages() {
        return Math.max(1,(seats.size()+seatsPerPage-1)/seatsPerPage);
    }

    private void loadSeatPage() {

        seatsPanel.removeAll();
        int start = (currentPage-1)*seatsPerPage;
        int end = Math.min(start+seatsPerPage,seats.size());
        int[] positions = {20,130,340,450};

        for(int i = start; i < end; i++) {

            SeatData seat = seats.get(i);
            int row = (i-start)/4;
            int column = (i-start)%4;
            JButton btnSeat = createButton(seat.getSeatNumber());
            btnSeat.setBounds(positions[column],row*47,90,38);

            if(!"Available".equalsIgnoreCase(seat.getStatus())) {

                btnSeat.setBackground(new Color(220,220,220));
                btnSeat.setForeground(Color.GRAY);
                btnSeat.setEnabled(false);

            } else if(selectedSeats.contains(seat.getSeatNumber())) {

                btnSeat.setBackground(new Color(225,0,45));
                btnSeat.setForeground(Color.WHITE);
            }

            btnSeat.setToolTipText(seat.getStatus());
            btnSeat.addActionListener(e -> {

                String number = seat.getSeatNumber();

                if(selectedSeats.contains(number)) {

                    selectedSeats.remove(number);

                } else if(selectedSeats.size() < passengerCount) {

                    selectedSeats.add(number);

                } else {

                    qpal.components.AppDialogs.showMessageDialog(this,"You have selected a seat for every passenger. Deselect a seat to change it.",
                            "Warning",JOptionPane.WARNING_MESSAGE);
                }

                loadSeatPage();
            });
            seatsPanel.add(btnSeat);
        }

        lblPage.setText("Page " + currentPage + " of " + getTotalPages());
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < getTotalPages());

        passengerPage = Math.min(passengerPage,Math.max(1,(passengerCount+2)/3));

        for(int i = 0; i < lblAssignments.length; i++) {

            int passenger = (passengerPage-1)*3+i;
            passengerCards[i].setVisible(passenger < passengerCount);
            lblPassengerNames[i].setText("Passenger " + (passenger+1));
            String number = "—";

            if(passenger < selectedSeats.size()) {

                number = selectedSeats.get(passenger);
                passengerCards[i].setBorder(BorderFactory.createLineBorder(new Color(225,0,45),2));

            } else {

                passengerCards[i].setBorder(BorderFactory.createLineBorder(new Color(215,215,215)));
            }

            lblAssignments[i].setText(number);
        }

        lblSelectedCount.setText(selectedSeats.size() + " / " + passengerCount + " selected");
        lblSelectedCount.setToolTipText("Passenger page " + passengerPage);
        btnPassengerPrev.setEnabled(passengerPage > 1);
        btnPassengerNext.setEnabled(passengerPage * 3 < passengerCount);
        seatsPanel.revalidate();
        seatsPanel.repaint();
    }

    public String getSelectedSeats() {
        return String.join(", ",selectedSeats);
    }

    public List<String> getSelectedSeatNumbers() {
        return new ArrayList<>(selectedSeats);
    }

    public int getTripID() {
        return tripID;
    }

    public void resetInputs() {

        selectedSeats.clear();
        selectedTrip = null;
        tripID = 0;
        currentPage = 1;
        databaseSeatsLoaded = false;
        seatCapacity = 40;
        seats.clear();
        loadSeatPage();
    }

    private void showPage(String page) {

        if(getParent() != null && getParent().getLayout() instanceof CardLayout) {

            CardLayout cardlayout = (CardLayout)getParent().getLayout();
            cardlayout.show(getParent(),page);
        }
    }
}
