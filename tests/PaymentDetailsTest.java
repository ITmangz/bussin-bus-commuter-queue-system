import java.awt.*;
import java.lang.reflect.*;
import javax.swing.*;
import qpal.view.Commuter.PaymentDetailsPanel;
public class PaymentDetailsTest {
    static class TestPanel extends PaymentDetailsPanel {
        String error;
        TestPanel(boolean wallet) { super(wallet); }
        @Override protected void showValidationError(String message) { error = message; }
    }
    static Object field(Object o,String n) throws Exception {Field f=PaymentDetailsPanel.class.isInstance(o) ? PaymentDetailsPanel.class.getDeclaredField(n) : o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    static void check(boolean ok) {if(!ok) throw new AssertionError();}
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                for(boolean wallet:new boolean[]{true,false}) {
                    TestPanel p=new TestPanel(wallet);
                    check(!p.validateInputs());
                    check(p.error.equals(wallet ? "Please select an e-wallet provider." : "Please select a card type."));
                    ((JComboBox<?>)field(p,"providerbox")).setSelectedIndex(1);
                    ((JTextField)field(p,"numberfield")).setText(wallet?"09123456789":"1234");
                    ((JTextField)field(p,"referencefield")).setText("TESTREF123");
                    check(p.validateInputs()); check(p.getPaymentImageLabel().getIcon()==null);
                    p.setSize(860,290); p.doLayout();
                    var img=new java.awt.image.BufferedImage(860,290,java.awt.image.BufferedImage.TYPE_INT_RGB);
                    var g=img.createGraphics();p.printAll(g);g.dispose();
                    javax.imageio.ImageIO.write(img,"png",new java.io.File("build/"+(wallet?"ewallet":"card")+"-details.png"));
                    JTextField number=(JTextField)field(p,"numberfield");
                    JTextField reference=(JTextField)field(p,"referencefield");
                    String validNumber=number.getText();
                    for(String invalid:new String[]{"1234567890123456","12a","12 3","12-3","+639123456789","１２３４"}) {
                        number.setText(invalid); check(number.getText().equals(validNumber));
                    }
                    number.setText("123"); check(!p.validateInputs());
                    check(p.error.equals(wallet ? "Mobile number must contain 11 digits." : "Card number must contain exactly 4 digits."));
                    if(wallet) { number.setText("08123456789"); check(!p.validateInputs()); }
                    number.setText(validNumber);
                    reference.setText("ABCDEFGHIJKLMNOP1234"); check(p.validateInputs());
                    for(String invalid:new String[]{"ABCDEFGHIJKLMNOP12345","TEST-REF","TEST REF","REF_123"}) {
                        reference.setText(invalid); check(reference.getText().equals("ABCDEFGHIJKLMNOP1234"));
                    }
                    reference.select(0,4); reference.replaceSelection("1234");
                    check(reference.getText().equals("1234EFGHIJKLMNOP1234"));
                    reference.getDocument().insertString(20,"X",null); check(reference.getText().length()==20);
                    reference.setText(""); check(!p.validateInputs());
                    check(p.error.equals("Please enter a reference number."));
                    p.resetInputs();check(!p.validateInputs());
                }
                var screen=new qpal.view.Commuter.PaymentPanel(null,null);
                Field method=screen.getClass().getDeclaredField("selectedPayment"); method.setAccessible(true);
                Method show=screen.getClass().getDeclaredMethod("showPaymentDetails"); show.setAccessible(true);
                Method back=screen.getClass().getDeclaredMethod("showPaymentChoices"); back.setAccessible(true);
                for(String type:new String[]{"E-Wallet","Card"}) {
                    method.set(screen,type); show.invoke(screen);
                    check(((JPanel)field(screen,type.equals("Card")?"cardDetails":"ewalletDetails")).isVisible());
                    check(!((JPanel)field(screen,"cashCard")).isVisible());
                    back.invoke(screen); check(((JPanel)field(screen,"cashCard")).isVisible());
                }
                screen.resetInputs(); check(!(boolean)field(screen,"enteringDetails"));
                System.out.println("PASS: wallet/card required fields, number validation, blank image labels and reset");
            } catch(Exception e) {throw new RuntimeException(e);}
        });
        System.exit(0);
    }
}



