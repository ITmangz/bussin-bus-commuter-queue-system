package qpal.view.Admin;

import java.awt.*;
import java.awt.print.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qpal.dao.*;
import qpal.model.BookingData.*;
import qpal.util.UiTask;

/** Payment, receipt collection, and one boarding pass per reserved seat. */
public final class QueuePaymentDialog extends JDialog {
    private final QueueRow row;
    private final int station;
    private Receipt receipt;
    private QueuePaymentDao.Progress progress;
    private final JTextField received = new JTextField();
    private final JLabel change = new JLabel("Change: PHP 0.00");
    private final JLabel status = new JLabel(" ");
    private final JButton pay = new JButton("Mark as Paid");


    private boolean busy;
    private Window dashboard;
    private boolean dashboardWasVisible;
    private boolean printingView;

    private void hideForPrinting() {
        if (printingView) return;
        printingView=true;
        dashboard=getOwner();
        dashboardWasVisible=dashboard!=null && dashboard.isVisible();
        setVisible(false);
        if (dashboardWasVisible) dashboard.setVisible(false);
    }

    private void returnToQueue() {
        if (printingView && dashboardWasVisible) {
            dashboard.setVisible(true);
            dashboard.toFront();
        }
        printingView=false;
        busy=false;
        dispose();
    }

    public static void open(Component parent, QueueRow row, int station, Runnable closed) {
        UiTask.run(() -> new Object[]{new BookingDao().receipt(row.bookingId()),new QueuePaymentDao().progress(row.id())}, data -> {
            QueuePaymentDialog dialog = new QueuePaymentDialog(parent,row,station,(Receipt)data[0],(QueuePaymentDao.Progress)data[1]);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosed(java.awt.event.WindowEvent event) { closed.run(); }
            });
            dialog.setVisible(true);
        }, ex -> { qpal.components.AppDialogs.showMessageDialog(parent,ex.getMessage()); closed.run(); });
    }

    public static void openTickets(Component parent, QueueRow row, int station, Runnable closed) {
        UiTask.run(() -> new Object[]{new BookingDao().receipt(row.bookingId()),new QueuePaymentDao().progress(row.id())}, data -> {
            Receipt receipt=(Receipt)data[0];
            QueuePaymentDao.Progress progress=(QueuePaymentDao.Progress)data[1];
            if (!"Paid".equals(receipt.paymentStatus()) || !progress.receiptPrinted()) {
                qpal.components.AppDialogs.showMessageDialog(parent,
                        "Please finish Payment and collect the payment receipt before printing boarding tickets.",
                        "Payment Required",JOptionPane.WARNING_MESSAGE);
                closed.run(); return;
            }
            QueuePaymentDialog dialog=new QueuePaymentDialog(parent,row,station,receipt,progress);
            try { dialog.preview(true); }
            finally { dialog.dispose(); closed.run(); }
        }, ex -> { qpal.components.AppDialogs.showMessageDialog(parent,ex.getMessage()); closed.run(); });
    }

    private QueuePaymentDialog(Component parent, QueueRow row, int station, Receipt receipt, QueuePaymentDao.Progress progress) {
        super(SwingUtilities.getWindowAncestor(parent),"Payment",Dialog.ModalityType.APPLICATION_MODAL);
        this.row=row; this.station=station; this.receipt=receipt; this.progress=progress;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { if (!busy) dispose(); }
        });
        setUndecorated(true);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE); content.setBorder(new EmptyBorder(25,30,25,30));
        JLabel title = new JLabel("Payment");
        title.setFont(new Font("SansSerif",Font.BOLD,18)); title.setForeground(new Color(225,29,72));
        title.setAlignmentX(Component.LEFT_ALIGNMENT); content.add(title); content.add(Box.createVerticalStrut(7));
        JLabel subtitle = new JLabel("Review the queue details and enter payment.");
        subtitle.setFont(new Font("SansSerif",Font.PLAIN,12)); subtitle.setForeground(new Color(100,100,100));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT); content.add(subtitle); content.add(Box.createVerticalStrut(20));
        addField(content,"Queue Number",String.format("P%03d",row.number()));
        addField(content,"Route",receipt.route());
        addField(content,"Total Seats",String.valueOf(row.passengers()));
        addField(content,"Seat Numbers",receipt.seats());
        addField(content,"Total Fare (PHP)",receipt.total().toPlainString());
        addInput(content,"Amount Received (PHP)",received);
        if (progress.received()!=null) received.setText(progress.received().toPlainString());
        ((javax.swing.text.AbstractDocument)received.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override public void insertString(FilterBypass fb,int offset,String text,javax.swing.text.AttributeSet attributes)
                    throws javax.swing.text.BadLocationException {
                replace(fb,offset,0,text,attributes);
            }
            @Override public void replace(FilterBypass fb,int offset,int length,String text,javax.swing.text.AttributeSet attributes)
                    throws javax.swing.text.BadLocationException {
                String current=fb.getDocument().getText(0,fb.getDocument().getLength());
                String value=current.substring(0,offset)+(text==null ? "" : text)+current.substring(offset+length);
                if (value.matches("[0-9]{0,5}(\\.[0-9]{0,2})?")) super.replace(fb,offset,length,text,attributes);
                else Toolkit.getDefaultToolkit().beep();
            }
        });
        change.setFont(new Font("SansSerif",Font.PLAIN,12)); change.setAlignmentX(Component.LEFT_ALIGNMENT);
        status.setFont(new Font("SansSerif",Font.PLAIN,11)); status.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(change); content.add(Box.createVerticalStrut(8)); content.add(status); content.add(Box.createVerticalStrut(20));
        JPanel buttons = new JPanel(new GridLayout(1,2,12,0)); buttons.setOpaque(false);
        JButton close = new JButton("Close"); close.addActionListener(e -> { if (!busy) dispose(); });
        for (JButton button : new JButton[]{pay,close}) {
            button.setBackground(button==pay ? new Color(0,190,100) : new Color(240,0,55));
            button.setForeground(Color.WHITE); button.setFont(new Font("SansSerif",Font.BOLD,13));
            button.setFocusPainted(false); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); buttons.add(button);
        }
        content.add(buttons);
        for (Component component : content.getComponents()) {
            if (component instanceof JLabel label) {
                int height=label==title ? 28 : 20;
                label.setPreferredSize(new Dimension(300,height));
                label.setMinimumSize(new Dimension(300,height));
                label.setMaximumSize(new Dimension(300,height));
            }
        }
        setContentPane(AdminFormStyle.frame(content));
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                if ("Paid".equals(QueuePaymentDialog.this.receipt.paymentStatus()) && !QueuePaymentDialog.this.progress.receiptPrinted())
                    SwingUtilities.invokeLater(() -> preview(false));
            }
        });
        received.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateChange(); }
        });
        pay.addActionListener(e -> pay());

        updateChange(); updateButtons(); pack();
        Rectangle screen = getGraphicsConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int maximumHeight = screen.height - insets.top - insets.bottom - 40;
        if (getHeight() > maximumHeight) {
            Container form = getContentPane();
            JScrollPane scroll = new JScrollPane(form);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            qpal.components.ScrollBarStyle.apply(scroll,Color.WHITE);
            setContentPane(scroll);
            setSize(getWidth() + 12,maximumHeight);
        }
        setLocationRelativeTo(getOwner());
    }

    private static void addField(JPanel content,String label,String value) {
        JTextField field=new JTextField(value); field.setEditable(false);
        field.setToolTipText(value); field.setCaretPosition(0);
        addInput(content,label,field);
    }
    private static void addInput(JPanel content,String label,JTextField field) {
        JLabel caption=new JLabel(label); caption.setFont(new Font("SansSerif",Font.BOLD,13));
        caption.setAlignmentX(Component.LEFT_ALIGNMENT); caption.setLabelFor(field);
        content.add(caption); content.add(Box.createVerticalStrut(7));
        field.setFont(new Font("SansSerif",Font.PLAIN,14)); field.setBackground(new Color(220,220,220));
        field.setBorder(new EmptyBorder(8,10,8,10)); field.setMaximumSize(new Dimension(300,38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT); content.add(field); content.add(Box.createVerticalStrut(14));
    }

    private void autoPrintReceipt() {
        List<String> pages=List.of(receiptText());
        busy=true; updateButtons();
        hideForPrinting();
        UiTask.run(() -> {
            PrinterJob job=PrinterJob.getPrinterJob();
            if (job.getPrintService()==null) throw new PrinterException("No printer is available. Payment is saved; connect a printer and retry.");
            job.setJobName("BUSSIN Payment Receipt"); job.setCopies(1);
            job.setPrintable((graphics,format,index) -> {
                if (index>0) return Printable.NO_SUCH_PAGE;
                Graphics2D g=(Graphics2D)graphics.create();
                g.translate(format.getImageableX(),format.getImageableY());
                drawDocument(g,pages.get(0),format.getImageableWidth(),format.getImageableHeight());
                g.dispose(); return Printable.PAGE_EXISTS;
            });
            job.print(); return true;
        }, result -> collect(false,pages), ex -> {
            error(ex); returnToQueue();
        });
    }

    private void updateChange() {
        try { change.setText("Change: PHP " + QueuePaymentDao.validateReceived(received.getText(),receipt.total()).subtract(receipt.total()).toPlainString()); }
        catch (IllegalArgumentException ex) { change.setText("Enter an amount covering the total fare."); }
    }
    private void updateButtons() {
        boolean paid = "Paid".equals(receipt.paymentStatus());
        received.setEditable(!paid && !busy); pay.setEnabled(!paid && !busy);


        status.setText(progress.ticketsPrinted() ? "Tickets collected. You can now complete the queue."
                : progress.receiptPrinted() ? "Receipt collected. Use Print Ticket in the queue." : paid ? "Paid. Print the payment receipt next." : "Enter payment to continue.");
    }
    private void pay() {
        final BigDecimal amount;
        try { amount=QueuePaymentDao.validateReceived(received.getText(),receipt.total()); }
        catch (IllegalArgumentException ex) { error(ex); return; }
        busy=true; updateButtons();
        UiTask.run(() -> {
            new QueuePaymentDao().pay(row.id(),station,amount);
            return new Object[]{new BookingDao().receipt(row.bookingId()),new QueuePaymentDao().progress(row.id())};
        }, data -> {
            receipt=(Receipt)data[0]; progress=(QueuePaymentDao.Progress)data[1]; busy=false; updateButtons(); autoPrintReceipt();
        }, ex -> { busy=false; updateButtons(); error(ex); });
    }

    public static List<String> boardingTickets(Receipt receipt) {
        return java.util.Arrays.stream(receipt.seats().split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(seat ->
                "BUSSIN | BOARDING PASS\n\nQueue: " + String.format("B%03d",receipt.queueNumber())
                + "\nBooking: " + receipt.reference() + "\nBus: " + receipt.bus() + "\nRoute: " + receipt.route()
                + "\nDeparture: " + receipt.schedule() + "\nSeat: " + seat + "\n\nOne passenger · Keep this ticket for boarding.").toList();
    }
    private String receiptText() {
        return "BUSSIN | PAYMENT RECEIPT\n\n" + receipt.text() + "\nTotal seats: " + row.passengers()
                + (progress.received()==null ? "\nAmount received: previously paid" : "\nAmount received: PHP " + progress.received()
                + "\nChange: PHP " + progress.received().subtract(receipt.total()));
    }
    private void preview(boolean tickets) {
        List<String> pages = tickets ? boardingTickets(receipt) : List.of(receiptText());
        if (tickets && pages.size()!=row.passengers()) { error(new IllegalStateException("Seat count changed. Reopen Payment before printing.")); return; }
        if (tickets) { collect(true,pages); return; }
        JDialog preview = new JDialog(this,tickets ? "Boarding Tickets" : "Payment Receipt",true);
        JPanel panel = new JPanel(new BorderLayout(10,10)); panel.setBorder(new EmptyBorder(18,18,18,18)); panel.setBackground(Color.WHITE);
        JTextArea text = new JTextArea(String.join("\n\n------------------------------\n\n",pages),18,44);
        text.setEditable(false); text.setFont(new Font("Monospaced",Font.PLAIN,13)); panel.add(new JScrollPane(text),BorderLayout.CENTER);
        JButton print = new JButton(tickets ? "Print all " + pages.size() + " boarding tickets" : "Print payment receipt");
        panel.add(print,BorderLayout.SOUTH);
        print.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob(); job.setJobName(tickets ? "BUSSIN Boarding Tickets" : "BUSSIN Payment Receipt");
            job.setCopies(1);
            job.setPrintable((graphics,format,index) -> {
                if (index>=pages.size()) return Printable.NO_SUCH_PAGE;
                Graphics2D g=(Graphics2D)graphics.create();
                g.translate(format.getImageableX(),format.getImageableY());
                drawDocument(g,pages.get(index),format.getImageableWidth(),format.getImageableHeight());
                g.dispose(); return Printable.PAGE_EXISTS;
            });
            if (!job.printDialog()) return;
            busy=true; updateButtons(); print.setEnabled(false); preview.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            UiTask.run(() -> { job.print(); return true; }, ok -> {
                preview.dispose(); collect(tickets,pages);
            }, ex -> { busy=false; updateButtons(); print.setEnabled(true); preview.setDefaultCloseOperation(DISPOSE_ON_CLOSE); error(ex); });
        });
        preview.setContentPane(panel); preview.pack(); preview.setLocationRelativeTo(this); preview.setVisible(true);
    }

    static void drawDocument(Graphics2D g,String text,double width,double height) {
        g.setFont(new Font("SansSerif",Font.PLAIN,13));
        String[] lines=text.split("\n");
        int widest=java.util.Arrays.stream(lines).mapToInt(s -> g.getFontMetrics().stringWidth(s)).max().orElse(300);
        double scale=Math.min(1,Math.min(width/(widest+30.0),height/(lines.length*22.0+30)));
        g.scale(scale,scale); g.setColor(new Color(210,0,50)); g.fillRect(0,0,widest+30,30);
        int y=21;
        for (int i=0;i<lines.length;i++) { g.setColor(i==0 ? Color.WHITE : Color.BLACK); g.drawString(lines[i],12,y); y+=22; }
    }

    private static void drawReceiptPaper(Graphics2D g,String text,int width,int height) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif",Font.PLAIN,12));
        String[] lines=text.split("\n");
        int widest=java.util.Arrays.stream(lines).mapToInt(line -> g.getFontMetrics().stringWidth(line)).max().orElse(1);
        double scale=Math.min(width/(double)Math.max(1,widest),height/(lines.length*18.0));
        g.scale(scale,scale); g.setColor(Color.BLACK);
        int y=13;
        for (String line:lines) { g.drawString(line,0,y); y+=18; }
    }

    private void collect(boolean tickets,List<String> pages) {
        // Independent owner keeps the printer visible while both application windows are hidden.
        JDialog popup=new JDialog((Window)null,"Collect " + (tickets ? "boarding tickets" : "receipt"),Dialog.ModalityType.APPLICATION_MODAL);
        popup.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        popup.setUndecorated(true);
        popup.setBackground(new Color(0,0,0,0));
        popup.getRootPane().setOpaque(false);
        popup.getLayeredPane().setOpaque(false);
        final double[] fraction={0};
        final int[] current={0};
        final String[] seats=receipt.seats().split(",\\s*");
        final int paperX=86, paperY=99, paperWidth=220, paperHeight=tickets ? 592 : 251;
        Image machine=new ImageIcon("resources/icons/printmachine.png").getImage();
        JPanel paper=new JPanel(null) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g=(Graphics2D)graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                // Keep the machine at its existing size; the asset includes transparent margins.
                g.drawImage(machine,0,0,420,672,this);
                g.clipRect(paperX,paperY,paperWidth,paperHeight);
                g.translate(paperX,paperY-paperHeight+paperHeight*fraction[0]);
                java.awt.geom.Path2D sheet=new java.awt.geom.Path2D.Double();
                sheet.moveTo(0,0); sheet.lineTo(paperWidth,0); sheet.lineTo(paperWidth,paperHeight-3);
                for (int x=paperWidth;x>0;x-=6) {
                    sheet.lineTo(Math.max(0,x-3),paperHeight);
                    sheet.lineTo(Math.max(0,x-6),paperHeight-3);
                }
                sheet.closePath();
                g.setColor(new Color(253,253,253)); g.fill(sheet); g.clip(sheet);
                if (tickets) {
                    g.translate(paperWidth,0); g.rotate(Math.PI/2);
                    BoardingPass.draw(g,receipt,seats[current[0]],paperHeight,paperWidth);
                } else {
                    g.translate(7,8);
                    drawReceiptPaper(g,pages.get(0),paperWidth-14,paperHeight-20);
                }
                g.dispose();
            }
        };
        paper.setOpaque(false); paper.setPreferredSize(new Dimension(420,tickets ? 710 : 410));
        JButton collect=new JButton(); collect.setEnabled(false); collect.setVisible(false);
        collect.setBounds(paperX,paperY,paperWidth,paperHeight);
        collect.setOpaque(false); collect.setContentAreaFilled(false); collect.setBorderPainted(false);
        collect.setFocusPainted(false); collect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        collect.getAccessibleContext().setAccessibleName("Collect " + (tickets ? pages.size()+" boarding tickets" : "payment receipt"));
        collect.setToolTipText("Click the paper to collect " + (tickets ? "boarding tickets" : "your receipt"));
        paper.add(collect);
        long[] start={System.nanoTime()};
        Timer timer=new Timer(30,null);
        timer.addActionListener(e -> { fraction[0]=Math.min(1,(System.nanoTime()-start[0])/1_800_000_000.0); paper.repaint();
            if (fraction[0]>=1) { timer.stop(); collect.setVisible(true); collect.setEnabled(true); collect.requestFocusInWindow(); }
        });
        Runnable printNext = () -> {
            fraction[0]=0; collect.setVisible(false); collect.setEnabled(false); paper.repaint();
            busy=true; updateButtons();
            UiTask.run(() -> {
                new QueuePaymentDao().requireBoardingPayment(row.id());
                PrinterJob job=PrinterJob.getPrinterJob();
                if (job.getPrintService()==null) throw new PrinterException("No printer is available. Connect a printer and retry.");
                job.setJobName("BUSSIN Boarding Pass " + (current[0]+1) + " of " + pages.size()); job.setCopies(1);
                final String seat=seats[current[0]];
                job.setPrintable((graphics,format,index) -> {
                    if (index>0) return Printable.NO_SUCH_PAGE;
                    Graphics2D g=(Graphics2D)graphics.create(); g.translate(format.getImageableX(),format.getImageableY());
                    BoardingPass.draw(g,receipt,seat,format.getImageableWidth(),format.getImageableHeight());
                    g.dispose(); return Printable.PAGE_EXISTS;
                });
                job.print(); return true;
            }, printed -> {
                collect.setToolTipText("Click to collect boarding ticket " + (current[0]+1) + " of " + pages.size());
                collect.getAccessibleContext().setAccessibleName(collect.getToolTipText());
                start[0]=System.nanoTime(); timer.start();
            }, ex -> { error(ex); busy=false; updateButtons(); popup.dispose(); });
        };
        collect.addActionListener(e -> {
            collect.setEnabled(false);
            if (tickets && current[0]+1 < pages.size()) {
                current[0]++; printNext.run(); return;
            }
            UiTask.run(() -> { new QueuePaymentDao().printed(row.id(),tickets); return new QueuePaymentDao().progress(row.id()); }, saved -> {
                progress=saved; busy=false; updateButtons(); popup.dispose();
                dispose();
            }, ex -> {
                collect.setEnabled(true); error(ex);
                popup.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                popup.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override public void windowClosed(java.awt.event.WindowEvent event) { busy=false; updateButtons(); }
                });
            });
        });
        popup.setContentPane(paper); popup.pack(); popup.setLocationRelativeTo(null);
        hideForPrinting();
        try {
            if (tickets) printNext.run(); else timer.start();
            popup.setVisible(true);
        } finally {
            timer.stop(); popup.dispose(); returnToQueue();
        }
    }
    private void error(Exception ex) { qpal.components.AppDialogs.showMessageDialog(isShowing() ? this : null,ex.getMessage(),"Payment",JOptionPane.WARNING_MESSAGE); }
}
