rootProject.name = "megalodonte-script-app"
includeBuild("../megalodonte-jsonc-ui")
val theme = file("../../megalodonte-ecossystem/megalodonte-libs/megalodonte-theme")
if (theme.isDirectory) includeBuild(theme)
