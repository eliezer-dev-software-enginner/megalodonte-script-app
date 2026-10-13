# Script App codebase

| Source | Responsibility |
| --- | --- |
| [`AppConfig`](../src/main/java/megalodonte/script/AppConfig.java) | Parse application properties and view/script package settings. |
| [`ScriptLauncher`](../src/main/java/megalodonte/script/ScriptLauncher.java) | Standard Java main class for script-only applications. |
| [`ScriptApplication`](../src/main/java/megalodonte/script/ScriptApplication.java) | Host JSONC sessions, theme, navigation and lifecycle. |
| [`ScriptResolver`](../src/main/java/megalodonte/script/ScriptResolver.java) | Resolve compiled scripts and allowed action methods by view/event name. |
| [`ViewScript`](../src/main/java/megalodonte/script/ViewScript.java) | Client domain-action base class, state helpers, validation callbacks and background work. |

The [README](../README.md) describes the application contract. Views remain JSONC resources; client Java code contains actions and services, not component construction. Long-running operations use `ViewScript.background`, and view disposal cancels pending callbacks.
