package megalodonte.script;

import megalodonte.application.MegalodonteApp;
import megalodonte.base.theme.ThemeManager;
import megalodonte.base.theme.ThemeInterface;
import megalodonte.theme.DefaultTheme;

/** Shared entrypoint used by every script-only client application. */
public final class ScriptLauncher {
    private ScriptLauncher() {}
    public static void main(String[] args) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        AppConfig config = AppConfig.load(loader);
        ThemeManager.setTheme(theme(config, loader));
        if (config.icon() != null) MegalodonteApp.appIcon(config.icon());
        MegalodonteApp.appName(config.name());
        var app = new ScriptApplication(config, loader);
        MegalodonteApp.run(context -> {
            context.useView(app, config.width(), config.height());
            var stage = context.javafxStage();
            configureWindow(stage, config);
        }, event -> { if (event == MegalodonteApp.Event.CloseRequest) app.close(); });
    }
    private static ThemeInterface theme(AppConfig config, ClassLoader loader) {
        if (config.themeClass() == null) return new DefaultTheme();
        try {
            return Class.forName(config.themeClass(), true, loader)
                    .asSubclass(ThemeInterface.class).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException error) {
            throw new IllegalArgumentException("Cannot load app.themeClass: " + config.themeClass(), error);
        }
    }
    static void configureWindow(javafx.stage.Stage stage, AppConfig config) {
        stage.setTitle(config.name());
        stage.setResizable(true);
        stage.setMaxWidth(Double.MAX_VALUE); stage.setMaxHeight(Double.MAX_VALUE);
        var bounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double width = Math.min(config.width(), bounds.getWidth());
        double height = Math.min(config.height(), bounds.getHeight());
        stage.setMinWidth(Math.min(config.minWidth(), width));
        stage.setMinHeight(Math.min(config.minHeight(), height));
        stage.setWidth(width); stage.setHeight(height);
        stage.setX(bounds.getMinX() + (bounds.getWidth() - width) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - height) / 2);
    }
}
