buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.4.0") // atualizado para 8.4
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24") // atualizado para Kotlin compatível com Compose 1.5.14
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}