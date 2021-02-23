package cn.maxmc.maxjoiner

import io.izzel.taboolib.module.compat.PlaceholderHook
import io.izzel.taboolib.module.inject.THook
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

@THook
class PAPIHook: PlaceholderHook.Expansion {
    override fun plugin(): Plugin = MaxJoiner.plugin

    override fun identifier(): String = "maxjoiner"

    override fun onPlaceholderRequest(player: Player, str: String): String {
        val split = str.split("_")
        if(split.size != 2) {
            return ""
        }
        when (split[0]) {
            "current" -> {
                val serverCategory = ServerManager.getCategoryByName(split[1]) ?: return ""
                return serverCategory.currentPlayer.toString()
            }
            "max" -> {
                val serverCategory = ServerManager.getCategoryByName(split[1]) ?: return ""
                return serverCategory.maxPlayer.toString()
            }
            else -> {
                return ""
            }
        }

    }
}