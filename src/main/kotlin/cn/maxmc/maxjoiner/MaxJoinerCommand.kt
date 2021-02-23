package cn.maxmc.maxjoiner

import io.izzel.taboolib.module.command.base.*
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@BaseCommand(name = "maxJoiner",aliases = ["sj","serverjoiner","maxjoiner","maxjoin","queue","mj"])
class MaxJoinerCommand: BaseMainCommand() {

    @SubCommand(permission = "maxjoiner.quick",requiredPlayer = true,arguments = ["category"], description = "quick join a category")
    val quick = object : BaseSubCommand() {
        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("category",true, CommandTab {
                return@CommandTab ServerManager.categories.map { it.name }
            }))
        }

        override fun onCommand(sender: CommandSender, cmd: Command, lab: String, args: Array<out String>) {
            sender as Player

            val categoryName = args[0]
            val category = ServerManager.getCategoryByName(categoryName)

            if(!sender.hasPermission("maxjoiner.join.${categoryName}")) {
                TLocale.sendTo(sender,"msg.no_permission")
            }

            if(category == null) {
                TLocale.sendTo(sender,"msg.null_category")
                return
            }

            val pollServer = category.pollServer()
            if (pollServer == null) {
                TLocale.sendTo(sender,"msg.none_server")
                return
            }

            sender.connect(pollServer)
        }
    }

    @SubCommand(permission = "maxjoiner.gui", requiredPlayer = true, arguments = ["category"], description = "open the gui of a category")
    val gui = object : BaseSubCommand() {
        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("category",true, CommandTab {
                return@CommandTab ServerManager.categories.map { it.name }
            }))
        }

        override fun onCommand(sender: CommandSender, cmd: Command, lab: String, args: Array<out String>) {
            sender as Player

            val categoryName = args[0]
            val category = ServerManager.getCategoryByName(categoryName)

            if(!sender.hasPermission("maxjoiner.join.${categoryName}")) {
                TLocale.sendTo(sender,"msg.no_permission")
            }

            if(category == null) {
                TLocale.sendTo(sender,"msg.null_category")
                return
            }

            category.openGui(sender)

        }
    }

    @SubCommand(permission = "maxjoiner.list", description = "show all categories")
    val list = object : BaseSubCommand() {
        override fun onCommand(sender: CommandSender, cmd: Command, lab: String, args: Array<out String>) {
            TLocale.sendTo(sender,"msg.list")
            ServerManager.categories.forEach {
                sender.sendMessage("§e${it.displayName}: §a${it.name}")
            }
        }
    }

}