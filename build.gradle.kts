plugins {
    id("io.izzel.taboolib") version "1.40"
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
}

group = "cn.maxmc.maxjoiner"
version = "1.2.0"

//sourceCompatibility = "1.8"
//targetCompatibility = "1.8"

taboolib {
    install("common")
    install("common-5")
    install("platform-bukkit")
    version = "6.0.9-25"
//    options("skip-kotlin-relocate")
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
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
    implementation(kotlin("stdlib"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("ink.ptms:nms-all:1.0.0")
}