package megalodonte.script;

import megalodonte.base.state.State;
import megalodonte.jsonc.*;
import java.util.Map;
import java.util.function.Consumer;

/** A per-view Java script. onSubmit runs only after the host validates the JSONC rules. */
public abstract class ViewScript {
    private UiSession session;
    private Consumer<String> navigator;
    private final java.util.concurrent.ExecutorService worker = java.util.concurrent.Executors.newSingleThreadExecutor(
            runnable -> Thread.ofVirtual().name("megalodonte-view-worker").unstarted(runnable));
    private volatile boolean detached;


    final void attach(UiSession session, Consumer<String> navigator) { this.session = session; this.navigator = navigator; }
    final void detach() { detached = true; worker.shutdownNow(); session = null; navigator = null; }
    /** Runs blocking work serially and delivers callbacks on the UI thread while this view is alive. */
    protected final <T> void background(java.util.concurrent.Callable<T> work,
                                      Consumer<T> success, Consumer<Exception> failure) {
        states();
        worker.submit(() -> {
            try {
                T value = work.call();
                javafx.application.Platform.runLater(() -> { if (!detached) success.accept(value); });
            } catch (Exception error) {
                if (!detached) javafx.application.Platform.runLater(() -> { if (!detached) failure.accept(error); });
            }
        });
    }
    /** Framework-owned native directory picker; scripts do not create toolkit controls. */
    protected final java.nio.file.Path chooseDirectory(String title, java.nio.file.Path initial) {
        states();
        var picker = new javafx.stage.DirectoryChooser();
        picker.setTitle(title);
        if (initial != null && java.nio.file.Files.isDirectory(initial)) picker.setInitialDirectory(initial.toFile());
        var chosen = picker.showDialog(session.render().getJavaFxNode().getScene().getWindow());
        return chosen == null ? null : chosen.toPath();
    }
    protected final StateStore states() {
        if (session == null) throw new IllegalStateException("Script is not attached to a view");
        return session.states();
    }
    /** Access a rendered JSONC component, for custom platform adapters. */
    protected final megalodonte.base.components.Component component(String id) {
        states();
        return session.component(id);
    }
    protected final String text(String name) { return states().string(name).get(); }
    protected final Object get(String name) { return states().get(name); }
    protected final <T> State<T> state(String name, Class<T> type) { return states().state(name, type); }
    protected final void set(String name, Object value) { states().set(name, value); }
    protected final void message(String text) { set("mensagem", text); }
    protected final void navigate(String destination) {
        states();
        navigator.accept(destination);
    }
    public abstract void onSubmit();
    public void onMount() {}
    public void onDestroy() {}
    /** Override to customize error presentation; default targets the string state 'mensagem'. */
    public void onValidationFailed(Map<String, String> errors) { message(errors.values().iterator().next()); }
}
