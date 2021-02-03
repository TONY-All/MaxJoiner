package cn.maxmc.maxjoiner.server

import cn.maxmc.maxjoiner.connect
import io.izzel.taboolib.module.locale.TLocale
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class CategoryListener(private val category: ServerCategory): Listener {

    @EventHandler
    fun onClick(e:InventoryClickEvent) {

        // filter
        if((e.inventory != category.getQuick()) && (e.inventory != category.getTotal())) {
            return
        }

        e.isCancelled = true

        if(e.currentItem == null || e.currentItem.type == Material.AIR) {
            return
        }

        if(e.currentItem == ServerCategory.show.build()) {
            e.whoClicked.openInventory(category.getTotal())
            return
        }

        if(e.currentItem == ServerCategory.back.build()) {
            e.whoClicked.openInventory(category.getQuick())
            return
        }

        if(e.currentItem == ServerCategory.join.build()) {
            val pollServer = category.pollServer()
            if (pollServer == null) {
                TLocale.sendTo(e.whoClicked,"msg.none_server")
                return
            }
            val player = e.whoClicked
            player as Player
            player.connect(pollServer)
            return
        }

        val server = category.itemServerMap[e.currentItem]

        val player = e.whoClicked
        player as Player

        if(server == null) {
            return
        }

        if(!server.currentState.isOnline) {
            TLocale.sendTo(player, "msg.cant_join")
            return
        }

        if(server.currentState.current >= server.currentState.max) {
            TLocale.sendTo(player, "msg.server_full")
            return
        }

        if(!server.canJoin && !(server.canSpectate)) {
            TLocale.sendTo(player, "msg.cant_join")
            return
        }

        player.connect(server)
    }
}