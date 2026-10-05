import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import qpal.view.Admin.AdminQueuePanel;
import qpal.model.BookingData.QueueRow;
public class BoardingPaginationTest {
 static Object get(Object o,String n)throws Exception {Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static void set(Object o,String n,Object v)throws Exception {Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 static Object call(Object o,String n)throws Exception {Method m=o.getClass().getDeclaredMethod(n);m.setAccessible(true);return m.invoke(o);}
 static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception {
  SwingUtilities.invokeAndWait(()->{try {
   AdminQueuePanel panel=new AdminQueuePanel();
   var rows=new ArrayList<QueueRow>();
   DefaultTableModel model=(DefaultTableModel)get(panel,"boardingModel");
   set(panel,"loading",true);
   for(int i=1;i<=12;i++){rows.add(new QueueRow(i,i,i,"A - B","BUS","Tomorrow","Passenger","Paid","Boarding",1));model.addRow(new Object[]{String.format("B%03d",i),"A - B","BUS","Tomorrow","Paid","Boarding",1});}
   set(panel,"boardingRows",rows); set(panel,"loading",false);
   set(panel,"paymentStations",Map.of(1,91)); set(panel,"boardingStations",Map.of(2,3));
   set(panel,"rows",List.of(new QueueRow(91,91,9,"A - B","BUS","Tomorrow","Group","Paid","Serving",5)));
   JTabbedPane tabs=(JTabbedPane)get(panel,"queues");tabs.setSelectedIndex(1);
   var stats=(java.util.List<JLabel>)get(panel,"stats");
   check(stats.get(2).getText().equals("— | B003"),"Boarding tab shows gate queues immediately");
   tabs.setSelectedIndex(0);
   check(stats.get(2).getText().equals("P009 | —"),"Payment tab restores counter queues");
   tabs.setSelectedIndex(1);
   call(panel,"loadBoardingPage");
   JTable table=(JTable)get(panel,"boardingTable");check(table.getRowCount()==10,"First page has ten rows");
   set(panel,"boardingPage",2);call(panel,"loadBoardingPage");check(table.getRowCount()==2,"Second page has two rows");
   table.setRowSelectionInterval(1,1);check(((QueueRow)call(panel,"selectedRow")).id()==12,"Actions map visible row to correct booking");
   set(panel,"boardingSearch","b003");call(panel,"loadBoardingPage");check(table.getRowCount()==1,"Prefixed search works across pages");
   table.setRowSelectionInterval(0,0);check(((QueueRow)call(panel,"selectedRow")).id()==3,"Filtered actions target correct booking");
   check(((JLabel)get(panel,"number")).getText().equals("B003"),"Details show boarding prefix");
   set(panel,"boardingSearch","missing");call(panel,"loadBoardingPage");check(table.getRowCount()==0,"Empty search works");
  }catch(Exception ex){throw new RuntimeException(ex);}});
  System.out.println("PASS: boarding pagination, search, selection mapping and details");
 }
}
