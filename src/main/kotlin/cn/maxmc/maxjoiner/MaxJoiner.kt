package cn.maxmc.maxjoiner

import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.module.config.TConfig
import io.izzel.taboolib.module.dependency.TDependency
import io.izzel.taboolib.module.inject.TInject
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.scheduler.BukkitTask
import java.util.*

object MaxJoiner: Plugin() {
    @TInject("settings.yml",locale = "lang")
    lateinit var settings: TConfig

    @TInject("servers.yml")
    lateinit var servers: TConfig

    private val timer = Timer("Server Query")

    override fun onEnable() {
        Bukkit.getServer().messenger.registerOutgoingPluginChannel(this.plugin, "BungeeCord")
        TDependency.requestLib("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2",TDependency.MAVEN_REPO,"")
        timer.schedule(object : TimerTask() {
            override fun run() {
                ServerManager.updatePing()
            }
        },0L, settings.getLong("update_delay"))
        servers.listener {
            ServerManager.loadCategories()
        }
    }

    override fun onDisable() {
        timer.cancel()
    }
}