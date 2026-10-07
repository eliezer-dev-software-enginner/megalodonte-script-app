package megalodonte.script;

import megalodonte.base.state.State;
import megalodonte.jsonc.*;
import java.util.Map;
import java.util.function.Consumer;

/** A per-view Java script. onSubmit runs only after the host validates the JSONC rules. */
public abstract class ViewScript {
    private UiSession session;
    private Consumer<String> navigator;

    final void attach(UiSession session, Consumer<String> navigator) { this.session = session; this.navigator = navigator; }
    final void detach() { session = null; navigator = null; }
    protected final StateStore states() {
        if (session == null) throw new IllegalStateException("Script is not attached to a view");
        return session.states();
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
