package cn.maxmc.maxjoiner

import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

class PAPIHook : PlaceholderExpansion {
    override val identifier: String = "maxjoiner"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        val split = args.split("_")
        if (split.size != 2) {
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