import javax.swing.*;
import java.awt.*;
import qpal.dao.DashboardDao;
import qpal.view.Admin.AdminDashboardPanel;
public class BoardingProgressTest {
 static JProgressBar bar(Container root) {
  for(Component c:root.getComponents()) {
   if(c instanceof JProgressBar b) return b;
   if(c instanceof Container nested) { var b=bar(nested); if(b!=null) return b; }
  }
  return null;
 }
 public static void main(String[] args) throws Exception {
  SwingUtilities.invokeAndWait(() -> {
   var card=AdminDashboardPanel.boardingCard(new DashboardDao.BoardingTrip("Bus04","PITX - Amadeo","05:30 PM",10,30,6));
   var progress=bar(card);
   if(progress==null || progress.getValue()!=6 || progress.getMaximum()!=10) throw new AssertionError("Progress must use boarded / expected, not capacity");
   if(!progress.getAccessibleContext().getAccessibleName().equals("Passengers boarded 6 of 10")) throw new AssertionError("Boarding label");
   var empty=bar(AdminDashboardPanel.boardingCard(new DashboardDao.BoardingTrip("Bus04","PITX - Amadeo","05:30 PM",0,30,0)));
   if(empty.getValue()!=0 || empty.getPercentComplete()!=0) throw new AssertionError("Empty trip must show zero progress");
  });
  System.out.println("PASS: boarding progress uses passengers boarded / expected; empty trip stays at zero");
 }
}
