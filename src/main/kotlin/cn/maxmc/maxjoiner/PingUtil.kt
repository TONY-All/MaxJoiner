package cn.maxmc.maxjoiner

import cn.maxmc.maxjoiner.server.Server
import cn.maxmc.maxjoiner.server.ServerInfo
import com.google.common.io.ByteStreams
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO
import kotlin.experimental.and

val timeout: Int
get() = MaxJoiner.settings.getInt("timeout")

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun ping(url: String, port: Int): ServerInfo {
    return withContext(Dispatchers.IO) {
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(url, port), timeout)
        } catch (e: Exception) {
            return@withContext ServerInfo(false,0,0,"")
        }
        val inputStream = socket.getInputStream()
        val dataInputStream = DataInputStream(inputStream)
        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val bs = ByteArrayOutputStream()
        val out = DataOutputStream(bs)
        // Send Handshake
        let {
            out.write(0)
            writeVarInt(out, 4)
            writeString(out, url)
            out.writeShort(port)
            writeVarInt(out, 1)
            sendPacket(dataOutputStream, bs.toByteArray())
        }
        // Query
        val result = let {
            sendPacket(dataOutputStream, ByteArray(1))
            readVarInt(dataInputStream)
            val packetId: Int = readVarInt(dataInputStream)
            return@let if (packetId != 0) {
                throw IOException("Invalid packetId")
            } else {
                val stringLength: Int = readVarInt(dataInputStream)
                if (stringLength < 1) {
                    throw IOException("Invalid string length.")
                } else {
                    val responseData = ByteArray(stringLength)
                    dataInputStream.readFully(responseData)
                    val jsonString = String(responseData, StandardCharsets.UTF_8)
                    jsonString
                }
            }
        }

        val jsonObject = JsonParser().parse(result).asJsonObject
        val online = jsonObject.get("players").asJsonObject.get("online").asInt
        val max = jsonObject.get("players").asJsonObject.get("max").asInt
        val description = jsonObject.get("description").asString
        ServerInfo(true,online,max,description)
    }
}

private fun sendPacket(out: DataOutputStream,data: ByteArray) {
    writeVarInt(out, data.size)
    out.write(data)
    out.flush()
}

private fun readVarInt(`in`: DataInputStream): Int {
    var i = 0
    var j = 0
    var k: Byte
    do {
        k = `in`.readByte()
        i = i or ((k and 127).toInt() shl j++ * 7)
        if (j > 5) {
            throw RuntimeException("VarInt too big")
        }
    } while ((k.toInt() and 128) == 128)
    return i
}

private fun writeVarInt(out: DataOutputStream, paramInt: Int) {
    var temp = paramInt
    while (temp and -128 != 0) {
        out.write(temp and 127 or 128)
        temp = temp ushr 7
    }
    out.write(temp)
}

private fun writeString(out: DataOutputStream, string: String) {
    writeVarInt(out, string.length)
    out.write(string.toByteArray(StandardCharsets.UTF_8))
}

fun ConfigurationSection.getStringColored(path: String): String {
    return this.getString(path).replace('&','§')
}

@Suppress("UnstableApiUsage")
fun Player.connect(server: Server) {
    server.bcName
    val out = ByteStreams.newDataOutput()

    out.writeUTF("Connect")
    out.writeUTF(server.bcName)

    this.sendPluginMessage(MaxJoiner.plugin,"BungeeCord",out.toByteArray())
}