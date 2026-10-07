package megalodonte.script;

import megalodonte.jsonc.UiException;
import megalodonte.script.fixtures.TestPage;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ScriptApplicationTest {
    static AppConfig config() { return new AppConfig("Test", "test_page", "megalodonte.script.fixtures", "/views", 520, 680, 420, 640); }
    static String view(String id, String action) {
        return """
                {"version":1,"id":"%s","states":{
                   "name":{"type":"string","initial":""},"mensagem":{"type":"string","initial":""}},
                 "view":{"type":"Column","children":[
                   {"type":"Input","bind":{"value":"name"},"validation":{"required":true}},
                   {"type":"Button","props":{"text":"Submit"},"events":{"onClick":"%s"}}]}}
                """.formatted(id, action);
    }
    static ClassLoader resources(Map<String, String> resources) {
        return new ClassLoader(TestPage.class.getClassLoader()) {
            @Override public InputStream getResourceAsStream(String name) {
                String value = resources.get(name);
                return value == null ? null : new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
    @BeforeEach void reset() { TestPage.submissions = 0; TestPage.actions = 0; }
    @Test void conventionAndValidationGateRequireNoManualRegistration() {
        var loader = resources(Map.of("views/test_page.view.jsonc", view("test_page", "onSubmit")));
        var resolved = new ScriptResolver(config(), loader).resolve("test_page");
        assertInstanceOf(TestPage.class, resolved.script());
        assertEquals("Login", ScriptResolver.classNameFor("login"));
        assertEquals("LoginScreen", ScriptResolver.classNameFor("login_screen"));
        try (var app = new ScriptApplication(config(), loader)) {
            app.current().invoke("onSubmit");
            assertEquals(0, TestPage.submissions);
            assertTrue(app.current().states().string("mensagem").get().contains("obrigatório"));
            app.current().states().string("name").set("Maria");
            app.current().invoke("onSubmit");
            assertEquals(1, TestPage.submissions);
            assertEquals("submitted Maria", app.current().states().get("mensagem"));
        }
    }
    @Test void customPublicNoArgumentActionIsResolvedAutomatically() {
        var loader = resources(Map.of("views/test_page.view.jsonc", view("test_page", "doWork")));
        try (var app = new ScriptApplication(config(), loader)) {
            app.current().invoke("doWork");
            assertEquals(1, TestPage.actions);
        }
    }
    @Test void rejectsMissingViewsScriptsMismatchedIdsAndInvalidNames() {
        var resolver = new ScriptResolver(config(), resources(Map.of(
                "views/test_page.view.jsonc", view("other", "onSubmit"),
                "views/missing_script.view.jsonc", view("missing_script", "onSubmit"))));
        assertThrows(UiException.class, () -> resolver.resolve("absent"));
        assertThrows(UiException.class, () -> resolver.resolve("missing_script"));
        assertThrows(UiException.class, () -> resolver.resolve("test_page"));
        for (String name : new String[]{"../login", "Login", "login.view.jsonc", "login__screen"})
            assertThrows(IllegalArgumentException.class, () -> ScriptResolver.classNameFor(name));
    }
    @Test void rejectsUnknownActionsAndLifecycleMethodsAsEvents() {
        for (String action : new String[]{"missing", "getClass", "onMount", "onDestroy"}) {
            var loader = resources(Map.of("views/test_page.view.jsonc", view("test_page", action)));
            assertThrows(UiException.class, () -> new ScriptApplication(config(), loader));
        }
    }
    @Test void configHasUsefulFailuresAndUtf8Names() {
        var loader = resources(Map.of("application.properties", "app.name=Aplicação\napp.scriptsPackage=example.scripts\n"));
        assertEquals("Aplicação", AppConfig.load(loader).name());
        assertThrows(IllegalArgumentException.class, () -> AppConfig.load(resources(Map.of())));
        assertThrows(IllegalArgumentException.class, () -> AppConfig.load(resources(Map.of("application.properties", "app.scriptsPackage=p\napp.wdth=100"))));
        assertThrows(IllegalArgumentException.class, () -> AppConfig.load(resources(Map.of("application.properties", "app.scriptsPackage=p\napp.width=small"))));
    }
}
