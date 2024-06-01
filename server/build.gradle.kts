plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "in.shrtl.app"
version = "1.0.0"
application {
    mainClass.set("in.shrtl.app.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}
ktor {
    fatJar {
        archiveFileName.set("server-$version.jar")
    }
}

dependencies {
    implementation(libs.bundles.exposed)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.logging)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.logback)
    implementation(libs.postgresql)
    implementation(libs.slf4j.simple)
    implementation(projects.shared)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.server.tests)
}
