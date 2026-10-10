package qpal.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.*;
import javax.swing.*;

/** Serial Windows announcements; queue actions never wait for speech. */
public final class QueueVoice {
    private QueueVoice() {}

    private static final ExecutorService SPEAKER =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread thread = new Thread(r, "queue-voice");
                        thread.setDaemon(true);
                        return thread;
                    });
    private static boolean warned;
    private static final String SCRIPT =
            """
$ErrorActionPreference='Stop'
$voice=New-Object -ComObject SAPI.SpVoice
$token=New-Object -ComObject SAPI.SpObjectToken
$token.SetId('HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Speech_OneCore\\Voices\\Tokens\\MSTTS_V110_enUS_MarkM')
$voice.Voice=$token
$voice.Rate=0
$voice.Volume=100
[void]$voice.Speak($env:QPAL_QUEUE_ANNOUNCEMENT)
""";

    public static void announce(int number, boolean boarding, int station, boolean skipped) {
        String queue =
                (boarding ? "B " : "P ")
                        + String.format(java.util.Locale.ROOT, "%03d", number)
                                .replace("", " ")
                                .trim();
        String message =
                "Queue number "
                        + queue
                        + (skipped
                                ? " is skipped."
                                : ", please proceed to "
                                        + (boarding ? "boarding gate " : "payment counter ")
                                        + station
                                        + ".");
        SPEAKER.execute(
                () -> {
                    try {
                        String encoded =
                                Base64.getEncoder()
                                        .encodeToString(SCRIPT.getBytes(StandardCharsets.UTF_16LE));
                        ProcessBuilder builder =
                                new ProcessBuilder(
                                        "powershell.exe",
                                        "-NoProfile",
                                        "-NonInteractive",
                                        "-WindowStyle",
                                        "Hidden",
                                        "-EncodedCommand",
                                        encoded);
                        builder.environment().put("QPAL_QUEUE_ANNOUNCEMENT", message);
                        builder.redirectErrorStream(true);
                        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                        Process process = builder.start();
                        if (!process.waitFor(45, TimeUnit.SECONDS)) {
                            process.destroyForcibly();
                            throw new IllegalStateException("Speech timed out.");
                        }
                        if (process.exitValue() != 0)
                            throw new IllegalStateException(
                                    "Microsoft Mark voice could not be started.");
                    } catch (Exception ex) {
                        System.err.println("Queue announcement: " + ex.getMessage());
                        if (!warned) {
                            warned = true;
                            SwingUtilities.invokeLater(
                                    () ->
                                            qpal.components.AppDialogs.showMessageDialog(
                                                    null,
                                                    "Microsoft Mark could not announce the queue."
                                                        + " The queue action was saved. Check"
                                                        + " Windows speech and audio settings.",
                                                    "Queue Voice",
                                                    JOptionPane.WARNING_MESSAGE));
                        }
                    }
                });
    }
}
