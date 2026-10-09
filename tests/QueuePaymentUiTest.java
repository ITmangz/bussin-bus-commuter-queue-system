import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.lang.reflect.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import qpal.dao.QueuePaymentDao.Progress;
import qpal.model.BookingData.*;
import qpal.view.Admin.QueuePaymentDialog;

public class QueuePaymentUiTest {
    static Object field(Object target,String name) throws Exception {
        Field f=target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target);
    }
    static void check(boolean condition,String message) { if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                var constructor=QueuePaymentDialog.class.getDeclaredConstructor(Component.class,QueueRow.class,int.class,Receipt.class,Progress.class);
                constructor.setAccessible(true);
                var row=new QueueRow(1,1,12,"Manila - Baguio","BUS-001","2026-10-02 12:00","Passenger","Pending","Serving",10);
                var receipt=new Receipt(1,"TEST-RECEIPT",LocalDate.of(2026,10,1),12,"BUS-001","Manila - Baguio","2026-10-02 12:00",
                        "A1, A2, A3, A4, A5, B1, B2, B3, B4, B5",new BigDecimal("500.00"),"Cash","Pending");
                for (int state=0;state<3;state++) {
                    Receipt r=state==0 ? receipt : new Receipt(receipt.bookingId(),receipt.reference(),receipt.queueDate(),receipt.queueNumber(),receipt.bus(),receipt.route(),receipt.schedule(),receipt.seats(),receipt.total(),receipt.method(),"Paid");
                    JDialog dialog=(JDialog)constructor.newInstance(new JPanel(),row,1,r,new Progress(state==0 ? null : new BigDecimal("1000.00"),state==2,false));
                    try {
                        check(((JButton)field(dialog,"pay")).isEnabled()==(state==0),"Payment cannot be repeated");
                        if (state==0) {
                            JTextField input=(JTextField)field(dialog,"received");
                            input.setText("10,000");
                            check(input.getText().equals("10,000"),"Accept six-character maximum");
                            check(((JLabel)field(dialog,"change")).getText().equals("Change: PHP 9500.00"),"Comma-formatted change");
                            for (String invalid : new String[]{"10,001","10001","100000","9999.99"}) {
                                input.selectAll(); input.replaceSelection(invalid);
                                check(input.getText().equals("10,000"),"Reject over-limit input: "+invalid);
                            }
                            input.setText("999.99");
                            check(input.getText().equals("999.99"),"Accept cents within six characters");
                            input.setText("1000");
                        }
                        check(((JLabel)field(dialog,"change")).getText().equals("Change: PHP 500.00"),"Live change");
                        dialog.validate();
                        var pane=dialog.getContentPane();
                        BufferedImage image=new BufferedImage(pane.getWidth(),pane.getHeight(),BufferedImage.TYPE_INT_RGB);
                        Graphics2D g=image.createGraphics(); pane.printAll(g); g.dispose();
                        ImageIO.write(image,"png",new java.io.File("build/payment-preview-"+state+".png"));
                    } finally { dialog.dispose(); }
                }
                for (String method : new String[]{"GCash","Card"}) {
                    var cashless=new Receipt(1,"CASHLESS",receipt.queueDate(),12,receipt.bus(),receipt.route(),receipt.schedule(),receipt.seats(),receipt.total(),method,"Pending");
                    JDialog dialog=(JDialog)constructor.newInstance(new JPanel(),row,1,cashless,new Progress(null,false,false));
                    try {
                        JTextField amount=(JTextField)field(dialog,"received");
                        check(new BigDecimal(amount.getText()).compareTo(receipt.total())==0 && !amount.isEditable(),"Cashless amount preset and locked");
                        JButton pay=(JButton)field(dialog,"pay");check(!pay.isEnabled(),"Verification required");
                        ((JCheckBox)field(dialog,"verified")).doClick();check(pay.isEnabled(),"Verified payment enabled");
                    } finally {dialog.dispose();}
                }
                System.out.println("PASS: payment dialog states, change, previews and cashless verification");
            } catch(Exception ex) { throw new RuntimeException(ex); }
        });
    }
}
