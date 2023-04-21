package cn.maxmc.maxjoiner

import org.bukkit.command.CommandSender
import taboolib.common.platform.command.command

fun debugCmd() = command("updatePing") {
    execute<CommandSender> { _, _, _ ->
        ServerManager.updatePing()
    }
}