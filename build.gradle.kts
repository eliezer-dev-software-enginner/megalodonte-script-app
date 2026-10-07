plugins {
    `java-library`
    `maven-publish`
    id("org.openjfx.javafxplugin") version "0.1.0"
}
group = "megalodonte"
version = "0.1.0"
repositories { mavenLocal(); mavenCentral() }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }; withSourcesJar() }
javafx { version = "25.0.1"; modules("javafx.controls", "javafx.graphics") }
dependencies {
    api("megalodonte:megalodonte-jsonc-ui:0.1.0")
    implementation("megalodonte:megalodonte-theme:1.0.0-beta")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test { useJUnitPlatform(); jvmArgs("--enable-native-access=ALL-UNNAMED") }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
publishing { publications { create<MavenPublication>("mavenJava") { from(components["java"]) } } }
