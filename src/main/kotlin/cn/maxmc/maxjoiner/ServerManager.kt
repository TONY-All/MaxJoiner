package cn.maxmc.maxjoiner

import cn.maxmc.maxjoiner.server.Server
import cn.maxmc.maxjoiner.server.ServerCategory
import cn.maxmc.maxjoiner.server.ServerInfo
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import taboolib.common.platform.function.info
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.util.getStringColored
import taboolib.platform.BukkitPlugin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ServerManager {
    val categories = CopyOnWriteArrayList<ServerCategory>()
    private val index = HashMap<String, ServerCategory>()
    private val pool: ExecutorService = Executors.newFixedThreadPool(100)

    fun loadCategories() {
        categories.clear()
        info("§a| §7正在加载类别.")
        val confSec = MaxJoiner.servers.getConfigurationSection("categories")!!
        confSec.getKeys(false).forEach {
            info("§a| §7 Found $it")
            val category = loadCategory(confSec.getConfigurationSection(it)!!)
            categories.add(category)
            index[category.name] = category
        }
    }

    private fun loadCategory(confSec: ConfigurationSection): ServerCategory {
        val servers = ArrayList<Server>()
        confSec.getConfigurationSection("servers")!!.getKeys(false).forEach {
            val server = loadServer(confSec.getConfigurationSection("servers.$it")!!)
            servers.add(server)
        }

        return ServerCategory(
            confSec.name,
            confSec.getStringColored("name")!!,
            CopyOnWriteArrayList(confSec.getStringList("joinable")),
            CopyOnWriteArrayList(confSec.getStringList("spectatable")),
            servers
        )
    }

    private fun loadServer(confSec: ConfigurationSection): Server {
        val url = confSec.getString("ip")!!
        val ip = url.substringBefore(":")
        val port = url.substringAfter(":").toInt()

        return Server(
            confSec.name, confSec.getStringColored("name")!!, true, ip, port, ServerInfo(false, 0, 0, ""), false
        )
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun updatePing() {
        info("§a| §7正在更新服务器信息")
        pluginScope.launch(pool.asCoroutineDispatcher()) {
            val start = System.currentTimeMillis()
            categories.flatMap {
                val defs = it.servers.map { server ->
                    async {
                        val serverInfo = ping(server.url, server.port)
                        server.currentState = serverInfo
                        var temp = false
                        for (joinStr in it.joinable) {
                            if (joinStr == serverInfo.lore || server.currentState.lore.matches(Regex(joinStr))) {
                                temp = true
                                break
                            }
                        }
                        server.canJoin = temp
                        temp = false
                        for (patten in it.spectatable) {
                            if (patten == server.currentState.lore || server.currentState.lore.matches(Regex(patten))) {
                                temp = true
                                break
                            }
                        }
                        server.canSpectate = temp
                    }
                }
                Bukkit.getScheduler().runTaskAsynchronously(BukkitPlugin.getInstance()) {
                    CompletableFuture.allOf(*defs.map { def -> def.asCompletableFuture() }.toTypedArray()).join()
                    it.sort()
                    it.updateGUI()
                }
                defs
            }.awaitAll()
            info("更新完成！消耗 ${System.currentTimeMillis() - start} ms")
        }
    }

    fun getCategoryByName(name: String): ServerCategory? {
        return index[name]
    }
}