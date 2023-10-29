plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "io.github.mnesimiyilmaz"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.mnesimiyilmaz:sql4json:0.0.2")
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}

intellij {
    version.set("2022.2.5")
    type.set("IC")
    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

}
