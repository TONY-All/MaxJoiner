package cn.maxmc.maxjoiner

import cn.maxmc.maxjoiner.server.Server
import cn.maxmc.maxjoiner.server.ServerCategory
import cn.maxmc.maxjoiner.server.ServerInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection

object ServerManager {
    private val categories = ArrayList<ServerCategory>()
    private val index = HashMap<String, ServerCategory>()

    fun loadCategories() {
        categories.clear()
        val confSec = MaxJoiner.servers.getConfigurationSection("categories")
        confSec.getKeys(false).forEach {
            val category = loadCategory(confSec.getConfigurationSection(it))
            categories.add(category)
            index[category.name] = category
        }
    }

    private fun loadCategory(confSec: ConfigurationSection): ServerCategory {
        val servers = ArrayList<Server>()
        confSec.getConfigurationSection("servers").getKeys(false).forEach {
            val server = loadServer(confSec.getConfigurationSection("servers.$it"))
            servers.add(server)
        }

        return ServerCategory(
            confSec.name,
            confSec.getStringColored("name"),
            confSec.getStringList("joinable"),
            confSec.getStringList("spectatable"),
            servers
        )
    }

    private fun loadServer(confSec: ConfigurationSection): Server {
        val url = confSec.getString("ip")
        val ip = url.substringBefore(":")
        val port = url.substringAfter(":").toInt()

        return Server(
            confSec.name,
            confSec.getStringColored("name"),
            true,
            ip,
            port,
            ServerInfo(false,0,0,""),
            false
        )
    }

    fun updatePing() {
        categories.forEach {
            it.servers.forEach { server ->
                GlobalScope.launch {
                    val serverInfo = ping(server.url, server.port)
                    server.currentState = serverInfo
                    var temp = false
                    for (joinStr in it.joinable) {
                        if(joinStr == serverInfo.lore || server.currentState.lore.matches(Regex(joinStr))) {
                            temp = true
                            break
                        }
                    }
                    server.canJoin = temp
                    temp = false
                    for (patten in it.spectatable) {
                        if(patten == server.currentState.lore || server.currentState.lore.matches(Regex(patten))) {
                            temp = true
                            break
                        }
                    }
                    server.canSpectate = temp
                }
            }
            Bukkit.getScheduler().runTaskAsynchronously(MaxJoiner.plugin) {
                it.sort()
                it.updateGUI()
            }
        }
    }

    fun getCategoryByName(name: String): ServerCategory? {
        return index[name]
    }
}