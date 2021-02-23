package cn.maxmc.maxjoiner.server

import cn.maxmc.maxjoiner.MaxJoiner
import io.izzel.taboolib.util.item.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CopyOnWriteArrayList

data class ServerCategory(
    val name: String,
    val displayName: String,
    val joinable: CopyOnWriteArrayList<String>,
    val spectatable: CopyOnWriteArrayList<String>,
) {
    var servers: CopyOnWriteArrayList<Server> = CopyOnWriteArrayList()
    private set
    constructor(
        name: String,
        displayName: String,
        joinable: CopyOnWriteArrayList<String>,
        spectatable: CopyOnWriteArrayList<String>,
        inputSer: List<Server>
        ): this(name, displayName, joinable, spectatable) {
            servers = CopyOnWriteArrayList(inputSer)
    }

    companion object{
        val show = ItemBuilder(Material.valueOf(MaxJoiner.settings.getString("icons.show_all.type")))
            .name(MaxJoiner.settings.getStringColored("icons.show_all.name"))
            .damage(MaxJoiner.settings.getInt("icons.show_all.damage"))
            .lore(MaxJoiner.settings.getStringColored("icons.show_all.lore").lines())
            .also {
                if(MaxJoiner.settings.getBoolean("icons.show_all.shiny")) {
                    it.shiny()
                }
            }

        val join = ItemBuilder(Material.valueOf(MaxJoiner.settings.getString("icons.fast_join.type")))
            .name(MaxJoiner.settings.getStringColored("icons.fast_join.name"))
            .damage(MaxJoiner.settings.getInt("icons.fast_join.damage"))
            .lore(MaxJoiner.settings.getStringColored("icons.fast_join.lore"))
            .also {
                if(MaxJoiner.settings.getBoolean("icons.fast_join.shiny")) {
                    it.shiny()
                }
            }

        val back = ItemBuilder(Material.valueOf(MaxJoiner.settings.getString("icons.back.type")))
            .name(MaxJoiner.settings.getStringColored("icons.back.name"))
            .damage(MaxJoiner.settings.getInt("icons.back.damage"))
            .lore(MaxJoiner.settings.getStringColored("icons.back.lore").lines())
            .also {
                if(MaxJoiner.settings.getBoolean("icons.back.shiny")) {
                    it.shiny()
                }
            }
    }

    val itemServerMap = HashMap<ItemStack,Server>()

    val maxPlayer: Int
    get() {
        var players = 0
        servers.forEach {
            players += it.currentState.max

        }
        return players
    }

    val currentPlayer: Int
    get() {
            var players = 0
            servers.forEach {
                players += it.currentState.current
            }
            return players
        }

    private val slots = listOf(
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43,
    )
    /**
     * quick:
     *
     * X X X X R X X X X
     * X               X
     * X               X
     * X               X
     * X               X
     * X X X X A X X X X
     */
    private val quick: Inventory = Bukkit.createInventory(null, 6*9, displayName)

    /**
     * total:
     *
     * X X X X B X X X X
     * X               X
     * X               X
     * X               X
     * X               X
     * X X X X X X X X X
     */
    private val total: Inventory = Bukkit.createInventory(null, 6*9, displayName)

    private val listener = CategoryListener(this)

    // build items
    init {
        Bukkit.getPluginManager().registerEvents(listener,MaxJoiner.plugin)
        quick.setItem(4,join.build())
        quick.setItem(49,show.build())
        total.setItem(4,back.build())
    }

    fun openGui(player: Player) {
        player.openInventory(getQuick())
    }

    fun sort() {
        servers.sortDescending()
    }

    /**
     * 加入的服务器
     *
     * @return 可加入的服务器,若无可加入的服务器为null
     */
    fun pollServer(): Server?{
        sort()
        servers.forEach {
            if (it.canJoin) {
                return it
            }
        }
        return null
    }

    fun updateGUI() {
        // clearMap
        itemServerMap.clear()

        // clear quick
        slots.forEach {
            quick.setItem(it,null)
        }

        // build & fill item in quick
        val temp = ArrayList<Server>()
        servers.forEach {
            if(it.canJoin) temp.add(it)
        }
        temp.sortDescending()

        temp.forEachIndexed { index, server ->
            if(!server.canJoin) {
                return@forEachIndexed
            }
            val serverItem = buildServerItem(server)
            quick.setItem(slots[index],serverItem)
        }

        // clear total
        slots.forEach {
            total.setItem(it,null)
        }

        // build & fill item in total
        servers.forEachIndexed { index, server ->

//            println("Item\nSlot: $index \n Server: $server")
            val serverItem = buildServerItem(server)
            total.setItem(slots[index],serverItem)
        }
    }

    private fun buildServerItem(server: Server): ItemStack {
        MaxJoiner.settings

        // Offline
        if(!server.currentState.isOnline) {
            val builder = ItemBuilder(Material.valueOf(MaxJoiner.settings.getString("icons.closed.type")))
                .name(MaxJoiner.settings.getStringColored("icons.closed.name").replace("%name%",server.name))
                .damage(MaxJoiner.settings.getInt("icons.closed.damage"))
                .lore(MaxJoiner.settings.getString("icons.closed.lore").lines())
            if(MaxJoiner.settings.getBoolean("icons.closed.shiny")) {
                builder.shiny()
            }
            itemServerMap[builder.build()] = server
            return builder.build()
        }

        // check can spectate


        if(!server.canJoin) {

            // Unjoinable
            if(!server.canSpectate) {
                val builder = ItemBuilder(Material.valueOf(MaxJoiner.settings.getString("icons.unjoinable.type")))
                    .name(MaxJoiner.settings.getStringColored("icons.unjoinable.name").replace("%name%",server.name))
                    .damage(MaxJoiner.settings.getInt("icons.unjoinable.damage"))
                    .lore(MaxJoiner.settings.getStringColored("icons.unjoinable.lore").replace("%player%",server.currentState.current.toString()).replace("%motd%",server.currentState.lore).lines())
                if(MaxJoiner.settings.getBoolean("icons.unjoinable.shiny")) {
                    builder.shiny()
                }
                itemServerMap[builder.build()] = server
                return builder.build()
            }

            // Can spectate
            let {
                val builder = ItemBuilder(Material.valueOf(MaxJoiner.settings.getString("icons.spectateble.type")))
                    .damage(MaxJoiner.settings.getInt("icons.spectateble.damage"))
                    .name(MaxJoiner.settings.getStringColored("icons.spectateble.name").replace("%name%", server.name))
                    .lore(
                        MaxJoiner.settings.getStringColored("icons.spectateble.lore")
                            .replace("%player%", server.currentState.current.toString())
                            .replace("%motd%", server.currentState.lore).lines()
                    )
                if (MaxJoiner.settings.getBoolean("icons.spectateble.shiny")) {
                    builder.shiny()
                }
                itemServerMap[builder.build()] = server
                return builder.build()
            }
        }

        // Joinable
        val builder = ItemBuilder(Material.valueOf(MaxJoiner.settings.getString("icons.joinable.type")))
            .name(MaxJoiner.settings.getStringColored("icons.joinable.name").replace("%name%", server.name))
            .damage(MaxJoiner.settings.getInt("icons.joinable.damage"))
            .lore(MaxJoiner.settings.getStringColored("icons.joinable.lore").replace("%player%",server.currentState.current.toString()).replace("%motd%",server.currentState.lore).lines())
        itemServerMap[builder.build()] = server
        return builder.build()
    }

    @JvmName("quick")
    fun getQuick(): Inventory {
        updateGUI()
        return quick
    }

    @JvmName("total")
    fun getTotal(): Inventory {
        updateGUI()
        return total
    }
}