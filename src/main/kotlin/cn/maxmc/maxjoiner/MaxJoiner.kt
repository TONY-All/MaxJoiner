package cn.maxmc.maxjoiner

import org.bukkit.Bukkit
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.platform.BukkitPlugin
import java.util.*

@RuntimeDependency(
    "!org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.0-Beta",
    test = "kotlinx.coroutines.Job",
    relocate = ["!kotlin.", "!kotlin@kotlin_version_escape@."]
)
object MaxJoiner : Plugin() {
    @Config("settings.yml", autoReload = true)
    lateinit var settings: Configuration

    @Config("servers.yml")
    lateinit var servers: Configuration

    private val timer = Timer("Server Query")

    override fun onEnable() {
        registerCommand()
        debugCmd()
        Bukkit.getServer().messenger.registerOutgoingPluginChannel(BukkitPlugin.getInstance(), "BungeeCord")
        timer.schedule(object : TimerTask() {
            override fun run() {
                ServerManager.updatePing()
            }
        }, 0L, settings.getLong("update_delay"))
        ServerManager.loadCategories()
        servers.onReload {
            ServerManager.loadCategories()
        }
    }

    override fun onDisable() {
        timer.cancel()
    }
}