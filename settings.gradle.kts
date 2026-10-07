rootProject.name = "megalodonte-script-app"
val localSources = providers.gradleProperty("useLocalSources").orNull != "false"
val jsoncUi = file("../megalodonte-jsonc-ui")
if (localSources && jsoncUi.isDirectory) includeBuild(jsoncUi)
val theme = file("../../megalodonte-ecossystem/megalodonte-libs/megalodonte-theme")
if (localSources && theme.isDirectory) includeBuild(theme)
