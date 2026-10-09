package qpal.view.Admin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import qpal.dao.BookingDao;
import qpal.dao.QueueBookingDao;
import qpal.model.BookingData.*;
import qpal.util.UiTask;

final class QueueBookingDialog {
    private QueueBookingDialog() {}

    static void show(AdminQueuePanel parent, QueueRow existing, List<TripOption> trips,
            List<QueueBookingDao.Person> people) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                existing == null ? "Add Queue Booking" : "Edit Queue Booking", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        JPanel form = new JPanel(new BorderLayout(0,16));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(25,30,25,30));
        JPanel top = new JPanel(); top.setOpaque(false); top.setLayout(new BoxLayout(top,BoxLayout.Y_AXIS));
        JLabel title = new JLabel(existing==null ? "Add Queue Booking" : "Edit Queue P"+String.format("%03d",existing.number()));
        title.setFont(new Font("Segoe UI",Font.BOLD,22)); title.setForeground(new Color(225,29,72));
        top.add(title); top.add(Box.createVerticalStrut(14));
        JComboBox<TripOption> trip = new JComboBox<>(trips.toArray(new TripOption[0]));
        trip.setSelectedIndex(-1);
        trip.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focus) {
                super.getListCellRendererComponent(list,value,index,selected,focus);
                if (value instanceof TripOption t) setText(t.bus()+" | "+t.route()+" | "+t.schedule());
                return this;
            }
        });
        JComboBox<String> method = new JComboBox<>(new String[]{"Cash","GCash","Card"});
        if (existing==null) {
            top.add(new JLabel("Trip / Schedule")); top.add(trip); top.add(Box.createVerticalStrut(10));
            top.add(new JLabel("Payment Method — collect at the counter")); top.add(method);
        } else {
            top.add(new JLabel(existing.route()+" | "+existing.bus()));
            top.add(new JLabel("Edit passenger names. Category, queue position, seats and fare stay the same."));
        }
        form.add(top,BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Passenger Name","Type","Seat"},0) {
            @Override public boolean isCellEditable(int row,int col) { return existing==null || col==0; }
        };
        if (existing==null) model.addRow(new Object[]{"","Regular",null});
        else for (var person:people) model.addRow(new Object[]{person.name(),person.type(),"Unchanged"});
        JTable table = new JTable(model); table.setRowHeight(34);
        table.putClientProperty("terminateEditOnFocusLost",Boolean.TRUE);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(
                new JComboBox<>(new String[]{"Regular","Student","Senior","PWD"})));
        JComboBox<String> seats = new JComboBox<>();
        if (existing==null) table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(seats));
        JScrollPane scroll = new JScrollPane(table); scroll.setPreferredSize(new Dimension(620,230));
        AdminCard.styleScrollBar(scroll,Color.WHITE);
        JPanel middle = new JPanel(new BorderLayout(0,10)); middle.setOpaque(false); middle.add(scroll);
        JLabel note = new JLabel(existing==null ? "Choose a trip to load available seats." : "All passengers keep their reserved seats.");
        JPanel lower = new JPanel(new BorderLayout()); lower.setOpaque(false); lower.add(note,BorderLayout.NORTH);
        if (existing==null) {
            JPanel rowButtons = new JPanel(new FlowLayout(FlowLayout.LEFT)); rowButtons.setOpaque(false);
            JButton add = new JButton("Add Passenger"), remove = new JButton("Remove Passenger");
            add.addActionListener(e -> { if (table.isEditing()) table.getCellEditor().stopCellEditing();
                if (model.getRowCount()<10) model.addRow(new Object[]{"","Regular",null}); });
            remove.addActionListener(e -> { if (table.isEditing()) table.getCellEditor().stopCellEditing();
                if (table.getSelectedRow()>=0 && model.getRowCount()>1) model.removeRow(table.getSelectedRow()); });
            rowButtons.add(add); rowButtons.add(remove); lower.add(rowButtons,BorderLayout.SOUTH);
        }
        middle.add(lower,BorderLayout.SOUTH); form.add(middle);
        JPanel buttons = new JPanel(new GridLayout(1,2,12,0)); buttons.setOpaque(false);
        JButton save = new JButton(existing==null ? "Add Booking" : "Save Changes"), cancel=new JButton("Cancel");
        for (JButton b:new JButton[]{save,cancel}) {
            b.setPreferredSize(new Dimension(200,42)); b.setFont(new Font("Segoe UI",Font.BOLD,14));
            b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false);
        }
        save.setBackground(new Color(0,170,100)); cancel.setBackground(new Color(225,29,72));
        buttons.add(save); buttons.add(cancel); form.add(buttons,BorderLayout.SOUTH);
        cancel.addActionListener(e -> dialog.dispose());
        save.setEnabled(existing!=null);
        trip.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().cancelCellEditing();
            seats.removeAllItems(); save.setEnabled(false);
            for (int i=0;i<model.getRowCount();i++) model.setValueAt(null,i,2);
            TripOption selected=(TripOption)trip.getSelectedItem();
            if (selected==null) return;
            note.setText("Loading available seats...");
            UiTask.run(() -> new BookingDao().occupiedSeats(selected.id()), occupied -> {
                if (trip.getSelectedItem()!=selected) return;
                for (int seat=1;seat<=selected.capacity();seat++) if (!occupied.contains(seat)) seats.addItem(BookingDao.seatLabel(seat));
                note.setText("Select a different seat for each passenger. Maximum 10 passengers.");
                save.setEnabled(seats.getItemCount()>0);
            }, ex -> { if (trip.getSelectedItem()==selected) note.setText("Unable to load seats. Choose the trip again."); });
        });
        String reference="BK-"+java.util.UUID.randomUUID().toString().replace("-","").substring(0,26);
        save.addActionListener(e -> {
            if (table.isEditing() && !table.getCellEditor().stopCellEditing()) return;
            List<Passenger> passengers=new ArrayList<>();
            List<QueueBookingDao.Person> edited=new ArrayList<>();
            try {
                for (int i=0;i<model.getRowCount();i++) {
                    String name=String.valueOf(model.getValueAt(i,0)).trim(), type=String.valueOf(model.getValueAt(i,1));
                    if (name.isEmpty() || name.length()>100) throw new IllegalArgumentException("Enter passenger names of 1–100 characters.");
                    if (existing==null) {
                        if (model.getValueAt(i,2)==null) throw new IllegalArgumentException("Select a seat for every passenger.");
                        passengers.add(new Passenger(name,type,BookingDao.seatNumber(model.getValueAt(i,2).toString())));
                    } else edited.add(new QueueBookingDao.Person(people.get(i).id(),name,type));
                }
            } catch (IllegalArgumentException ex) { qpal.components.AppDialogs.showMessageDialog(dialog,ex.getMessage(),"Check Details",JOptionPane.WARNING_MESSAGE); return; }
            TripOption selected=(TripOption)trip.getSelectedItem();
            String payment=(String)method.getSelectedItem();
            save.setEnabled(false); cancel.setEnabled(false); trip.setEnabled(false);
            UiTask.run(() -> {
                if (existing==null) return new BookingDao().book(reference,selected,passengers,payment);
                new QueueBookingDao().edit(existing.bookingId(),people,edited); return null;
            }, receipt -> {
                dialog.dispose(); parent.refreshData();
                if (receipt!=null) qpal.components.AppDialogs.showMessageDialog(parent,"Booking added. Queue "+String.format("P%03d",receipt.queueNumber()));
            }, ex -> { save.setEnabled(true); cancel.setEnabled(true); trip.setEnabled(true);
                qpal.components.AppDialogs.showMessageDialog(dialog,ex.getMessage(),"Unable to Save",JOptionPane.WARNING_MESSAGE); });
        });
        dialog.setContentPane(AdminFormStyle.frame(form)); dialog.pack(); dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent); dialog.setVisible(true);
    }

    static boolean confirmDelete(AdminQueuePanel parent,QueueRow row) {
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(parent),"Delete Queue Booking",Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        JPanel form=new JPanel(new BorderLayout(0,18)); form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(25,30,25,30));
        JLabel title=new JLabel("Delete Queue P"+String.format("%03d",row.number())+"?");
        title.setFont(new Font("Segoe UI",Font.BOLD,22)); title.setForeground(new Color(225,29,72));
        form.add(title,BorderLayout.NORTH);
        form.add(new JLabel("<html>This cancels the booking and releases its reserved seats.<br>"
                +"The cancelled record stays in history; queue numbers stay unchanged.<br>"
                +"Any payment already collected is retained and is not refunded here.</html>"),BorderLayout.CENTER);
        boolean[] confirmed={false};
        JPanel buttons=new JPanel(new GridLayout(1,2,12,0)); buttons.setOpaque(false);
        JButton delete=new JButton("Delete Booking"),cancel=new JButton("Cancel");
        delete.setBackground(new Color(225,29,72)); delete.setForeground(Color.WHITE);
        delete.addActionListener(e->{confirmed[0]=true;dialog.dispose();}); cancel.addActionListener(e->dialog.dispose());
        buttons.add(delete);buttons.add(cancel);form.add(buttons,BorderLayout.SOUTH);
        dialog.setContentPane(AdminFormStyle.frame(form));dialog.pack();dialog.setLocationRelativeTo(parent);dialog.setVisible(true);
        return confirmed[0];
    }
}