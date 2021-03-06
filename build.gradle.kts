import Config.migrationCode
import Config.serverCode
import Config.tachideskVersion
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.moko.gradle) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.versions)
}

buildscript {
    dependencies {
        // Waiting on https://github.com/Guardsquare/proguard/issues/225
        classpath(libs.proguard)
    }
}

allprojects {
    group = "ca.gosyer"
    version = "1.3.0"

    dependencies {
        modules {
            module("androidx.lifecycle:lifecycle-viewmodel-ktx") {
                replacedBy("androidx.lifecycle:lifecycle-viewmodel")
            }
        }
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=compatibility",
            )
        }
    }
    tasks.withType<org.jmailen.gradle.kotlinter.tasks.LintTask> {
        source(files("src"))
        exclude("ca/gosyer/*/build")
    }
    tasks.withType<org.jmailen.gradle.kotlinter.tasks.FormatTask> {
        source(files("src"))
        exclude("ca/gosyer/*/build")
    }
    plugins.withType<com.android.build.gradle.BasePlugin> {
        configure<com.android.build.gradle.BaseExtension> {
            compileSdkVersion(31)
            defaultConfig {
                minSdk = 21
                targetSdk = 31
                /*versionCode(Config.versionCode)
                versionName(Config.versionName)
                ndk {
                    version = Config.ndk
                }*/
            }
            compileOptions {
                isCoreLibraryDesugaringEnabled = true
                sourceCompatibility(Config.androidJvmTarget)
                targetCompatibility(Config.androidJvmTarget)
            }
            sourceSets {
                named("main") {
                    val altManifest = file("src/androidMain/AndroidManifest.xml")
                    if (altManifest.exists()) {
                        manifest.srcFile(altManifest.path)
                    }
                }
            }
            dependencies {
                add("coreLibraryDesugaring", libs.desugarJdkLibs)
            }
            buildFeatures.apply {
                aidl = false
                renderScript = false
                shaders = false
            }
        }
    }
    plugins.withType<com.codingfeline.buildkonfig.gradle.BuildKonfigPlugin> {
        configure<com.codingfeline.buildkonfig.gradle.BuildKonfigExtension> {
            defaultConfigs {
                buildConfigField(Type.STRING, "NAME", rootProject.name)
                buildConfigField(Type.STRING, "VERSION", project.version.toString())
                buildConfigField(Type.INT, "MIGRATION_CODE", migrationCode.toString())
                buildConfigField(Type.BOOLEAN, "DEBUG", project.hasProperty("debugApp").toString())
                buildConfigField(Type.BOOLEAN, "IS_PREVIEW", project.hasProperty("preview").toString())
                buildConfigField(Type.INT, "PREVIEW_BUILD", project.properties["preview"]?.toString()?.trim('"') ?: 0.toString())

                // Tachidesk
                buildConfigField(Type.STRING, "TACHIDESK_SP_VERSION", tachideskVersion)
                buildConfigField(Type.INT, "SERVER_CODE", serverCode.toString())
            }
        }
    }
    plugins.withType<org.jmailen.gradle.kotlinter.KotlinterPlugin> {
        configure<org.jmailen.gradle.kotlinter.KotlinterExtension> {
            experimentalRules = true
            disabledRules = arrayOf("experimental:argument-list-wrapping", "experimental:trailing-comma")
        }
    }

    plugins.withType<com.google.devtools.ksp.gradle.KspGradleSubplugin> {
        configure<com.google.devtools.ksp.gradle.KspExtension> {
            arg("me.tatarka.inject.generateCompanionExtensions", "true")
            if (project.hasProperty("debugApp")) {
                arg("me.tatarka.inject.dumpGraph", "true")
            }
        }
    }

    plugins.withType<JacocoPlugin> {
        configure<JacocoPluginExtension> {
            toolVersion = "0.8.7"
        }
    }

    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper> {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            afterEvaluate {
                if (!Config.androidDev) {
                    sourceSets.addSrcDir("desktopMain", "src/jvmMain/kotlin")
                    sourceSets.addSrcDir("desktopTest", "src/jvmTest/kotlin")
                }
                sourceSets.addSrcDir("androidMain", "src/jvmMain/kotlin")
                sourceSets.addSrcDir("androidTest", "src/jvmTest/kotlin")
            }
        }
    }
}

fun NamedDomainObjectContainer<org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet>.addSrcDir(configuration: String, srcDir: String) {
    filter { it.name.contains(configuration) }
        .forEach {
            it.kotlin.srcDir(srcDir)
        }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.contains(it, true) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
