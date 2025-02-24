plugins {
    application
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
}

application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor core y Netty
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    // Plugin de content negotiation
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    // Serialización JSON con kotlinx
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.11")
    // Exposed y MariaDB
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.7")
    // HikariCP para conexión
    implementation("com.zaxxer:HikariCP:5.0.1")
    // Test
    testImplementation("io.ktor:ktor-server-tests:2.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.0")
    // BCrypt
    implementation("org.mindrot:jbcrypt:0.4")
    //Ktor Authentication y JWT
    implementation("io.ktor:ktor-server-auth:2.3.0")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.0")
    implementation("com.auth0:java-jwt:3.18.2")
}
