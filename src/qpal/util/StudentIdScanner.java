package qpal.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;

/** Launches the Python camera reader; Java never opens the camera. */
public final class StudentIdScanner {
    private StudentIdScanner() {}

    public static boolean validNumber(String value) {
        return value != null && value.matches("(?:20[0-9]{2}-[0-9]{5}-[A-Z]{2}-[0-9]|20[0-9]{2}-[0-9]{6,7}|20[0-9]{7})");
    }

    static String readNumber(Process process, AtomicBoolean cancelled) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (cancelled.get()) return null;
                if (line.equals("BUSSIN_SCAN_CANCELLED")) return null;
                if (line.startsWith("BUSSIN_STUDENT_ID=")) {
                    String number = line.substring("BUSSIN_STUDENT_ID=".length()).trim();
                    if (!validNumber(number)) throw new IOException("Invalid scanner result");
                    return number;
                }
            }
        }
        if (process.waitFor() != 0 && !cancelled.get()) throw new IOException("Python scanner exited with an error");
        return null;
    }

    public interface Listener {
        void frame(java.awt.image.BufferedImage image);
        void status(String message);
        void result(String number);
        void error(String message);
    }

    public static Session start(Listener listener) {
        Path script = Path.of("samples", "student-card-python", "embedded_scanner.py").toAbsolutePath();
        String configured = System.getProperty("qpal.python", System.getenv("QPAL_PYTHON"));
        Path bundled = Path.of(System.getProperty("user.home"), ".cache", "codex-runtimes",
                "codex-primary-runtime", "dependencies", "python", "python.exe");
        String python = configured != null && !configured.isBlank() ? configured
                : Files.isRegularFile(bundled) ? bundled.toString() : "python";
        return new Session(java.util.List.of(python, "-X", "utf8", "-u", script.toString()), listener);
    }

    public static final class Session implements AutoCloseable {
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicReference<Process> running = new AtomicReference<>();
        private final AtomicReference<java.awt.image.BufferedImage> latest = new AtomicReference<>();
        private final Timer preview;
        private final Timer timeout;
        private final Listener listener;

        Session(java.util.List<String> command, Listener listener) {
            this.listener = listener;
            preview = new Timer(80, e -> {
                var image = latest.getAndSet(null);
                if (!cancelled.get() && image != null) listener.frame(image);
            });
            timeout = new Timer(185000, e -> finish(null, "Scan timed out. Try again or enter your ID manually."));
            timeout.setRepeats(false);
            preview.start(); timeout.start();
            Thread worker = new Thread(() -> read(command), "student-id-python");
            worker.setDaemon(true);
            worker.start();
        }

        private void read(java.util.List<String> command) {
            Process process = null;
            try {
                process = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
                running.set(process);
                if (cancelled.get()) return;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while (!cancelled.get() && (line = reader.readLine()) != null) {
                        if (line.startsWith("FRAME=")) {
                            if (line.length() > 1500000) throw new IOException("Oversized preview");
                            byte[] bytes = java.util.Base64.getDecoder().decode(line.substring(6));
                            var image = javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes));
                            if (image != null) latest.set(image);
                        } else if (line.startsWith("STATUS=")) {
                            String message = line.substring(7);
                            SwingUtilities.invokeLater(() -> { if (!cancelled.get()) listener.status(message); });
                        } else if (line.startsWith("BUSSIN_STUDENT_ID=")) {
                            String number = line.substring("BUSSIN_STUDENT_ID=".length()).trim();
                            if (!validNumber(number)) throw new IOException("Invalid scanner result");
                            finish(number, null);
                            return;
                        } else if (line.startsWith("ERROR=")) {
                            finish(null, line.substring(6));
                            return;
                        }
                    }
                }
                if (!cancelled.get()) finish(null, "Camera closed. Try again or enter your ID manually.");
            } catch (Exception ex) {
                if (!cancelled.get()) finish(null, "Scanner unavailable. Check the Python setup or enter your ID manually.");
            } finally {
                if (process != null && process.isAlive()) process.destroyForcibly();
                running.compareAndSet(process, null);
            }
        }

        private void finish(String number, String error) {
            SwingUtilities.invokeLater(() -> {
                if (cancelled.get()) return;
                close();
                if (number != null) listener.result(number); else listener.error(error);
            });
        }

        @Override public void close() {
            cancelled.set(true);
            preview.stop(); timeout.stop(); latest.set(null);
            Process process = running.get();
            if (process != null) process.destroyForcibly();
        }
    }
}
