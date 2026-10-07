package megalodonte.script;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import megalodonte.base.components.*;
import megalodonte.jsonc.*;
import java.lang.reflect.*;

/** Reusable screen host: action registration, validation, navigation and script lifecycle. */
public final class ScriptApplication implements ScreenComponent, AutoCloseable {
    private final ScriptResolver resolver;
    private Active current;
    private StackPane host;
    private Component root;
    private boolean closed;
    private static final class Active {
        final UiSession session;
        final ViewScript script;
        boolean mounted;
        Active(UiSession session, ViewScript script) { this.session = session; this.script = script; }
        void mount() { if (!mounted) { mounted = true; script.onMount(); } }
        void close() {
            try { if (mounted) script.onDestroy(); }
            finally { try { session.close(); } finally { script.detach(); } }
        }
    }
    public ScriptApplication(AppConfig config, ClassLoader loader) {
        resolver = new ScriptResolver(config, loader);
        current = open(config.initialView());
    }
    private void ensureOpen() { if (closed) throw new IllegalStateException("Application is closed"); }
    public UiSession current() { ensureOpen(); return current.session; }
    private Active open(String name) {
        var resolved = resolver.resolve(name);
        var script = resolved.script();
        var actions = new Actions();
        for (String action : resolved.definition().actionNames()) {
            if (action.equals("onSubmit")) actions.register(action, session -> {
                var errors = session.validate();
                if (errors.isEmpty()) script.onSubmit(); else script.onValidationFailed(errors);
            });
            else if (action.startsWith("navigate_")) {
                String destination = action.substring("navigate_".length());
                ScriptResolver.classNameFor(destination);
                actions.register(action, session -> navigate(destination));
            } else {
                Method method = actionMethod(script, action);
                actions.register(action, session -> invoke(script, method));
            }
        }
        var session = resolved.definition().open(actions);
        script.attach(session, this::navigate);
        return new Active(session, script);
    }
    private static Method actionMethod(ViewScript script, String name) {
        try {
            Method method = script.getClass().getMethod(name);
            if (method.getReturnType() != void.class || Modifier.isStatic(method.getModifiers())
                    || method.getDeclaringClass() == ViewScript.class || method.getDeclaringClass() == Object.class
                    || name.equals("onMount") || name.equals("onDestroy")) throw new NoSuchMethodException();
            return method;
        } catch (NoSuchMethodException e) { throw new UiException(script.getClass().getName(), "Expected public void " + name + "() script action", e); }
    }
    private static void invoke(ViewScript script, Method method) {
        try { method.invoke(script); }
        catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException error) throw error;
            if (e.getCause() instanceof Error error) throw error;
            throw new IllegalStateException("Script action failed: " + method.getName(), e.getCause());
        } catch (IllegalAccessException e) { throw new IllegalStateException("Script action is inaccessible", e); }
    }
    @Override public Component render() {
        ensureOpen(); requireFx();
        if (root == null) {
            host = new StackPane(current.session.render().getJavaFxNode());
            root = Component.CreateFromJavaFxNode(host);
        }
        return root;
    }
    @Override public void onMount() { ensureOpen(); requireFx(); current.mount(); }
    public void navigate(String destination) {
        ensureOpen(); requireFx();
        if (current.session.id().equals(destination)) return;
        var next = open(destination);
        try {
            var node = next.session.render().getJavaFxNode();
            if (host == null) render();
            host.getChildren().setAll(node);
        } catch (RuntimeException error) { next.close(); throw error; }
        var previous = current;
        current = next;
        try { previous.close(); } finally { next.mount(); }
    }
    private static void requireFx() { if (!Platform.isFxApplicationThread()) throw new IllegalStateException("Use the JavaFX application thread"); }
    @Override public void onDestroy() { close(); }
    @Override public void close() {
        if (closed) return;
        if (host != null) requireFx();
        closed = true;
        try { current.close(); }
        finally { if (host != null) host.getChildren().clear(); current = null; host = null; root = null; }
    }
}
