pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

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
