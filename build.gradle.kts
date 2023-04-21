plugins {
    id("io.izzel.taboolib") version "1.56"
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
}

group = "cn.maxmc.maxjoiner"
version = "1.3.4"

//sourceCompatibility = "1.8"
//targetCompatibility = "1.8"

taboolib {
    install("common")
    install("common-5")
    install("platform-bukkit")
    install("module-configuration")
    install("module-chat")
    install("module-lang")
    version = "6.0.10-114"
//    options("skip-kotlin-relocate")
    classifier = null
    description {
        bukkitApi("1.13")
        contributors {
            name("TONY_All")
        }
    }
}

repositories {
    maven("http://lss233.littleservice.cn/repositories/minecraft/") {
        isAllowInsecureProtocol = true
    }
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io/")
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
    implementation(kotlin("stdlib"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("ink.ptms:nms-all:1.0.0")
}