package qpal.view.Employee;

import java.awt.*;
import java.util.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.EmployeeStationDao;
import qpal.model.EmployeeStation;
import qpal.util.UiTask;

public final class EmployeeStationPanel extends JPanel {
    private final Map<EmployeeStation,JToggleButton> choices=new LinkedHashMap<>();
    private final ButtonGroup group=new ButtonGroup();
    private final JButton confirm=new JButton("Confirm");
    private final JLabel status=new JLabel("Checking station availability…",SwingConstants.CENTER);
    private EmployeeStation selected;
    private boolean loading, submitting;
    private final javax.swing.Timer timer;
    // Add your ImageIcons here later, just like the image JLabels in LoginPage.
    private final JLabel lblCounter1Image = new JLabel();
    private final JLabel lblCounter2Image = new JLabel();
    private final JLabel lblGate1Image = new JLabel();
    private final JLabel lblGate2Image = new JLabel();

    public EmployeeStationPanel(Consumer<EmployeeStation> confirmed) {
        setLayout(new GridBagLayout()); setBackground(new Color(245,245,245));
        setBorder(new EmptyBorder(28,25,28,25));
        JPanel content=new JPanel(new BorderLayout(0,28)); content.setOpaque(false);
        JPanel heading=new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading,BoxLayout.Y_AXIS));
        JLabel title=label("Select your assigned Station",28,true,new Color(228,0,70));
        JLabel subtitle=label("Choose the station you will be assigned to for this session.",13,false,new Color(120,120,120));
        heading.add(title); heading.add(Box.createVerticalStrut(4)); heading.add(subtitle);
        content.add(heading,BorderLayout.NORTH);
        JPanel cards=new JPanel(new GridLayout(1,4,16,0)); cards.setOpaque(false);
        for(String kind:new String[]{"Payment","Boarding"}) for(int number=1;number<=2;number++) {
            EmployeeStation station=new EmployeeStation(kind,number);
            StationCard card=new StationCard(); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
            JLabel icon=station.boarding() ? (number==1 ? lblGate1Image : lblGate2Image)
                    : (number==1 ? lblCounter1Image : lblCounter2Image);
            Dimension imageSize=new Dimension(90,76);
            icon.setPreferredSize(imageSize); icon.setMinimumSize(imageSize); icon.setMaximumSize(imageSize);
            icon.setHorizontalAlignment(SwingConstants.CENTER); icon.setVerticalAlignment(SwingConstants.CENTER);
            icon.setAlignmentX(CENTER_ALIGNMENT);
            // Example: lblCounter1Image.setIcon(new ImageIcon(new ImageIcon("resources/icons/counter1.png")
            //         .getImage().getScaledInstance(90,76,Image.SCALE_SMOOTH)));
            card.add(icon); card.add(Box.createVerticalStrut(16));
            card.add(label(station.title(),16,true,Color.BLACK)); card.add(Box.createVerticalStrut(5));
            card.add(label(station.description(),12,false,new Color(120,120,120))); card.add(Box.createVerticalStrut(22));
            JToggleButton select=new JToggleButton("Select") {
                @Override protected void paintComponent(Graphics graphics) {
                    Graphics2D g=(Graphics2D)graphics.create();
                    g.setColor(!isEnabled() ? new Color(245,245,245) : isSelected() ? new Color(228,0,70) : Color.WHITE);
                    g.fillRect(0,0,getWidth(),getHeight()); g.dispose(); super.paintComponent(graphics);
                }
            };
            select.setContentAreaFilled(false);
            select.setRolloverEnabled(true); select.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            select.setFont(new Font("SansSerif",Font.BOLD,12)); select.setForeground(new Color(228,0,70));
            select.setBackground(Color.WHITE); select.setFocusPainted(false);
            select.setBorder(BorderFactory.createLineBorder(new Color(228,0,70)));
            select.setAlignmentX(CENTER_ALIGNMENT); select.setMaximumSize(new Dimension(145,34));
            select.setPreferredSize(new Dimension(145,34)); select.setEnabled(false);
            select.getAccessibleContext().setAccessibleName("Select "+station.title());
            select.addActionListener(e -> {
                if(station.equals(selected)) { selected=null; group.clearSelection(); }
                else selected=station;
                updateSelection();
            });
            choices.put(station,select); group.add(select); card.add(select); cards.add(card);
            select.getModel().addChangeListener(e -> select.setForeground(select.isSelected() && select.isEnabled() ? Color.WHITE : new Color(228,0,70)));
            card.bind(select);
        }
        content.add(cards,BorderLayout.CENTER);
        JPanel footer=new JPanel(); footer.setOpaque(false); footer.setLayout(new BoxLayout(footer,BoxLayout.Y_AXIS));
        status.setFont(new Font("SansSerif",Font.PLAIN,12)); status.setForeground(new Color(110,110,110)); status.setAlignmentX(CENTER_ALIGNMENT);
        confirm.setBackground(new Color(228,0,70)); confirm.setForeground(Color.WHITE); confirm.setFocusPainted(false);
        confirm.setFont(new Font("SansSerif",Font.BOLD,14)); confirm.setBorderPainted(false);
        confirm.setPreferredSize(new Dimension(220,42)); confirm.setMaximumSize(new Dimension(220,42)); confirm.setAlignmentX(CENTER_ALIGNMENT);
        confirm.setEnabled(false); confirm.addActionListener(e -> { if(selected!=null) confirmed.accept(selected); });
        footer.add(status); footer.add(Box.createVerticalStrut(16)); footer.add(confirm); content.add(footer,BorderLayout.SOUTH);
        GridBagConstraints cell=new GridBagConstraints(); cell.weightx=1; cell.fill=GridBagConstraints.HORIZONTAL;
        add(content,cell);
        timer=new javax.swing.Timer(5000,e -> refresh());
        addHierarchyListener(e -> { if(isShowing()) { timer.start(); refresh(); } else timer.stop(); });
    }

    public void setSubmitting(boolean value) {
        submitting=value; choices.values().forEach(b -> b.setEnabled(false)); confirm.setEnabled(false);
        status.setText(value ? "Assigning your station…" : "Checking station availability…");
        if(!value) refresh();
    }
    public void showError(String message) { status.setText(message); }
    private void updateSelection() {
        choices.forEach((station,button) -> {
            if(button.isEnabled()) button.setText(station.equals(selected) ? "Selected" : "Select");
        });
        confirm.setEnabled(selected!=null && !submitting);
    }
    public void refresh() {
        if(loading || submitting) return; loading=true;
        UiTask.run(() -> new EmployeeStationDao().occupied(), occupied -> {
            loading=false; if(submitting) return;
            choices.forEach((station,button) -> { button.setEnabled(!occupied.contains(station)); button.setText(occupied.contains(station)?"In use":"Select"); });
            if(selected!=null && occupied.contains(selected)) { selected=null; group.clearSelection(); }
            status.setText(occupied.size()==4 ? "All stations are in use. Availability refreshes automatically." : "Select an available station to continue.");
            updateSelection();
        }, ex -> {
            loading=false; choices.values().forEach(b -> b.setEnabled(false)); confirm.setEnabled(false);
            status.setText("Unable to check stations. Retrying automatically…");
        });
    }
    private static JLabel label(String text,int size,boolean bold,Color color) {
        JLabel label=new JLabel(text); label.setFont(new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size));
        label.setForeground(color); label.setAlignmentX(CENTER_ALIGNMENT); return label;
    }

    /** Hover covers the card and every child, without changing its padding or button fill. */
    private static final class StationCard extends JPanel {
        private JToggleButton choice;
        private boolean hovered;

        StationCard() { setOpaque(false); setBorder(new EmptyBorder(16,16,16,16)); }

        void bind(JToggleButton button) {
            choice=button;
            button.getModel().addChangeListener(e -> repaint());
            button.addPropertyChangeListener("enabled",e -> repaint());
            java.awt.event.MouseAdapter hover=new java.awt.event.MouseAdapter() {
                private void update(java.awt.event.MouseEvent event) {
                    Point point=SwingUtilities.convertPoint(event.getComponent(),event.getPoint(),StationCard.this);
                    hovered=contains(point); repaint();
                }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { update(e); }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { update(e); }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    // The button already handles its own click; forward card/label clicks only.
                    if(e.getComponent()!=choice && SwingUtilities.isLeftMouseButton(e) && choice.isEnabled())
                        choice.doClick(0);
                }
            };
            trackHover(this,hover);
        }

        private void trackHover(Component component,java.awt.event.MouseAdapter listener) {
            component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            component.addMouseListener(listener);
            if(component instanceof Container container)
                for(Component child:container.getComponents()) trackHover(child,listener);
        }

        @Override public void removeNotify() { hovered=false; super.removeNotify(); }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g=(Graphics2D)graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            boolean highlighted=choice!=null && choice.isEnabled() && (hovered || choice.isSelected());
            g.setColor(Color.WHITE); g.fillRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);
            g.setColor(highlighted ? new Color(228,0,70) : new Color(232,236,242));
            g.setStroke(new BasicStroke(highlighted ? 2f : 1f));
            g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14); g.dispose();
        }
    }
}
