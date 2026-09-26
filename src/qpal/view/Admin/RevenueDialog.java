package qpal.view.Admin;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import qpal.dao.PaymentDao;
import qpal.dao.PaymentDao.*;
import qpal.dao.PaymentDao.Choice;
import qpal.util.UiTask;

public final class RevenueDialog {
    private RevenueDialog() {}
    public static void show(AdminRevenuePanel parent, Payment original, List<Choice> choices) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),original==null ? "Add Revenue" : "Edit Revenue",Dialog.ModalityType.APPLICATION_MODAL);
        JPanel form = new JPanel(); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JLabel heading = new JLabel(original==null ? "Add Payment" : "Edit Payment #" + original.id());
        heading.setFont(new Font("SansSerif",Font.BOLD,24)); form.add(heading); form.add(Box.createVerticalStrut(18));
        JComboBox<Choice> trip = new JComboBox<>();
        if (original!=null) trip.addItem(new Choice(original.tripId(),original.bookingId(),"Trip #" + original.tripId()
                + (original.bookingId()==null ? "" : " | Booking #" + original.bookingId()),original.amount()));
        else { trip.addItem(null); choices.forEach(trip::addItem); }
        trip.setEnabled(original==null);
        JTextField commuter = new JTextField(original==null ? "Passenger 1" : original.commuter());
        JTextField amount = new JTextField(original==null ? "" : original.amount().toPlainString());
        JComboBox<String> method = new JComboBox<>(new String[]{"Cash","GCash","Card"});
        JComboBox<String> status = new JComboBox<>(new String[]{"Pending","Paid","Cancelled"});
        if (original!=null) { method.setSelectedItem(original.method()); status.setSelectedItem(original.status()); amount.setEditable(original.bookingId()==null); }
        trip.addActionListener(e -> { Choice chosen=(Choice)trip.getSelectedItem();
            if (chosen!=null) { amount.setText(chosen.amount().toPlainString()); amount.setEditable(chosen.bookingId()==null); } });
        field(form,"Trip / booking",trip); field(form,"Commuter label",commuter); field(form,"Amount (PHP)",amount);
        field(form,"Payment method",method); field(form,"Payment status",status);
        JLabel note = new JLabel("<html>Choose Paid only after collecting payment.<br>Unpaying a completed booking returns its queue to Waiting.</html>");
        form.add(note); form.add(Box.createVerticalStrut(16));
        JPanel buttons = new JPanel(new GridLayout(1,2,12,0));
        JButton save = new JButton("Save"), cancel = new JButton("Cancel");
        save.setBackground(new Color(34,197,94)); save.setForeground(Color.WHITE);
        buttons.add(save); buttons.add(cancel); form.add(buttons);
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            BigDecimal value;
            try { value = new BigDecimal(amount.getText().trim()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dialog,"Enter a valid amount."); return; }
            Choice choice = (Choice)trip.getSelectedItem();
            String label = commuter.getText(), paymentMethod = (String)method.getSelectedItem(), paymentStatus = (String)status.getSelectedItem();
            save.setEnabled(false); cancel.setEnabled(false); dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            UiTask.run(() -> { new PaymentDao().save(original,choice,label,value,paymentMethod,paymentStatus); return true; }, result -> {
                dialog.dispose(); parent.refreshData();
            }, ex -> { save.setEnabled(true); cancel.setEnabled(true); dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                JOptionPane.showMessageDialog(dialog,ex.getMessage(),"Unable to Save",JOptionPane.WARNING_MESSAGE); });
        });
        dialog.setContentPane(AdminFormStyle.frame(form)); dialog.pack(); dialog.setResizable(false); dialog.setLocationRelativeTo(parent); dialog.setVisible(true);
    }
    private static void field(JPanel form,String label,JComponent input) {
        JLabel title = new JLabel(label); title.setAlignmentX(Component.LEFT_ALIGNMENT); form.add(title); form.add(Box.createVerticalStrut(5));
        input.setAlignmentX(Component.LEFT_ALIGNMENT); input.setMaximumSize(new Dimension(530,36)); input.setPreferredSize(new Dimension(530,36));
        form.add(input); form.add(Box.createVerticalStrut(12));
    }
}
