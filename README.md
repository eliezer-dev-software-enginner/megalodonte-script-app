# Megalodonte Script App

The [codebase guide](docs/CODEBASE.md) maps the launcher and script APIs.

This library runs applications whose client code consists of one Java `ViewScript` per view. It uses `megalodonte-jsonc-ui` to validate and render `.view.jsonc` data, and supplies the launcher, action resolution, navigation and lifecycle management.

## Configure an application

Use `megalodonte:megalodonte-script-app:0.1.0`, Java 25 and JavaFX 25.0.1. The project uses the Gradle 9.2.0 wrapper. The build resolves `megalodonte:megalodonte-jsonc-ui:0.1.0` and `megalodonte:megalodonte-theme:1.0.0-beta` from an adjacent source checkout when present, or from the configured Maven repositories (`mavenLocal()` and Maven Central). These artifacts must be available to build this library; the repository does not include them. This repository publishes the library to Maven Local with `./gradlew publishToMavenLocal` once its dependencies resolve. During local development, `includeBuild("../megalodonte-script-app")` can substitute an adjacent checkout of this library.

For a separate Gradle application using the published library, configure `build.gradle.kts` as follows. The library and its transitive dependencies must be available in the listed repositories:

```kotlin
plugins {
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("megalodonte:megalodonte-script-app:0.1.0")
}

application {
    mainClass.set("megalodonte.script.ScriptLauncher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
```

JavaFX is a dependency of this library. After adding the resources and script below, run the consuming application with Gradle's `run` task.

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

1. Add `src/main/resources/views/login.view.jsonc` with `id: "login"`, local states and the component tree.
2. Add public `src/main/java/example/app/scripts/Login.java` extending `ViewScript` with a public no-argument constructor.
3. Put domain operations in public `void` methods such as `onSubmit()`.

For example, these two files provide a required input and a validated submit action:

```jsonc
{
  "version": 1,
  "id": "login",
  "states": {
    "name": { "type": "string", "initial": "" },
    "mensagem": { "type": "string", "initial": "" }
  },
  "view": {
    "type": "Column",
    "children": [
      { "type": "Input", "bind": { "value": "name" }, "validation": { "required": true } },
      { "type": "Button", "props": { "text": "Submit" }, "events": { "onClick": "onSubmit" } }
    ]
  }
}
```

```java
package example.app.scripts;

import megalodonte.script.ViewScript;

public class Login extends ViewScript {
    @Override public void onSubmit() {
        message("Hello, " + text("name"));
    }
}
```

The example declares `mensagem` because the default validation handler and `message(...)` write to that state. Add a component bound to `mensagem` if you want to display the text in the view.

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

The default validation handler writes to the string state `mensagem`; the view must declare it and bind a component to display it. Override `onValidationFailed(Map<String,String>)` to change that behavior. Capture state values before long-running work; updates to a mounted view and navigation belong on the JavaFX thread.

### Lifecycle and asynchronous work

`onMount()` runs after mounting; `onDestroy()` runs before disposal. Navigation retains the window and releases previous bindings, theme subscriptions, event handlers and script. A destination that fails to load or render leaves the current view intact. Reopening a view creates fresh local state. `ScriptApplication` can also be hosted by an existing application.

`background(Callable<T>, Consumer<T> success, Consumer<Exception> failure)` runs blocking work in a per-view queue backed by a virtual thread. Callbacks return to the UI thread. Disposing the view interrupts work and ignores pending callbacks. `chooseDirectory(title, initialPath)` opens the framework-owned native directory picker and returns a `Path` or `null`, so consumer scripts do not import JavaFX.

To run this repository's tests and publish its artifact to your local Maven repository, from the repository root:

```sh
./gradlew test publishToMavenLocal
```

No license file is present in this repository; licensing terms require confirmation from the maintainers.
