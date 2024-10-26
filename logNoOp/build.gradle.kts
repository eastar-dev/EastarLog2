import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.vanniktech.maven)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    namespace = "dev.eastar.log"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        version = libs.versions.version.get()
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
                artifactId = "log-no-op"
                version = project.version as String
            }
        }
    }
}

/**
 * Maven Central
 *
 * id("com.vanniktech.maven.publish") version "0.30.0"
 *
 * import com.vanniktech.maven.publish.SonatypeHost
 */

mavenPublishing {
    // 기본 Maven Central로 배포
    //publishToMavenCentral(SonatypeHost.DEFAULT)

    // 또는 https://s01.oss.sonatype.org로 배포
    //publishToMavenCentral(SonatypeHost.S01)

    // 또는 https://central.sonatype.com/를 사용하는 Central Portal로 배포
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // 모든 배포에 대해 GPG 서명 활성화
    signAllPublications()

    coordinates("dev.eastar", "log-no-op", libs.versions.version.get())

    pom {
        //프로젝트 이름, 설명, URL
        name.set("EastarLog2")// 라이브러리 이름
        description.set("Simple Setting Log lib for Android.")// 라이브러리 설명
        inceptionYear.set("2017") // 프로젝트 시작 연도
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
                id.set("eastar")
                name.set("eastar Jeong")
                url.set("https://git.eastar.dev")
            }
        }
        scm {
            url.set("https://github.com/username/mylibrary/")
            connection.set("scm:git:git://github.com/eastar-dev/EastarLog2.git")
            developerConnection.set("scm:git:git://github.com/eastar-dev/EastarLog2.git")
        }
    }
}