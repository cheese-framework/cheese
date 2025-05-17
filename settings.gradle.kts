pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { setUrl("https://developer.huawei.com/repo/") }
        maven { setUrl("https://repo1.maven.org/maven2/") }
    }
}

rootProject.name = "cheese"
include(":app:debug")
include(":core")
include(":frontend:javascript")
include(":ncnn")
include(":ocr")
include(":mlkit")
include(":opencv")
include(":shared")
//include(":termux")
include(":app:release")
