# Megalodonte Script App

The [codebase guide](docs/CODEBASE.md) maps the launcher and script APIs.

This library runs applications whose client code consists of one Java `ViewScript` per view. It uses `megalodonte-jsonc-ui` to validate and render `.view.jsonc` data, and supplies the launcher, action resolution, navigation and lifecycle management.

## Configure an application

Use `megalodonte:megalodonte-script-app:0.1.0`, Java 25 and JavaFX 25.0.1. The project uses the Gradle 9.2.0 wrapper. The build resolves `megalodonte:megalodonte-jsonc-ui:0.1.0` and `megalodonte:megalodonte-theme:1.0.0-beta` from an adjacent source checkout when present, or from the configured Maven repositories (`mavenLocal()` and Maven Central). These artifacts must be available to build this library; the repository does not include them. For a consuming Gradle application, add the library dependency and set `application.mainClass` to `megalodonte.script.ScriptLauncher`. During local development, `includeBuild("../megalodonte-script-app")` can substitute an adjacent checkout of this library.

Create `src/main/resources/application.properties` (UTF-8):

```properties
app.name=Example Application
app.initialView=login
app.scriptsPackage=example.app.scripts
app.views=/views
app.width=520
app.height=680
app.minWidth=420
app.minHeight=640
# Optional: enable only after providing the resource or implementation:
# app.icon=/assets/app.png
# app.themeClass=example.app.MyTheme
```

Dimensions are window pixels. The script package is required. Other properties have defaults; the default application name is Megalodonte. The launcher initializes the theme, icon and view host. If enabled, `app.icon` must refer to a PNG or JPEG classpath resource and `app.themeClass` to an available `ThemeInterface` implementation. Without `app.themeClass` it uses `DefaultTheme`; without `app.icon` it keeps the platform icon.

## Add a screen

1. Add `views/login.view.jsonc` with `id: "login"`, local states and the component tree.
2. Add public `example.app.scripts.Login extends ViewScript` with a public no-argument constructor.
3. Put domain operations in public `void` methods such as `onSubmit()`.

There is no manual screen registry. Navigation loads destinations by name: `login_screen.view.jsonc` resolves `LoginScreen`, and `login` resolves `Login`. Every session receives a new script instance. Java source is compiled normally; no Java interpretation or hot reload occurs.

### Actions and state

- `onSubmit` validates inputs before invoking the script.
- `navigate_signup` opens `signup.view.jsonc` and resolves `Signup.java`.
- Another action name resolves a public, no-argument, `void` instance method in the current script.

Use `events.onClick` on buttons and `events.onEnter` on supported inputs. `onSubmit`, the `navigate_` prefix and lifecycle callbacks are reserved. The runtime rejects `Object` methods, base helpers and lifecycle callbacks as ordinary actions. Only `onSubmit` receives automatic validation.

```java
String email = text("email");
Object value = get("total");
State<BigDecimal> total = state("total", BigDecimal.class);
set("mensagem", "Ready");
message("Ready"); // shortcut for the string state named mensagem
navigate("signup");
```

The default validation handler writes to the string state `mensagem`; the view must declare and display it. Override `onValidationFailed(Map<String,String>)` to change that behavior. Capture state values before long-running work; updates to a mounted view and navigation belong on the JavaFX thread.

### Lifecycle and asynchronous work

`onMount()` runs after mounting; `onDestroy()` runs before disposal. Navigation retains the window and releases previous bindings, theme subscriptions, event handlers and script. A destination that fails to load or render leaves the current view intact. Reopening a view creates fresh local state. `ScriptApplication` can also be hosted by an existing application.

`background(Callable<T>, Consumer<T> success, Consumer<Exception> failure)` runs blocking work in a per-view queue backed by a virtual thread. Callbacks return to the UI thread. Disposing the view interrupts work and ignores pending callbacks. `chooseDirectory(title, initialPath)` opens the framework-owned native directory picker and returns a `Path` or `null`, so consumer scripts do not import JavaFX.

To run this repository's tests and publish its artifact to your local Maven repository, from the repository root:

```sh
./gradlew test publishToMavenLocal
```

No license file is present in this repository; licensing terms require confirmation from the maintainers.
