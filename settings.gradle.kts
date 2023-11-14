pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.60.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        val hasLicense = extra.has("charts.kt-customer-id")

        if (hasLicense) {
            maven(url = "https://maven.pkg.jetbrains.space/data2viz/p/charts-1-r/maven") {
                content { includeGroup("io.data2viz.charts") }
                credentials {
                    username = extra["charts.kt-customer-id"] as String
                    password = extra["charts.kt-customer-key"] as String
                }
            }
        } else {
            maven(url = "https://maven.pkg.jetbrains.space/data2viz/p/maven/public") {
                content { includeGroup("io.data2viz.charts") }
            }
        }
        mavenLocal()
    }
}

rootProject.name = "Charts.kt-samples"

include(":androidApp")
include(":shared")
