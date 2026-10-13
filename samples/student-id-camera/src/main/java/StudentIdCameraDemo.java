import com.github.sarxos.webcam.Webcam;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.LoadLibs;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Standalone camera/OCR sample. It does not connect to the queue system or save captures. */
public final class StudentIdCameraDemo extends JFrame {
    private final JComboBox<Webcam> cameras = new JComboBox<>();
    private final JButton start = button("Start camera"), stop = button("Stop camera");
    private final JButton capture = button("Capture & read"), upload = button("Open image");
    private final JCheckBox automatic = new JCheckBox("Auto-read every 3 seconds");
    private final JTextArea raw = new JTextArea(7, 28), result = new JTextArea(5, 28);
    private final JLabel status = new JLabel("Finding cameras...");
    private final ImageView live = new ImageView("Start your camera, then hold the ID steady.");
    private final ImageView captured = new ImageView("Your captured image appears here.");
    private final ExecutorService cameraTasks =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread thread = new Thread(r, "camera");
                        thread.setDaemon(true);
                        return thread;
                    });
    private final ExecutorService ocrTasks =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread thread = new Thread(r, "ocr");
                        thread.setDaemon(true);
                        return thread;
                    });
    private volatile Webcam webcam;
    private volatile BufferedImage latest;
    private volatile boolean running, closed;
    private boolean reading, changingCamera;
    private final javax.swing.Timer previewTimer =
            new javax.swing.Timer(120, e -> live.showImage(latest));
    private final javax.swing.Timer autoTimer =
            new javax.swing.Timer(
                    3000,
                    e -> {
                        if (automatic.isSelected() && running && !reading && latest != null)
                            read(latest);
                    });

    public StudentIdCameraDemo() {
        super("Student ID camera test — Bussin");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel heading = new JPanel(new BorderLayout(0, 6));
        JLabel title = new JLabel("Student ID scanner · sample");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        heading.add(title, BorderLayout.NORTH);
        heading.add(
                new JLabel("PUP · NU MOA · NU APC · FEU Tech | Review every result"),
                BorderLayout.SOUTH);
        root.add(heading, BorderLayout.NORTH);
        JPanel cameraArea = new JPanel(new BorderLayout(8, 12));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        cameras.setPreferredSize(new Dimension(200, 34));
        controls.add(cameras);
        controls.add(start);
        controls.add(stop);
        cameraArea.add(controls, BorderLayout.NORTH);
        cameraArea.add(live, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.add(capture);
        actions.add(upload);
        actions.add(automatic);
        cameraArea.add(actions, BorderLayout.SOUTH);
        JPanel review = new JPanel(new BorderLayout(0, 10));
        review.setPreferredSize(new Dimension(350, 500));
        captured.setPreferredSize(new Dimension(350, 200));
        review.add(captured, BorderLayout.NORTH);
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(new JLabel("OCR text — you may correct it here"));
        raw.setLineWrap(true);
        raw.setWrapStyleWord(true);
        details.add(new JScrollPane(raw));
        JButton recheck = button("Check edited text");
        recheck.addActionListener(e -> showMatches(raw.getText()));
        details.add(recheck);
        details.add(new JLabel("Matching student-number formats"));
        result.setEditable(false);
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        details.add(new JScrollPane(result));
        review.add(details, BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cameraArea, review);
        split.setResizeWeight(0.65);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 5));
        footer.add(status);
        footer.add(
                new JLabel(
                        "Format match only; not proof of school or enrollment. Images stay in"
                            + " memory."));
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1140, 760);
        setLocationRelativeTo(null);
        start.addActionListener(e -> startCamera());
        stop.addActionListener(e -> stopCamera());
        capture.addActionListener(
                e -> {
                    if (latest != null) read(latest);
                });
        upload.addActionListener(e -> openImage());
        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        closed = true;
                        running = false;
                        previewTimer.stop();
                        autoTimer.stop();
                        cameraTasks.shutdown();
                        ocrTasks.shutdownNow();
                        dispose();
                    }
                });
        buttons();
        cameraTasks.submit(
                () -> {
                    try {
                        List<Webcam> found = Webcam.getWebcams(15, TimeUnit.SECONDS);
                        SwingUtilities.invokeLater(
                                () -> {
                                    if (closed) return;
                                    for (Webcam device : found) cameras.addItem(device);
                                    status.setText(
                                            found.isEmpty()
                                                    ? "No camera found. You can still test with"
                                                          + " Open image."
                                                    : "Ready. Choose a camera and click Start"
                                                          + " camera.");
                                    buttons();
                                });
                    } catch (Throwable ex) {
                        error("Camera discovery failed. Open image is still available.", ex);
                    }
                });
    }

    private void startCamera() {
        Webcam selected = (Webcam) cameras.getSelectedItem();
        if (selected == null || running || changingCamera) return;
        changingCamera = true;
        latest = null;
        status.setText("Opening camera...");
        buttons();
        cameraTasks.submit(
                () -> {
                    try {
                        webcam = selected;
                        Dimension largest =
                                java.util.Arrays.stream(selected.getViewSizes())
                                        .max(
                                                java.util.Comparator.comparingInt(
                                                        d -> d.width * d.height))
                                        .orElse(new Dimension(640, 480));
                        selected.setViewSize(largest);
                        if (closed || !selected.open())
                            throw new IllegalStateException("Camera could not open.");
                        running = !closed;
                        SwingUtilities.invokeLater(
                                () -> {
                                    if (closed) return;
                                    changingCamera = false;
                                    buttons();
                                    previewTimer.start();
                                    autoTimer.start();
                                    status.setText(
                                            "Hold the ID close enough to read the number. Avoid"
                                                + " glare; click Capture & read.");
                                });
                        while (running && !closed) {
                            BufferedImage frame = selected.getImage();
                            if (frame != null) latest = copy(frame);
                            Thread.sleep(100);
                        }
                    } catch (Throwable ex) {
                        error("Camera unavailable. Close other camera apps and try again.", ex);
                    } finally {
                        running = false;
                        try {
                            if (selected.isOpen()) selected.close();
                        } catch (Throwable ignored) {
                        }
                        SwingUtilities.invokeLater(
                                () -> {
                                    changingCamera = false;
                                    previewTimer.stop();
                                    autoTimer.stop();
                                    automatic.setSelected(false);
                                    buttons();
                                    if (status.getText().equals("Stopping camera..."))
                                        status.setText("Camera stopped.");
                                });
                    }
                });
    }

    private void stopCamera() {
        running = false;
        changingCamera = true;
        automatic.setSelected(false);
        previewTimer.stop();
        autoTimer.stop();
        buttons();
        status.setText("Stopping camera...");
    }

    private void openImage() {
        automatic.setSelected(false);
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        "ID images (PNG, JPG, BMP)", "png", "jpg", "jpeg", "bmp"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            BufferedImage image = ImageIO.read(chooser.getSelectedFile());
            if (image == null) throw new IllegalArgumentException("Unsupported image format.");
            read(image);
        } catch (Exception ex) {
            error("Could not open that image.", ex);
        }
    }

    private void read(BufferedImage image) {
        if (reading || closed) return;
        reading = true;
        buttons();
        captured.showImage(image);
        raw.setText("");
        result.setText("");
        status.setText("Reading ID text locally...");
        ocrTasks.submit(
                () -> {
                    try {
                        String text = recognize(image);
                        SwingUtilities.invokeLater(
                                () -> {
                                    if (closed) return;
                                    raw.setText(text);
                                    showMatches(text);
                                    status.setText(
                                            "Read complete. Check the number against the ID before"
                                                + " using it.");
                                });
                    } catch (Throwable ex) {
                        error("OCR failed. See README for the Windows runtime requirement.", ex);
                    } finally {
                        SwingUtilities.invokeLater(
                                () -> {
                                    reading = false;
                                    buttons();
                                });
                    }
                });
    }

    static String recognize(BufferedImage image) throws Exception {
        Tesseract engine = new Tesseract();
        engine.setDatapath(LoadLibs.extractTessResources("tessdata").getAbsolutePath());
        engine.setLanguage("eng");
        engine.setOcrEngineMode(1);
        engine.setPageSegMode(11);
        // Enlarge small camera frames to help OCR read the student number.
        double scale = Math.min(3.0, Math.max(1.0, 1800.0 / image.getWidth()));
        BufferedImage enlarged =
                new BufferedImage(
                        (int) (image.getWidth() * scale),
                        (int) (image.getHeight() * scale),
                        BufferedImage.TYPE_INT_RGB);
        Graphics2D g = enlarged.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, enlarged.getWidth(), enlarged.getHeight());
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(image, 0, 0, enlarged.getWidth(), enlarged.getHeight(), null);
        g.dispose();
        String fullText = engine.doOCR(enlarged);
        // A second pass focuses on the characters in the four supplied number examples.
        // Keep both readings visible rather than silently correcting an ID number.
        engine.setVariable("tessedit_char_whitelist", "0123456789MN-");
        String numberText = engine.doOCR(enlarged);
        return "Full-text reading:\n"
                + fullText
                + "\nNumber-focused reading (compare with the ID):\n"
                + numberText;
    }

    private void showMatches(String text) {
        List<StudentIdFormats.Match> matches = StudentIdFormats.find(text);
        result.setText(
                matches.isEmpty()
                        ? "No supported number found. Try a sharper image or correct the OCR text."
                        : String.join("\n", matches.stream().map(Object::toString).toList())
                                + (matches.size() > 1
                                        ? "\nMultiple matches — review which one belongs to the ID."
                                        : "\nReview required."));
    }

    private void buttons() {
        start.setEnabled(!closed && !running && !changingCamera && cameras.getItemCount() > 0);
        cameras.setEnabled(!running && !changingCamera);
        stop.setEnabled(running && !changingCamera);
        capture.setEnabled(running && !reading);
        upload.setEnabled(!reading);
        automatic.setEnabled(running);
    }

    private void error(String message, Throwable error) {
        System.err.println(message);
        error.printStackTrace();
        SwingUtilities.invokeLater(
                () -> {
                    if (!closed) status.setText(message);
                });
    }

    private static JButton button(String title) {
        JButton button = new JButton(title);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        return button;
    }

    private static BufferedImage copy(BufferedImage image) {
        BufferedImage copy =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return copy;
    }

    private static final class ImageView extends JPanel {
        private BufferedImage image;
        private final String placeholder;

        ImageView(String placeholder) {
            this.placeholder = placeholder;
            setBackground(new Color(240, 243, 247));
        }

        void showImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image == null) {
                graphics.setColor(Color.DARK_GRAY);
                graphics.drawString(placeholder, 16, 30);
                return;
            }
            double scale =
                    Math.min(
                            (double) getWidth() / image.getWidth(),
                            (double) getHeight() / image.getHeight());
            int w = (int) (image.getWidth() * scale), h = (int) (image.getHeight() * scale);
            graphics.drawImage(image, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--self-test")) {
            SampleChecks.run();
            return;
        }
        SwingUtilities.invokeLater(() -> new StudentIdCameraDemo().setVisible(true));
    }
}
