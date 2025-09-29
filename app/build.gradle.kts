plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.apollographql.apollo") version "4.3.3"
    id("com.google.devtools.ksp") version "2.2.20-2.0.3"
    kotlin("plugin.serialization") version "2.2.20"
}

android {
    namespace = "com.rangerscards"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rangerscards"
        minSdk = 24
        targetSdk = 36
        versionCode = 63
        versionName = "2.5.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        androidResources.localeFilters += listOf("en", "ru", "de", "fr", "it", "es")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    //Import splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-auth")

    // Import Apollo Kotlin
    implementation("com.apollographql.apollo:apollo-runtime:4.3.3")
    implementation("com.apollographql.apollo:apollo-normalized-cache:4.3.3")
    implementation("com.apollographql.apollo:apollo-normalized-cache-sqlite:4.3.3")

    //Import Room
    implementation("androidx.room:room-ktx:2.8.0")
    implementation("androidx.room:room-runtime:2.8.0")
    implementation("androidx.room:room-paging:2.8.0")
    ksp("androidx.room:room-compiler:2.8.0")

    //Import Paging
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")

    //Import Coil
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    //Import json serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    //Import charts
    implementation ("io.github.ehsannarmani:compose-charts:0.1.11")

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.navigation:navigation-compose:2.9.4")
    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

apollo {
    service("service") {
        // The package name for the generated models
        packageName.set("com.rangerscards")
        schemaFiles.from("src/main/graphql/schema.graphqls", "src/main/graphql/extra.graphqls")
        addTypename.set("always")
        mapScalarToKotlinString("timestamptz")
        mapScalar("jsonb", "kotlinx.serialization.json.JsonElement", "com.rangerscards.data.objects.JsonElementAdapter")
    }
}