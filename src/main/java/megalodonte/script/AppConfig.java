package megalodonte.script;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Declarative application setup. No client bootstrap class is needed. */
public record AppConfig(String name, String initialView, String scriptsPackage, String views,
                        int width, int height, int minWidth, int minHeight) {
    public AppConfig {
        Objects.requireNonNull(name); Objects.requireNonNull(views);
        ScriptResolver.classNameFor(initialView);
        if (scriptsPackage == null || !scriptsPackage.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*"))
            throw new IllegalArgumentException("app.scriptsPackage must be a Java package");
        if (!views.matches("/?[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+)*")) throw new IllegalArgumentException("app.views must be a resource directory");
        if (width <= 0 || height <= 0 || minWidth <= 0 || minHeight <= 0 || width < minWidth || height < minHeight)
            throw new IllegalArgumentException("Window dimensions must be positive and respect their minimums");
    }
    public static AppConfig load(ClassLoader loader) {
        try (var stream = loader.getResourceAsStream("application.properties")) {
            if (stream == null) throw new IllegalArgumentException("Missing application.properties on the application classpath");
            var props = new Properties();
            props.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            Set<String> allowed = Set.of("app.name", "app.initialView", "app.scriptsPackage", "app.views", "app.width", "app.height", "app.minWidth", "app.minHeight");
            for (String key : props.stringPropertyNames()) if (!allowed.contains(key)) throw new IllegalArgumentException("Unknown application property: " + key);
            return new AppConfig(props.getProperty("app.name", "Megalodonte"), props.getProperty("app.initialView", "login"),
                    props.getProperty("app.scriptsPackage"), props.getProperty("app.views", "/views"),
                    number(props, "app.width", 520), number(props, "app.height", 680),
                    number(props, "app.minWidth", 420), number(props, "app.minHeight", 640));
        } catch (IOException e) { throw new IllegalArgumentException("Cannot read application.properties", e); }
    }
    private static int number(Properties props, String key, int fallback) {
        try { return Integer.parseInt(props.getProperty(key, Integer.toString(fallback))); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(key + " must be an integer", e); }
    }
}
