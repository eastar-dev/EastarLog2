import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("com.vanniktech.maven.publish") version "0.28.0"
}

android {
    compileSdk = 34

    namespace = "dev.eastar.log"
    defaultConfig {
        minSdk = 26
        version = "2.4.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    //https://developer.android.com/studio/write/lint#snapshot
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.firebase.messaging.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

/**
 * jitpack with id("maven-publish")
 *
 * build to same
 * sh ./gradlew log:bundleReleaseAar
 *
 * https://developer.android.com/studio/build/maven-publish-plugin
 * run gradle publishToMavenLocal
 * and check
 *  cd ~/.m2/repository/com/example/MyLibrary/log
 * log-1.0.pom
 * log-1.0.aar
 * log-1.0.module
 *
 * Because the components are created only during the afterEvaluate phase, you must
 * configure your publications using the afterEvaluate() lifecycle method.
 */
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Applies the component for the release build variant.
                from(components["release"])
                groupId = "com.example.MyLibrary"
                artifactId = "log"
                version = project.version as String
            }
        }
    }
}

/**
 * Maven Central
 *
 * id("com.vanniktech.maven.publish") version "0.28.0"
 *
 * import com.vanniktech.maven.publish.SonatypeHost
 */

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("dev.eastar", "eastarlog2", "2.5.0")

    pom {
        //프로젝트 이름, 설명, URL
        name.set("EastarLog2")
        description.set("Simple Setting Log lib for Android.")
        inceptionYear.set("2017")
        url.set("https://github.com/eastar-dev/EastarLog2")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("username")
                name.set("User Name")
                url.set("https://github.com/username/")
            }
        }
        scm {
            url.set("https://github.com/username/mylibrary/")
            connection.set("scm:git:git://github.com/eastar-dev/EastarLog2.git")
            developerConnection.set("scm:git:git://github.com/eastar-dev/EastarLog2.git")
        }
    }
}