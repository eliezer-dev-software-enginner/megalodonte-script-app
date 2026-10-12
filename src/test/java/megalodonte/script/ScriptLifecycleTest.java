package megalodonte.script;

import javafx.application.Platform;
import megalodonte.base.theme.ThemeManager;
import megalodonte.theme.DefaultTheme;
import megalodonte.script.fixtures.*;
import megalodonte.jsonc.UiException;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ScriptLifecycleTest {
    @Test void declarativeNavigationMountsScriptsAndDisposesPreviousSession() throws Exception {
        var started = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); started.countDown(); });
        assertTrue(started.await(15, TimeUnit.SECONDS));
        var task = new FutureTask<Void>(() -> {
            ThemeManager.setTheme(new DefaultTheme());
            var stage = new javafx.stage.Stage();
            stage.setResizable(false); stage.setMaxWidth(600); stage.setMaxHeight(500);
            ScriptLauncher.configureWindow(stage,ScriptApplicationTest.config());
            assertTrue(stage.isResizable()); assertEquals(Double.MAX_VALUE,stage.getMaxWidth());
            assertEquals(Double.MAX_VALUE,stage.getMaxHeight());
            var visual = javafx.stage.Screen.getPrimary().getVisualBounds();
            assertTrue(stage.getX() >= visual.getMinX());
            assertTrue(stage.getY() >= visual.getMinY());
            assertTrue(stage.getX() + stage.getWidth() <= visual.getMaxX() + 1);
            assertTrue(stage.getY() + stage.getHeight() <= visual.getMaxY() + 1);
            stage.setScene(new javafx.scene.Scene(new javafx.scene.layout.StackPane(),520,680));
            stage.show(); stage.setMaximized(true); assertTrue(stage.isMaximized()); stage.close();
            TestPage.mounts = 0; TestPage.destroys = 0; NextPage.mounts = 0; NextPage.destroys = 0;
            var resources = Map.of("views/test_page.view.jsonc", ScriptApplicationTest.view("test_page", "navigate_next_page"),
                    "views/next_page.view.jsonc", ScriptApplicationTest.view("next_page", "navigate_test_page"));
            var app = new ScriptApplication(ScriptApplicationTest.config(), ScriptApplicationTest.resources(resources));
            var root = app.render();
            app.onMount(); app.onMount();
            assertEquals(1, TestPage.mounts);
            var previous = app.current();
            assertThrows(UiException.class, () -> app.navigate("absent"));
            assertSame(previous, app.current());
            previous.invoke("navigate_next_page");
            assertEquals("next_page", app.current().id());
            assertEquals(1, TestPage.destroys); assertEquals(1, NextPage.mounts);
            assertThrows(IllegalStateException.class, previous::states);
            assertSame(root, app.render());
            app.current().invoke("navigate_test_page");
            assertEquals(1, NextPage.destroys); assertEquals(2, TestPage.mounts);
            app.close(); app.close(); assertEquals(2, TestPage.destroys);
            return null;
        });
        try { Platform.runLater(task); task.get(20, TimeUnit.SECONDS); }
        finally { Platform.exit(); }
    }
}
