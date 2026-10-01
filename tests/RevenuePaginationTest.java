import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import qpal.view.Admin.AdminRevenuePanel;

public class RevenuePaginationTest {
    static Object get(Object o,String name) throws Exception {
        Field f=o.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(o);
    }
    static void call(Object o,String name) throws Exception {
        Method m=o.getClass().getDeclaredMethod(name); m.setAccessible(true); m.invoke(o);
    }
    static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> { try {
            AdminRevenuePanel panel=new AdminRevenuePanel();
            List<Object[]> rows=(List<Object[]>)get(panel,"revenues");
            for(int i=0;i<29;i++) rows.add(new Object[]{i,String.format("P%03d",i),"Bus","Route","Today",50,"Paid","1A"});
            for(int i=0;i<26;i++) rows.add(new Object[]{100+i,String.format("P%03d",100+i),"Bus","Route","Today",50,"Pending","1B"});
            rows.add(new Object[]{200,"Cancelled","Bus","Route","Today",50,"Cancelled","1C"});
            call(panel,"searchRevenue");
            JTable table=(JTable)get(panel,"table");
            check(table.getValueAt(0,7).equals("Pending"),"Pending first");
            JButton next=(JButton)get(panel,"btnNext"),one=(JButton)get(panel,"btnOne"),two=(JButton)get(panel,"btnTwo");
            next.doClick(); next.doClick();
            check(one.getText().equals("3") && two.getText().equals("4"),"Page 3 shows 3/4");
            two.doClick(); check((int)get(panel,"currentPage")==4,"Page 4 button works");
            next.doClick(); check(one.getText().equals("5") && two.getText().equals("6"),"Page 5 shows 5/6");
            two.doClick(); check(table.getRowCount()==6 && !next.isEnabled(),"Last page is complete");
            check(table.getValueAt(5,7).equals("Paid"),"Paid last");
            ((JComboBox<?>)get(panel,"cmbStatus")).setSelectedItem("Paid");
            check(((JLabel)get(panel,"lblInfo")).getText().contains("of 29 commuters"),"Exact paid count");
            int count=table.getRowCount();
            while(next.isEnabled()) { next.doClick(); count+=table.getRowCount(); }
            check(count==29 && !two.isVisible() && one.getText().equals("3"),"29 paid rows across pages; odd final page");
            check(table.getColumnName(1).equals("Queue No.") && table.getColumnName(2).equals("Seat"),"Queue and seat columns adjacent");
            ((JTextField)get(panel,"searchField")).setText("P013"); call(panel,"searchRevenue");
            check(table.getRowCount()==1 && table.getValueAt(0,1).equals("P013"),"Queue number search");
            ((JTextField)get(panel,"searchField")).setText("1A"); call(panel,"searchRevenue");
            check(((JLabel)get(panel,"lblInfo")).getText().contains("of 29 commuters"),"Seat search");
            ((JTextField)get(panel,"searchField")).setText("missing"); call(panel,"searchRevenue");
            check(table.getRowCount()==0 && !next.isEnabled(),"Empty search resets pagination");
        } catch(Exception ex) { throw new RuntimeException(ex); } });
        System.out.println("PASS: revenue status order, 29 commuters, page pairs, last page and empty search");
    }
}
