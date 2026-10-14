import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import qpal.view.Admin.QueueMonitor;
import qpal.model.BookingData.QueueRow;
public class QueueMonitorLayoutTest {
 static void layout(Container c) { c.doLayout(); for(Component child:c.getComponents())if(child instanceof Container nested)layout(nested); }
 static void checkLabels(Container c, JPanel card) {
  for(Component child:c.getComponents()) {
   if(child instanceof JLabel label) {
    Rectangle bounds=SwingUtilities.convertRectangle(label.getParent(),label.getBounds(),card);
    if(bounds.y<0 || bounds.y+bounds.height>card.getHeight() || label.getHeight()<label.getFontMetrics(label.getFont()).getHeight())throw new AssertionError("Clipped: "+label.getText()+" "+bounds);
   }
   if(child instanceof Container nested)checkLabels(nested,card);
  }
 }
 public static void main(String[] args)throws Exception {
  SwingUtilities.invokeAndWait(()->{try {
   var render=QueueMonitor.class.getDeclaredMethod("render",java.util.List.class,Map.class,Map.class,Map.class);render.setAccessible(true);
   var field=QueueMonitor.class.getDeclaredField("upcoming");field.setAccessible(true);
   for(int count:new int[]{0,1,2,5}) for(int[] size:new int[][]{{780,540},{980,600},{1200,720}}) {
    QueueMonitor monitor=new QueueMonitor(false);
    var rows=new ArrayList<QueueRow>();
    for(int i=0;i<count;i++)rows.add(new QueueRow(i+1,i+1,21+i,"Route","Bus","Today","Passenger","Pending","Waiting",1,i<2));
    render.invoke(monitor,rows,Map.of(),Map.of(),Map.of());
    monitor.setSize(size[0],size[1]);layout(monitor);
    checkLabels((JPanel)field.get(monitor),(JPanel)field.get(monitor));
    if(count==2 && size[0]==980) {
     var image=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();monitor.printAll(g);g.dispose();ImageIO.write(image,"png",new java.io.File("build/payment-monitor-fixed.png"));
    }
   }
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS: waiting queue labels fit for 0, 1, 2 and 5 queues at three window sizes");
 }
}
