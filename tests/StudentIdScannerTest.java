package qpal.util;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class StudentIdScannerTest {
    static String run(String python, String code, boolean cancelled) throws Exception {
        Process process = new ProcessBuilder(python, "-u", "-c", code).start();
        try { return StudentIdScanner.readNumber(process, new AtomicBoolean(cancelled)); }
        finally { if (process.isAlive()) process.destroyForcibly(); }
    }
    public static void main(String[] args) throws Exception {
        String python = args[0];
        var finished = new java.util.concurrent.CountDownLatch(1);
        var frames = new java.util.concurrent.atomic.AtomicInteger();
        var result = new java.util.concurrent.atomic.AtomicReference<String>();
        var image = new java.awt.image.BufferedImage(4, 4, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "jpg", bytes);
        String encoded = java.util.Base64.getEncoder().encodeToString(bytes.toByteArray());
        javax.swing.SwingUtilities.invokeAndWait(() -> new StudentIdScanner.Session(
                java.util.List.of(python, "-u", "-c", "import time; print('FRAME=" + encoded
                        + "'); time.sleep(.4); print('BUSSIN_STUDENT_ID=202511062')"),
                new StudentIdScanner.Listener() {
                    public void frame(java.awt.image.BufferedImage frame) { frames.incrementAndGet(); }
                    public void status(String message) { }
                    public void result(String number) { result.set(number); finished.countDown(); }
                    public void error(String error) { result.set(error); finished.countDown(); }
                }));
        if (!finished.await(10, java.util.concurrent.TimeUnit.SECONDS)
                || !"202511062".equals(result.get()) || frames.get() == 0)
            throw new AssertionError("Embedded preview/result delivery failed: " + result.get());
        for (String id : new String[]{"2025-09011-MN-0", "2025-1058827", "2025-140065", "202511062"}) {
            String received = run(python, "print('scanner ready'); print('BUSSIN_STUDENT_ID=" + id + "')", false);
            if (!id.equals(received)) throw new AssertionError("Number not received");
        }
        if (run(python, "print('BUSSIN_STUDENT_ID=202511062')", true) != null)
            throw new AssertionError("Cancelled scan returned a number");
        if (run(python, "print('cancelled')", false) != null)
            throw new AssertionError("No result changed field");
        if (run(python, "print('BUSSIN_SCAN_CANCELLED'); import time; time.sleep(10)", false) != null)
            throw new AssertionError("Scanner cancellation changed field");
        for (String code : new String[]{"print('BUSSIN_STUDENT_ID=invalid')", "raise SystemExit(1)"}) {
            try { run(python, code, false); throw new AssertionError("Invalid scan accepted"); }
            catch (IOException expected) { }
        }
        System.out.println("PASS: Python-to-Java transfer for four formats, cancelled/empty results, invalid output and process errors.");
    }
}
