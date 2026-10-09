import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import qpal.model.BookingData.*;
import qpal.view.Admin.QueuePaymentDialog;
public class ReceiptFareTest {
 public static void main(String[] args) throws Exception {
  var fares=List.of(new FareLine("Regular",new BigDecimal("50"),new BigDecimal("50")),new FareLine("Student",new BigDecimal("50"),new BigDecimal("40")),new FareLine("PWD",new BigDecimal("50"),new BigDecimal("40")),new FareLine("Senior",new BigDecimal("50"),new BigDecimal("40")));
  var r=new Receipt(1,"TEST",LocalDate.now(),1,"BUS","A - B","Tomorrow","1A, 1B, 1C, 1D",new BigDecimal("170"),"Cash","Paid",fares);
  String text=r.fareBreakdown();
  for(String line:List.of("Fare subtotal: PHP 200","Student 20% (x1): -PHP 10","PWD 20% (x1): -PHP 10","Senior 20% (x1): -PHP 10","Total discount: PHP 30"))if(!text.contains(line))throw new AssertionError(line);
  var old=new Receipt(1,"OLD",LocalDate.now(),1,"BUS","A - B","Tomorrow","1A",new BigDecimal("50"),"Cash","Paid");
  if(!old.fareBreakdown().contains("Not recorded"))throw new AssertionError("Legacy discounts invented");
  String document=text+"\nTotal Number of Seats: 4\nTotal: PHP 170\nMode of Payment: Cash\nAmount Received: PHP 200.00\nChange: PHP 30.00";
  var measure=QueuePaymentDialog.class.getDeclaredMethod("receiptPaperHeight",String.class,int.class);measure.setAccessible(true);
  int height=(Integer)measure.invoke(null,document,220);
  int shortHeight=(Integer)measure.invoke(null,"Fare: PHP 50\nTotal: PHP 50\nAmount Paid: PHP 50",220);
  if(shortHeight>=height)throw new AssertionError("Receipt does not shrink with content");
  var image=new java.awt.image.BufferedImage(220,height,java.awt.image.BufferedImage.TYPE_INT_RGB);
  var g=image.createGraphics();g.setColor(java.awt.Color.WHITE);g.fillRect(0,0,220,height);g.translate(7,8);
  var draw=QueuePaymentDialog.class.getDeclaredMethod("drawReceiptPaper",java.awt.Graphics2D.class,String.class,int.class,int.class);draw.setAccessible(true);
  draw.invoke(null,g,document,206,height-20);
  g.dispose();javax.imageio.ImageIO.write(image,"png",new java.io.File("build/receipt-discounts.png"));
  System.out.println("PASS: fare subtotal, all discounts, total, legacy handling and receipt render");
 }
}
