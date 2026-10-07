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
            context.useView(app);
            var stage = context.javafxStage();
            stage.setTitle(config.name());
            stage.setWidth(config.width()); stage.setHeight(config.height());
            stage.setMinWidth(config.minWidth()); stage.setMinHeight(config.minHeight());
        }, event -> { if (event == MegalodonteApp.Event.CloseRequest) app.close(); });
    }
}
