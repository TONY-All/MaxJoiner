package cn.maxmc.maxjoiner

import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.command
import taboolib.platform.util.sendLang

fun registerCommand() {
    command("maxJoiner", aliases = listOf("serverJoiner", "sj", "mj")) {
        commandQuick()
        commandGUI()
        commandList()
    }
}

fun CommandBuilder.CommandBase.commandQuick() = literal("quick", permission = "maxjoiner.quick") {
    dynamic {
        suggestion<Player> { _, _ ->
            ServerManager.categories.map { it.name }
        }

        execute<Player> { sender, _, arg ->
            val category = ServerManager.getCategoryByName(arg)

            if (!sender.hasPermission("maxjoiner.join.$arg")) {
                sender.sendLang("no_permission")
            }

            if (category == null) {
                sender.sendLang("null_category")
                return@execute
            }

            val pollServer = category.pollServer()
            if (pollServer == null) {
                sender.sendLang("none_server")
                return@execute
            }

            sender.connect(pollServer)
        }
    }
}

fun CommandBuilder.CommandBase.commandGUI() = literal("gui", permission = "maxjoiner.gui") {
    dynamic {
        suggestion<Player> { _, _ ->
            return@suggestion ServerManager.categories.map { it.name }
        }

        execute<Player> { sender, _, arg ->
            val category = ServerManager.getCategoryByName(arg)
            if (!sender.hasPermission("maxjoiner.join.$arg")) {
                sender.sendLang("msg.no_permission")
            }

            category?.openGui(sender) ?: sender.sendLang("msg.null_category")
        }
    }
}

fun CommandBuilder.CommandBase.commandList() = literal("list", permission = "maxjoiner.list") {
    execute<Player> { sender, _, _ ->
        sender.sendLang("list")
        ServerManager.categories.forEach {
            sender.sendMessage("§e${it.displayName}: §a${it.name}")
        }
    }
}