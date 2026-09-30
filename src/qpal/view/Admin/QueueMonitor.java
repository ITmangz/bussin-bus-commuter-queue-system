package qpal.view.Admin;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.QueueDao;
import qpal.model.BookingData.QueueRow;

/** Independent, read-only commuter display with its own refresh lifecycle. */
public final class QueueMonitor extends JPanel {
    private static final Color RED=new Color(210,0,49), INK=new Color(31,43,62), MUTED=new Color(105,118,139);
    private final boolean boarding;
    private final JPanel cards=new JPanel(new GridLayout(1,2,24,0));
    private final JPanel upcoming=new AdminCard(24);
    private final JLabel status=text("Connecting to live queue…",12,MUTED);
    private final javax.swing.Timer timer;
    private boolean loading;
    public static void open(Component owner,boolean boarding) {
        JFrame frame=new JFrame(boarding ? "Boarding Queue Monitor" : "Payment Queue Monitor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new QueueMonitor(boarding));
        frame.setSize(1020,680); frame.setMinimumSize(new Dimension(780,560));
        frame.setLocationRelativeTo(owner); frame.setVisible(true);
    }
    public QueueMonitor(boolean boarding) {
        this.boarding=boarding;
        setLayout(new BorderLayout(0,24)); setBackground(new Color(246,247,251));
        setBorder(new EmptyBorder(28,32,20,32));
        JPanel heading=new JPanel(new BorderLayout()); heading.setOpaque(false);
        heading.add(text(boarding ? "Boarding Queue" : "Payment Queue",34,RED),BorderLayout.WEST);
        heading.add(text("COMMUTER LIVE DISPLAY",12,MUTED),BorderLayout.EAST);
        add(heading,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(0,22)); body.setOpaque(false); cards.setOpaque(false);
        body.add(cards,BorderLayout.CENTER); upcoming.setLayout(new BorderLayout(0,16));
        upcoming.setPreferredSize(new Dimension(0,135)); body.add(upcoming,BorderLayout.SOUTH);
        add(body,BorderLayout.CENTER); add(status,BorderLayout.SOUTH);
        render(java.util.List.of(),Map.of(),Map.of());
        timer=new javax.swing.Timer(3000,e -> refresh());
        addHierarchyListener(e -> { if(isShowing()) {timer.start(); refresh();} else timer.stop(); });
    }
    private void refresh() {
        if(loading) return; loading=true;
        qpal.util.UiTask.run(() -> {
            QueueDao dao=new QueueDao();
            var rows=boarding ? dao.boarding() : dao.today();
            Map<Integer,Integer> trips=new HashMap<>();
            if(boarding) try(var c=qpal.util.DbConnection.getConnection(); var p=c.prepareStatement("SELECT booking_id,trip_id FROM bookings"); var r=p.executeQuery()) {
                while(r.next()) trips.put(r.getInt(1),r.getInt(2));
            }
            return new Snapshot(rows,dao.stations(boarding ? "Boarding" : "Payment"),trips);
        }, data -> { loading=false; render(data.rows(),data.stations(),data.trips());
            status.setText("LIVE  •  Updated "+java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("h:mm:ss a"))+"  •  Please wait for your queue number");
        }, ex -> {loading=false; status.setText("Connection unavailable • Display may be out of date • Retrying…");});
    }
    private record Snapshot(java.util.List<QueueRow> rows,Map<Integer,Integer> stations,Map<Integer,Integer> trips) {}
    private void render(java.util.List<QueueRow> rows,Map<Integer,Integer> stations,Map<Integer,Integer> trips) {
        cards.removeAll();
        for(int station=1;station<=2;station++) {
            Integer id=stations.get(station);
            QueueRow row=rows.stream().filter(r -> Objects.equals(id,r.id()) && (boarding || r.status().equals("Serving"))).findFirst().orElse(null);
            JPanel card=new AdminCard(24); card.setLayout(new BorderLayout(0,20));
            JLabel title=text((boarding ? "GATE " : "COUNTER ")+station,22,Color.WHITE);
            title.setOpaque(true); title.setBackground(RED); title.setBorder(new EmptyBorder(14,18,14,18)); card.add(title,BorderLayout.NORTH);
            JPanel content=new JPanel(new GridLayout(boarding ? 5 : 2,1,0,8)); content.setOpaque(false);
            if(boarding) {
                content.add(text(row==null ? "Awaiting next trip" : String.format("T%03d",trips.getOrDefault(row.bookingId(),0)),32,INK));
                content.add(text(row==null ? "No active boarding call" : row.route(),20,INK));
                content.add(text(row==null ? "—" : row.bus()+"  •  "+row.schedule(),14,MUTED));
            }
            JLabel number=text(row==null ? "—" : String.format(boarding ? "B-%03d" : "P%03d",row.number()),boarding ? 40 : 68,RED);
            number.setHorizontalAlignment(SwingConstants.CENTER); content.add(number);
            JLabel state=text(row==null ? "Available" : boarding ? row.passengers()+" passengers • BOARDING" : "Now Serving",20,row==null ? MUTED : RED);
            state.setHorizontalAlignment(SwingConstants.CENTER); content.add(state); card.add(content,BorderLayout.CENTER); cards.add(card);
        }
        upcoming.removeAll(); upcoming.add(text(boarding ? "Next to Board" : "Next in Queue",20,RED),BorderLayout.NORTH);
        var next=rows.stream().filter(r -> boarding ? !stations.containsValue(r.id()) : r.status().equals("Waiting")).limit(boarding ? 1 : 5).toList();
        JPanel line=new JPanel(new GridLayout(1,Math.max(1,next.size()),12,0)); line.setOpaque(false);
        if(next.isEmpty()) line.add(text("No waiting queues",20,MUTED));
        for(var row:next) line.add(text(boarding ? String.format("T%03d • %s • %s • %s • B-%03d",trips.getOrDefault(row.bookingId(),0),row.route(),row.schedule(),row.bus(),row.number()) : String.format("P%03d",row.number()),boarding ? 16 : 34,RED));
        upcoming.add(line,BorderLayout.CENTER); revalidate(); repaint();
    }
    private static JLabel text(String value,int size,Color color) {
        JLabel label=new JLabel(value); label.setFont(new Font("Segoe UI",Font.BOLD,size)); label.setForeground(color); return label;
    }
}
