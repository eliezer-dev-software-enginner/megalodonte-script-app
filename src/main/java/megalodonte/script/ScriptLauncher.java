package megalodonte.script;

import megalodonte.application.MegalodonteApp;
import megalodonte.base.theme.ThemeManager;
import megalodonte.theme.DefaultTheme;

/** Shared entrypoint used by every script-only client application. */
public final class ScriptLauncher {
    private ScriptLauncher() {}
    public static void main(String[] args) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        AppConfig config = AppConfig.load(loader);
        ThemeManager.setTheme(new DefaultTheme());
        MegalodonteApp.appName(config.name());
        var app = new ScriptApplication(config, loader);
        MegalodonteApp.run(context -> {
            context.useView(app, config.width(), config.height());
            var stage = context.javafxStage();
            configureWindow(stage, config);
        }, event -> { if (event == MegalodonteApp.Event.CloseRequest) app.close(); });
    }
    static void configureWindow(javafx.stage.Stage stage, AppConfig config) {
        stage.setTitle(config.name());
        stage.setResizable(true);
        stage.setMaxWidth(Double.MAX_VALUE); stage.setMaxHeight(Double.MAX_VALUE);
        stage.setWidth(config.width()); stage.setHeight(config.height());
        stage.setMinWidth(config.minWidth()); stage.setMinHeight(config.minHeight());
    }
}
