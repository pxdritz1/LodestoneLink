plugins {
    kotlin("jvm") version "2.3.10"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.zyverasystems"
version = "1.0.2"

val minecraftApiVersion = "1.21.6-R0.1-SNAPSHOT"
val foliaSchedulerVersion = "0.7.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftApiVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.cjcrafter:foliascheduler:$foliaSchedulerVersion")
    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:$minecraftApiVersion")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.6")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier = ""
}
