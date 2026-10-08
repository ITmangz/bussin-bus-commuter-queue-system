import java.awt.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import javax.swing.*;
import qpal.model.BookingData.TripOption;
import qpal.view.Commuter.*;
public class PassengerTypesTest {
    static Object field(Object target,String name) throws Exception { Field f=target.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(target); }
    static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    static void render(JPanel panel,String file) throws Exception {
        panel.setSize(1000,650); layout(panel);
        var image=new java.awt.image.BufferedImage(1000,650,java.awt.image.BufferedImage.TYPE_INT_RGB);
        var g=image.createGraphics(); panel.printAll(g); g.dispose(); javax.imageio.ImageIO.write(image,"png",new java.io.File(file));
    }
    static void layout(Container panel) { panel.doLayout(); for(Component c:panel.getComponents()) if(c instanceof Container child) layout(child); }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                TripCardPanel card=new TripCardPanel(new TripOption(1,"Bus","PITX","Tagaytay",LocalDate.now(),LocalTime.NOON,BigDecimal.TEN,40,8));
                card.getActionMap().get("selectTrip").actionPerformed(null);
                PassengerDetailsPanel panel=new PassengerDetailsPanel(null);
                JButton[] plus=(JButton[])field(panel,"plus");
                for(int i=1;i<4;i++) plus[i].doClick(0);
                check(panel.getPassengerCount()==4,"Mixed counts");
                render(panel,"build/passenger-counts.png");
                Method rebuild=PassengerDetailsPanel.class.getDeclaredMethod("rebuildForms"); rebuild.setAccessible(true); rebuild.invoke(panel);
                JPanel pages=(JPanel)field(panel,"pages"); ((CardLayout)pages.getLayout()).show(pages,"Details");
                check(((JPanel)field(panel,"forms")).getComponentCount()==3,"Only three non-regular forms are visible");
                check(!panel.validatePassengerDetails(),"Empty forms blocked");
                List<?> forms=(List<?>)field(panel,"passengers");
                for(int i=0;i<forms.size();i++) {
                    Object form=forms.get(i);
                    ((JTextField)field(form,"id")).setText("TEST-"+i);
                }
                check(!panel.validatePassengerDetails(),"PWD type required");
                ((JComboBox<?>)field(forms.get(3),"disability")).setSelectedIndex(1);
                check(panel.validatePassengerDetails(),"Complete mixed booking without names");
                check(panel.getPassengerNames().equals(List.of("Passenger 1","Passenger 2","Passenger 3","Passenger 4")),"Automatic booking labels");
                check(panel.getPassengerTypes().equals(List.of("Regular","Student","Senior","PWD")),"Ordered types for payment");
                ((JTextField)field(forms.get(1),"id")).setText("  ");
                check(!panel.validatePassengerDetails(),"Blank student ID blocked");
                render(panel,"build/passenger-validation.png");
                ((JTextField)field(forms.get(1),"id")).setText("STUDENT-123");
                rebuild.invoke(panel);
                check(panel.validatePassengerDetails(),"Back retains forms");
                Method show=PassengerDetailsPanel.class.getDeclaredMethod("showDetail",int.class); show.setAccessible(true); show.invoke(panel,0);
                JButton next=null,back=null;
                for(Component component:panel.getComponents()) if(component instanceof JButton b) { if(b.getText().startsWith("Continue")) next=b; if(b.getText().equals("Back")) back=b; }
                next.doClick(0); check((int)field(panel,"detailIndex")==1,"Continue advances one passenger");
                back.doClick(0); check((int)field(panel,"detailIndex")==0,"Back returns one passenger");
                ((JTextField)field(forms.get(1),"id")).setText(""); next.doClick(0);
                check((int)field(panel,"detailIndex")==0,"Incomplete current step blocks navigation");
                panel.resetInputs(); rebuild.invoke(panel); check(panel.validatePassengerDetails(),"Regular needs no details");
                panel.resetInputs(); check(panel.getPassengerCount()==1 && panel.getPassengerNames().isEmpty(),"Reset clears personal details");
                System.out.println("PASS: mixed counts, required details, payment type order, preservation and reset");
            } catch(Exception e) { throw new RuntimeException(e); }
        });
    }
}




