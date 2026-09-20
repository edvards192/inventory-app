
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.statusPages)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    

    implementation(libs.postgresql)

    implementation(libs.logback.classic)

    implementation(libs.hikari)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)

    implementation(ktorLibs.server.callLogging)

    // BCrypt password hashing
    implementation("at.favre.lib:bcrypt:0.10.2")

    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
}
