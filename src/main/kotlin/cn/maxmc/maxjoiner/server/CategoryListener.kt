package cn.maxmc.maxjoiner.server

import cn.maxmc.maxjoiner.connect
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import taboolib.platform.util.sendLang

class CategoryListener(private val category: ServerCategory) : Listener {

    @EventHandler
    fun onClick(e: InventoryClickEvent) {

        // filter
        if ((e.inventory != category.getQuick()) && (e.inventory != category.getTotal())) {
            return
        }

        e.isCancelled = true

        if (e.currentItem == null || e.currentItem.type == Material.AIR) {
            return
        }

        if (e.currentItem == ServerCategory.show.build()) {
            e.whoClicked.openInventory(category.getTotal())
            return
        }

        if (e.currentItem == ServerCategory.back.build()) {
            e.whoClicked.openInventory(category.getQuick())
            return
        }

        if (e.currentItem == ServerCategory.join.build()) {
            val pollServer = category.pollServer()
            if (pollServer == null) {
                e.whoClicked.sendLang("msg.none_server")
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

        if (server == null) {
            return
        }

        if (!server.currentState.isOnline) {
            player.sendLang("cant_join")
            return
        }

        if (server.currentState.current >= server.currentState.max) {
            player.sendLang("server_full")
            return
        }

        if (!server.canJoin && !(server.canSpectate)) {
            player.sendLang("cant_join")
            return
        }

        player.connect(server)
    }
}