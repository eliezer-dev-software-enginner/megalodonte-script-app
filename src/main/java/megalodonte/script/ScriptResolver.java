package megalodonte.script;

import megalodonte.jsonc.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.*;
import java.util.Objects;

/** Resolves 'login.view.jsonc' to scriptsPackage.Login, without scanning the classpath. */
public final class ScriptResolver {
    private final AppConfig config;
    private final ClassLoader loader;
    public record ResolvedView(UiDefinition definition, ViewScript script) {}
    public ScriptResolver(AppConfig config, ClassLoader loader) { this.config = Objects.requireNonNull(config); this.loader = Objects.requireNonNull(loader); }
    public static String classNameFor(String name) {
        if (name == null || !name.matches("[a-z][A-Za-z0-9]*(?:_[A-Za-z0-9]+)*"))
            throw new IllegalArgumentException("View name must be an identifier starting with a lowercase letter: " + name);
        var result = new StringBuilder();
        for (String part : name.split("_")) result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        return result.toString();
    }
    public ResolvedView resolve(String name) {
        String className = config.scriptsPackage() + "." + classNameFor(name);
        String directory = config.views().startsWith("/") ? config.views().substring(1) : config.views();
        String resource = directory + "/" + name + ".view.jsonc";
        UiDefinition definition;
        try (InputStream stream = loader.getResourceAsStream(resource)) {
            if (stream == null) throw new UiException(resource, "View resource not found");
            definition = UiDefinition.parse(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) { throw new UiException(resource, "Cannot read view", e); }
        if (!definition.id().equals(name)) throw new UiException(resource, "Document id must match view name: " + name);
        try {
            Class<?> type = Class.forName(className, false, loader);
            if (!ViewScript.class.isAssignableFrom(type)) throw new UiException(resource, className + " must extend ViewScript");
            ViewScript script = (ViewScript) type.getConstructor().newInstance();
            return new ResolvedView(definition, script);
        } catch (ReflectiveOperationException e) { throw new UiException(resource, "Expected public script " + className + " with a public no-argument constructor", e); }
    }
}
