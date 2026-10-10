package qpal.view.Commuter;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class PassengerDetailsPanel extends JPanel {
    private static final String[] TYPES = {"Regular", "Student", "Senior", "PWD"};
    private final int[] counts = {1, 0, 0, 0};
    private final JLabel[] passengerImageLabels = new JLabel[4];

    public JLabel getRegularImageLabel() {
        return passengerImageLabels[0];
    }

    public JLabel getStudentImageLabel() {
        return passengerImageLabels[1];
    }

    public JLabel getSeniorImageLabel() {
        return passengerImageLabels[2];
    }

    public JLabel getPwdImageLabel() {
        return passengerImageLabels[3];
    }

    private final JLabel[] values = new JLabel[4];
    private final JButton[] plus = new JButton[4], minus = new JButton[4];
    private final JPanel pages = new JPanel(new CardLayout());
    private final JPanel forms = new JPanel();
    private final List<PassengerForm> passengers = new ArrayList<>();
    private boolean details;
    private int detailIndex;
    private JButton next;
    private final JLabel subtitle =
            new JLabel(
                    "Use + or − to add or remove passengers in each category.",
                    SwingConstants.CENTER);

    private List<PassengerForm> requiredForms() {
        return passengers.stream().filter(p -> !p.type.equals("Regular")).toList();
    }

    private void showDetail(int index) {
        detailIndex = index;
        details = true;
        pages.setBounds(70, 282, 860, 184);
        ((CardLayout) forms.getLayout()).show(forms, Integer.toString(index));
        ((CardLayout) pages.getLayout()).show(pages, "Details");
        subtitle.setText(
                "ID details "
                        + (index + 1)
                        + " of "
                        + requiredForms().size()
                        + " — Enter the information shown on your ID.");
    }

    // Kept as the regular counter for existing capacity checks.
    private JButton plusbtn;

    public PassengerDetailsPanel(TripDetailsPanel tripDetailsPanel) {
        setLayout(null);
        setSize(1000, 650);
        setBackground(new Color(248, 248, 248));
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 1000, 100);
        header.setBackground(new Color(225, 0, 45));
        JLabel logo =
                new JLabel(
                        new ImageIcon(
                                new ImageIcon("resources/icons/bussinlogokiosk.png")
                                        .getImage()
                                        .getScaledInstance(120, 55, Image.SCALE_SMOOTH)));
        logo.setBounds(35, 20, 120, 55);
        header.add(logo);
        JButton reset = button("Start Over", new Color(225, 0, 45), Color.WHITE);
        reset.setFont(new Font("Segoe UI", Font.BOLD, 16));
        reset.setBounds(805, 25, 115, 45);
        reset.addActionListener(e -> Kiosk.startOver(this));
        header.add(reset);
        JButton refresh = button("", new Color(225, 0, 45), Color.WHITE);
        refresh.setIcon(
                new ImageIcon(
                        new ImageIcon("resources/icons/refresh.png")
                                .getImage()
                                .getScaledInstance(36, 36, Image.SCALE_SMOOTH)));
        refresh.setBounds(920, 25, 45, 45);
        refresh.addActionListener(e -> Kiosk.startOver(this));
        header.add(refresh);
        JLabel date = new JLabel("", SwingConstants.CENTER),
                time = new JLabel("", SwingConstants.CENTER);
        date.setBounds(370, 27, 260, 20);
        time.setBounds(370, 47, 260, 20);
        for (JLabel label : new JLabel[] {date, time}) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(Color.WHITE);
            header.add(label);
        }
        Runnable clock =
                () -> {
                    var now = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Manila"));
                    date.setText(
                            now.format(
                                    java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));
                    time.setText(
                            now.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
                };
        clock.run();
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> clock.run());
        addHierarchyListener(
                e -> {
                    if (isShowing()) timer.start();
                    else timer.stop();
                });
        add(header);
        JPanel steps = KioskStepsPanel.create(2);
        steps.setBounds(0, 100, 1000, 75);
        add(steps);
        JLabel title = new JLabel("Passenger Details", SwingConstants.CENTER);
        title.setForeground(new Color(225, 0, 45));
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setBounds(70, 192, 860, 44);
        add(title);
        subtitle.setBounds(70, 238, 860, 25);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(85, 94, 108));
        add(subtitle);
        pages.setBounds(70, 282, 860, 290);
        pages.setOpaque(false);
        add(pages);
        JPanel selection = new JPanel(null);
        selection.setOpaque(false);
        for (int i = 0; i < TYPES.length; i++) {
            final int index = i;
            JPanel card = new RoundedPanel();

            card.setBounds(i * 220, 8, 200, 258);
            JLabel imageLabel = new JLabel();
            imageLabel.setName(TYPES[i].toLowerCase(java.util.Locale.ROOT) + "ImageLabel");
            imageLabel.setBounds(30, 14, 140, 110);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.getAccessibleContext().setAccessibleName(TYPES[i] + " passenger image");
            ImageIcon passengerIcon =
                    new ImageIcon(
                            "resources/icons/"
                                    + TYPES[i].toLowerCase(java.util.Locale.ROOT)
                                    + "ic.png");
            imageLabel.setIcon(
                    new ImageIcon(
                            passengerIcon.getImage().getScaledInstance(140, 110, Image.SCALE_SMOOTH)));
            passengerImageLabels[i] = imageLabel;
            card.add(imageLabel);
            JLabel label = new JLabel(TYPES[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 18));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBounds(10, 130, 180, 28);
            card.add(label);
            JLabel discount =
                    new JLabel(i == 0 ? "Standard fare" : "20% discount", SwingConstants.CENTER);
            discount.setBounds(10, 158, 180, 24);
            card.add(discount);
            minus[i] = counterButton("−", new Color(240, 243, 245), Color.BLACK);
            minus[i].setBounds(16, 194, 44, 44);
            plus[i] = counterButton("+", new Color(225, 0, 45), Color.WHITE);
            plus[i].setBounds(140, 194, 44, 44);
            values[i] = new JLabel("0", SwingConstants.CENTER);
            values[i].setFont(new Font("Segoe UI", Font.BOLD, 24));
            values[i].setBounds(64, 194, 72, 44);
            minus[i].getAccessibleContext().setAccessibleName("Remove " + TYPES[i] + " passenger");
            plus[i].getAccessibleContext().setAccessibleName("Add " + TYPES[i] + " passenger");
            minus[i].addActionListener(
                    e -> {
                        if (counts[index] > 0) counts[index]--;
                        updatePassengerCounter();
                    });
            plus[i].addActionListener(
                    e -> {
                        if (getPassengerCount() < passengerLimit()) counts[index]++;
                        updatePassengerCounter();
                    });
            card.add(minus[i]);
            card.add(values[i]);
            card.add(plus[i]);
            selection.add(card);
        }
        plusbtn = plus[0];
        pages.add(selection, "Counts");
        forms.setLayout(new CardLayout());
        forms.setBackground(getBackground());
        pages.add(forms, "Details");
        JButton back = button("Back", new Color(240, 243, 245), Color.BLACK);
        back.setBounds(0, 590, 500, 60);
        back.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        back.addActionListener(
                e -> {
                    if (details && detailIndex > 0) showDetail(detailIndex - 1);
                    else if (details) showCounts();
                    else showPage("AvailableTrip");
                });
        add(back);
        next = button("Continue  >", new Color(225, 0, 45), Color.WHITE);
        next.setBounds(500, 590, 500, 60);
        next.setFont(new Font("Segoe UI", Font.BOLD, 16));
        next.addActionListener(
                e -> {
                    updatePassengerCounter();
                    if (getPassengerCount() == 0) {
                        qpal.components.AppDialogs.showMessageDialog(
                                this,
                                "Add at least one passenger. Seats must be available for the"
                                        + " selected trip.");
                        return;
                    }
                    if (!details) {
                        rebuildForms();
                        if (counts[0] == getPassengerCount()) {
                            showPage("SelectSeats");
                            return;
                        }
                        showDetail(0);
                    } else if (requiredForms().get(detailIndex).validateFields()) {
                        if (detailIndex + 1 < requiredForms().size()) showDetail(detailIndex + 1);
                        else if (validatePassengerDetails()) showPage("SelectSeats");
                    }
                });
        add(next);
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentShown(ComponentEvent e) {
                        updatePassengerCounter();
                    }
                });
        updatePassengerCounter();
    }

    private static class RoundedPanel extends JPanel {
        RoundedPanel() {
            super(null);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            g.setColor(new Color(224, 229, 236));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            g.dispose();
        }
    }

    private static JButton counterButton(String text, Color background, Color foreground) {
        JButton b =
                new JButton(text) {
                    @Override
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(isEnabled() ? getBackground() : new Color(244, 246, 248));
                        g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g.dispose();
                        super.paintComponent(graphics);
                    }
                };
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setBackground(background);
        b.setForeground(foreground);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton b = new JButton(text);
        b.setBackground(background);
        b.setForeground(foreground);
        b.setFont(new Font("Segoe UI", Font.BOLD, 18));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    private int passengerLimit() {
        TripCardPanel card = TripCardPanel.getSelectedCard();
        return card == null || card.getTrip() == null
                ? 0
                : Math.max(
                        0,
                        Math.min(
                                10,
                                Math.min(card.getTrip().available(), card.getTrip().capacity())));
    }

    private void updatePassengerCounter() {
        int limit = passengerLimit();
        boolean changed = false;
        for (int i = 3; i >= 0 && getPassengerCount() > limit; i--) {
            int remove = Math.min(counts[i], getPassengerCount() - limit);
            counts[i] -= remove;
            changed |= remove > 0;
        }
        for (int i = 0; i < 4; i++) {
            values[i].setText(Integer.toString(counts[i]));
            minus[i].setEnabled(counts[i] > 0);
            plus[i].setEnabled(getPassengerCount() < limit);
        }
        if (!details)
            subtitle.setText(
                    getPassengerCount()
                            + " selected / "
                            + limit
                            + " maximum · "
                            + (limit == 0
                                    ? "No seats available. Please select another trip."
                                    : getPassengerCount() == limit
                                            ? "Limit reached. Remove a passenger to change"
                                                  + " categories."
                                            : "Select passengers below. "
                                                    + (limit - getPassengerCount())
                                                    + " more can be added."));
        if (changed && details) {
            rebuildForms();
            showCounts();
        }
    }

    private void rebuildForms() {
        List<PassengerForm> previous = new ArrayList<>(passengers);
        passengers.clear();
        forms.removeAll();
        for (int t = 0; t < 4; t++) {
            final String type = TYPES[t];
            List<PassengerForm> same = previous.stream().filter(p -> p.type.equals(type)).toList();
            for (int i = 0; i < counts[t]; i++) {
                PassengerForm form = i < same.size() ? same.get(i) : new PassengerForm(type);
                if (i >= same.size()) form.id.addActionListener(e -> next.doClick());
                passengers.add(form);
                form.heading.setText(type + " passenger " + (i + 1) + " of " + counts[t]);
                if (!type.equals("Regular"))
                    forms.add(form, Integer.toString(forms.getComponentCount()));
            }
        }
        forms.revalidate();
        forms.repaint();
    }

    public boolean validatePassengerDetails() {
        if (passengers.size() != getPassengerCount()
                || passengers.isEmpty()
                || getPassengerCount() > passengerLimit()) return false;
        for (PassengerForm p : passengers) {
            if (!p.type.equals("Regular")) showDetail(requiredForms().indexOf(p));
            if (!p.validateFields()) return false;
        }
        return true;
    }

    private void showCounts() {
        details = false;
        pages.setBounds(70, 282, 860, 290);
        updatePassengerCounter();
        ((CardLayout) pages.getLayout()).show(pages, "Counts");
    }

    private void showPage(String page) {
        if (getParent() != null) ((CardLayout) getParent().getLayout()).show(getParent(), page);
    }

    public int getPassengerCount() {
        return java.util.Arrays.stream(counts).sum();
    }

    public List<String> getPassengerNames() {
        return java.util.stream.IntStream.rangeClosed(1, passengers.size())
                .mapToObj(i -> "Passenger " + i)
                .toList();
    }

    public List<String> getPassengerTypes() {
        return passengers.stream().map(p -> p.type).toList();
    }

    public void resetInputs() {
        java.util.Arrays.fill(counts, 0);
        counts[0] = 1;
        passengers.clear();
        forms.removeAll();
        showCounts();
        updatePassengerCounter();
        if (getParent() != null)
            for (Component c : getParent().getComponents())
                if (c instanceof SelectSeatsPanel seats) seats.resetInputs();
    }

    private static class PassengerForm extends RoundedPanel {
        final String type;
        final JLabel heading = new JLabel();
        final JTextField id = new JTextField();
        String manuallyCheckedPwdId = "";
        boolean staffAssistanceRequested;
        final JComboBox<String> disability =
                new JComboBox<>(
                        new String[] {
                            "Select disability type",
                            "Physical disability",
                            "Visual disability",
                            "Hearing disability",
                            "Speech and language disability",
                            "Intellectual disability",
                            "Learning disability",
                            "Psychosocial disability",
                            "Other"
                        });

        PassengerForm(String type) {
            this.type = type;
            setLayout(null);
            setBackground(Color.WHITE);

            int height = 184;
            setPreferredSize(new Dimension(850, height));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
            setMinimumSize(new Dimension(500, height));
            heading.setBounds(24, 14, 800, 28);
            heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
            heading.setForeground(new Color(225, 0, 45));
            add(heading);
            JLabel hint =
                    new JLabel(
                            type.equals("PWD")
                                    ? "Enter your PWD ID number and choose your disability type."
                                    : "Enter the ID number printed on your "
                                            + type.toLowerCase(java.util.Locale.ROOT)
                                            + " ID.");
            hint.setBounds(24, 48, 810, 25);
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            hint.setForeground(new Color(85, 94, 108));
            add(hint);
            if (!type.equals("Regular")) {
                field(type + " ID Number *", id, 24, 94, type.equals("PWD") ? 390 : 810);
                // PWD allows extra space for alternative formats checked by staff.
                final int limit = type.equals("Student") ? 15 : type.equals("Senior") ? 20 : 100;
                id.setDocument(
                        new javax.swing.text.PlainDocument() {
                            @Override
                            public void insertString(
                                    int offs, String str, javax.swing.text.AttributeSet a)
                                    throws javax.swing.text.BadLocationException {
                                if (str == null) return;
                                manuallyCheckedPwdId = "";
                                if (getLength() + str.length() <= limit) {
                                    super.insertString(offs, str, a);
                                } else {
                                    id.setText("");
                                    Toolkit.getDefaultToolkit().beep();
                                    qpal.components.AppDialogs.showMessageDialog(
                                            id,
                                            type
                                                    + " ID Number must not exceed "
                                                    + limit
                                                    + " characters.",
                                            "Warning!",
                                            JOptionPane.WARNING_MESSAGE);
                                }
                            }

                            @Override
                            public void remove(int offs, int length)
                                    throws javax.swing.text.BadLocationException {
                                manuallyCheckedPwdId = "";
                                super.remove(offs, length);
                            }
                        });

                if (type.equals("PWD")) field("Disability Type *", disability, 444, 94, 390);
            }
        }

        private void field(String title, JComponent input, int x, int y, int width) {
            JLabel label = new JLabel(title);
            label.setBounds(x, y, width, 20);
            label.setLabelFor(input);
            add(label);
            input.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            input.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(220, 225, 232)),
                            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
            if (input instanceof JComboBox<?> combo)
                qpal.components.FormInputStyle.styleCombo(combo);
            input.setBounds(x, y + 26, width, 44);
            input.getAccessibleContext().setAccessibleName(title);
            add(input);
        }

        boolean validateFields() {
            if (type.equals("Regular")) return true;

            String number = id.getText().trim();
            String message = "";
            if (!number.equals(manuallyCheckedPwdId)) manuallyCheckedPwdId = "";

            if (type.equals("Student")) {
                if (number.isEmpty()) {
                    message = "Student ID Number is required.";
                } else if (!number.matches("[0-9]{4}-[0-9]{5}-[A-Z]{2}-[0-9]")
                        && !number.matches("[0-9]{4}-[0-9]{7}")
                        && !number.matches("[0-9]{4}-[0-9]{6}")) {
                    message = "Invalid Student ID format";
                }
            } else if (type.equals("Senior") || type.equals("Senior Citizen")) {
                if (number.isEmpty()) {
                    message = "Senior Citizen ID Number is required.";
                } else if (!number.matches("[A-Za-z0-9-]{6,20}")
                        || !number.matches(".*[A-Za-z0-9].*")) {
                    message = "Please enter a valid Senior Citizen ID Number.";
                }
            } else if (type.equals("PWD")) {
                if (staffAssistanceRequested && number.equalsIgnoreCase("staff")) {
                    manuallyCheckedPwdId = number;
                } else if (number.isEmpty()
                        || (!number.matches("[0-9]{2}-[0-9]{4}-[0-9]{3}-[0-9]{7}")
                                && !number.equals(manuallyCheckedPwdId))) {
                    id.setBorder(
                            BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(new Color(225, 0, 45)),
                                    BorderFactory.createEmptyBorder(8, 14, 8, 14)));
                    qpal.components.AppDialogs.showMessageDialog(
                            this,
                            number.isEmpty()
                                    ? "PWD ID Number is required."
                                    : "Please check your PWD ID Number.",
                            "PWD ID Verification",
                            JOptionPane.ERROR_MESSAGE);
                    int answer =
                            qpal.components.AppDialogs.showConfirmDialog(
                                    this,
                                    "Would you like staff assistance to verify your PWD ID?",
                                    "PWD ID Verification",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
                    staffAssistanceRequested = answer == JOptionPane.YES_OPTION;
                    if (staffAssistanceRequested) {
                        qpal.components.AppDialogs.showMessageDialog(
                                this,
                                "Please ask a staff member to verify your PWD ID\n"
                                        + "before continuing.",
                                "PWD ID Verification",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    id.requestFocusInWindow();
                    id.selectAll();
                    return false;
                }
            }

            if (!message.isEmpty()) {
                id.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(225, 0, 45)),
                                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
                qpal.components.AppDialogs.showMessageDialog(
                        this, message, "Passenger Details", JOptionPane.ERROR_MESSAGE);
                id.requestFocusInWindow();
                return false;
            }
            id.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(220, 225, 232)),
                            BorderFactory.createEmptyBorder(8, 14, 8, 14)));

            // Keep the existing PWD disability field and its required validation.
            if (type.equals("PWD") && disability.getSelectedIndex() == 0) {
                qpal.components.AppDialogs.showMessageDialog(
                        this,
                        "Disability type is required.",
                        "Passenger Details",
                        JOptionPane.ERROR_MESSAGE);
                disability.requestFocusInWindow();
                return false;
            }
            // Matching the format does not establish authenticity or discount eligibility.
            return true;
        }
    }
}
