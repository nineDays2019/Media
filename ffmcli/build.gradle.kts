plugins {
    id("com.android.library")
}

android {
    compileSdkVersion(ext("compileSdkVersion") as Int)

    defaultConfig {

        minSdkVersion(ext("minSdkVersion") as Int)
        targetSdkVersion(ext("targetSdkVersion") as Int)
        versionCode = ext("globalVersionCode").toString().toInt()
        versionName = "${ext("globalVersionName")}"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

//        externalNativeBuild {
//            cmake {
//                cppFlags
//            }
//            ndk {
//                abiFilters("armeabi-v7a")
//            }
//        }

    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField(
                "int", "FFMPEG_VERSION",
                ext("ffmpegCliVersion").toString()
            )
        }
        getByName("debug") {
            buildConfigField(
                "int", "FFMPEG_VERSION",
                "-2"
            )   // 默认需要移动文件
        }
    }
    libraryVariants.all {
        val name = buildType.name
        if (name != com.android.builder.core.BuilderConstants.DEBUG) {
            // skip debug builds
            val task = tasks.create("jar${name.capitalize()}", Jar::class.java)
            task.dependsOn(javaCompileProvider)
            javaCompileProvider.configure{
                task.from(destinationDir)
            }
            artifacts.add("archives", task)
        }
    }
//    externalNativeBuild {
//        cmake {
//            setPath("CMakeLists.txt")
//        }
//    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation("junit:junit:${ext("junitVersion")}")
    androidTestImplementation("com.android.support.test:runner:${ext("runnerVersion")}")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:${ext("espressoVersion")}")
    implementation(project(":eternal"))
}

fun ext(key: String): Any? = rootProject.ext[key]

task("printVersionInfo") {
    println("versionCode: ${ext("globalVersionCode")}\tversionName: ${ext("globalVersionName")}")
}