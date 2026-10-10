package qpal.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/** Run JDBC work away from Swing's event thread. */
public final class UiTask {
    private UiTask() {}

    public static <T> void run(Callable<T> work, Consumer<T> success, Consumer<Exception> failure) {
        new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception {
                return work.call();
            }

            protected void done() {
                try {
                    success.accept(get());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    failure.accept(cause instanceof Exception ? (Exception) cause : ex);
                }
            }
        }.execute();
    }
}
