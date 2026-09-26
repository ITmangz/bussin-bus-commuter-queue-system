package qpal.components;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class AppDialogsTest {
 static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
 static void layout(Container c) { c.doLayout(); for(Component child:c.getComponents()) if(child instanceof Container p) layout(p); }
 static JButton find(Container c,String text) {
  for(Component child:c.getComponents()) { if(child instanceof JButton b && b.getText().equals(text)) return b;
   if(child instanceof Container p) { JButton b=find(p,text); if(b!=null)return b; } } return null;
 }
 static JTextPane messageBody(Container c) {
  for(Component child:c.getComponents()) {
   if(child instanceof JTextPane text)return text;
   if(child instanceof Container nested) { JTextPane text=messageBody(nested);if(text!=null)return text; }
  } return null;
 }
 public static void main(String[] args) throws Exception {
  SwingUtilities.invokeAndWait(() -> {
   try {
    String[] titles={"Error","Check Details","Success","Confirmation"};
    int[] types={JOptionPane.ERROR_MESSAGE,JOptionPane.WARNING_MESSAGE,JOptionPane.INFORMATION_MESSAGE,JOptionPane.QUESTION_MESSAGE};
    java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(1000,600,2);
    Graphics2D graphics=image.createGraphics();graphics.setColor(new Color(145,150,160));graphics.fillRect(0,0,1000,600);
    for(int i=0;i<4;i++) {
     int[] result={-99};
     JPanel card=AppDialogs.buildCard(i==3?"Are you sure you want to delete this account?":"There was an error processing your request.",titles[i],types[i],i==3?JOptionPane.YES_NO_OPTION:JOptionPane.DEFAULT_OPTION,v->result[0]=v);
     card.setSize(card.getPreferredSize());layout(card);
     JButton button=find(card,i==3?"No":"OK");button.doClick();
     check(result[0]==(i==3?JOptionPane.NO_OPTION:JOptionPane.OK_OPTION),"Return value");
     Graphics2D g=(Graphics2D)graphics.create();g.translate(10+(i%2)*495,20+(i/2)*285);card.printAll(g);g.dispose();
    }
    graphics.dispose();javax.imageio.ImageIO.write(image,"png",new java.io.File("build/dialog-preview.png"));
    JPanel longCard=AppDialogs.buildCard("Passenger details\n".repeat(100),"Booking Details",JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION,v->{});
    check(longCard.getPreferredSize().height<450,"Long messages must scroll");
    JPanel startOver=AppDialogs.buildCard("Are you sure you want to start over? All transaction data will be cleared.",
            "Start Over",JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION,v->{});
    startOver.setSize(startOver.getPreferredSize());layout(startOver);layout(startOver);
    JTextPane text=messageBody(startOver);
    var first=text.modelToView2D(0);
    var last=text.modelToView2D(text.getDocument().getLength());
    check(last.getY()>first.getY(),"Start Over message must wrap onto another line");
    check(last.getMaxX()<=text.getWidth() && last.getMaxY()<=text.getHeight(),"Entire message fits");
    check(text.getParent() instanceof JViewport && text.getWidth()==text.getParent().getWidth(),"Message tracks viewport width");
    java.awt.image.BufferedImage wrapped=new java.awt.image.BufferedImage(startOver.getWidth(),startOver.getHeight(),2);
    Graphics2D wg=wrapped.createGraphics();startOver.printAll(wg);wg.dispose();
    javax.imageio.ImageIO.write(wrapped,"png",new java.io.File("build/start-over-preview.png"));
    if(!GraphicsEnvironment.isHeadless()) {
     JFrame owner=new JFrame();owner.setSize(640,480);owner.setLocationRelativeTo(null);owner.setVisible(true);
     Component previous=owner.getGlassPane();
     try {
      for(String action:new String[]{"Yes","No","Escape"}) {
       Timer timer=new Timer(100,e->{ for(Window w:Window.getWindows()) if(w instanceof JDialog d && d.isShowing()) {
        check(owner.getGlassPane()!=previous && owner.getGlassPane().isVisible(),"Dim overlay active");
        if(action.equals("Escape")) {
         KeyStroke key=KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
         Object binding=d.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(key);
         d.getRootPane().getActionMap().get(binding).actionPerformed(new ActionEvent(d,0,"escape"));
        } else find(d,action).doClick();
       }});timer.setRepeats(false);timer.start();
       int result=AppDialogs.showConfirmDialog(owner,"Dialog behavior check","Confirmation",JOptionPane.YES_NO_OPTION);
       check(result==(action.equals("Yes")?0:action.equals("No")?1:-1),"Modal return value "+action);
       check(owner.getGlassPane()==previous && !previous.isVisible(),"Overlay restored");
      }
     } finally {owner.dispose();}
    }
    System.out.println("PASS: dialog variants, return values, long content, modal dismissal and overlay restoration");
   }catch(Exception e){throw new RuntimeException(e);}
  });
 }
}



