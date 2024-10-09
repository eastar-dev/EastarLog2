plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
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

/*
build to same
sh ./gradlew log:bundleReleaseAar
*/
//https://developer.android.com/studio/build/maven-publish-plugin
//run gradle publishToMavenLocal
//and check
// cd ~/.m2/repository/com/example/MyLibrary/log
//log-1.0.pom
//log-1.0.aar
//log-1.0.module

// Because the components are created only during the afterEvaluate phase, you must
// configure your publications using the afterEvaluate() lifecycle method.
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
