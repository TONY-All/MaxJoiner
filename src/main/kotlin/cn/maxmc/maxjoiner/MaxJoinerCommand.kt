package cn.maxmc.maxjoiner

import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseMainCommand
import io.izzel.taboolib.module.command.base.BaseSubCommand
import io.izzel.taboolib.module.command.base.SubCommand
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@BaseCommand(name = "maxJoiner",aliases = ["sj","serverjoiner","maxjoiner","maxjoin","queue","mj"])
class MaxJoinerCommand: BaseMainCommand() {

    @SubCommand(permission = "maxjoiner.quick",requiredPlayer = true,arguments = ["category"])
    val quick = object : BaseSubCommand() {
        override fun onCommand(sender: CommandSender, cmd: Command, lab: String, args: Array<out String>) {
            sender as Player

            val categoryName = args[0]
            val category = ServerManager.getCategoryByName(categoryName)

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

    @SubCommand(permission = "maxjoiner.gui", requiredPlayer = true, arguments = ["category"])
    val gui = object : BaseSubCommand() {
        override fun onCommand(sender: CommandSender, cmd: Command, lab: String, args: Array<out String>) {
            sender as Player

            val categoryName = args[0]
            val category = ServerManager.getCategoryByName(categoryName)

            if(category == null) {
                TLocale.sendTo(sender,"msg.null_category")
                return
            }

            category.openGui(sender)

        }
    }


}