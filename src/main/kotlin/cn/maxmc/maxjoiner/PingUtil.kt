package cn.maxmc.maxjoiner

import cn.maxmc.maxjoiner.server.Server
import cn.maxmc.maxjoiner.server.ServerInfo
import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player
import taboolib.common.platform.function.warning
import taboolib.platform.BukkitPlugin
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import kotlin.experimental.and
import kotlin.time.ExperimentalTime

val timeout: Long
    get() = MaxJoiner.settings.getLong("timeout")

@OptIn(ExperimentalTime::class)
suspend fun ping(url: String, port: Int): ServerInfo = withTimeout(timeout) {
    withContext(Dispatchers.IO) {
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(url, port))
        } catch (e: Exception) {
            return@withContext ServerInfo(false, 0, 0, "")
        }
        val inputStream = socket.getInputStream()
        val dataInputStream = DataInputStream(inputStream)
        val outputStream = socket.getOutputStream()
        val dataOutputStream = DataOutputStream(outputStream)
        val bs = ByteArrayOutputStream()
        val out = DataOutputStream(bs)

        // Send Handshake
        out.write(0)
        writeVarInt(out, 4)
        writeString(out, url)
        out.writeShort(port)
        writeVarInt(out, 1)
        sendPacket(dataOutputStream, bs.toByteArray())

        // Query
        val result = runCatching {
            sendPacket(dataOutputStream, ByteArray(1))
            readVarInt(dataInputStream)
            val packetId: Int = readVarInt(dataInputStream)
            if (packetId != 0) {
                throw IOException("Invalid packetId")
            }
            val stringLength: Int = readVarInt(dataInputStream)
            if (stringLength < 1) {
                throw IOException("Invalid string length.")
            }
            val responseData = ByteArray(stringLength)
            dataInputStream.readFully(responseData)
            responseData.decodeToString()
        }.getOrElse {
            println("§c| §7 Error while contacting server: ${url}:$port")
            return@withContext ServerInfo(false, 0, 0, "")
        }

        try {
            val jsonObject = JsonParser().parse(result).asJsonObject
            val online = jsonObject.get("players").asJsonObject.get("online").asInt
            val max = jsonObject.get("players").asJsonObject.get("max").asInt
            val description = jsonObject.get("description").let {
                if (it is JsonObject) {
                    val parse = ComponentSerializer.parse(it.toString())
                    val ret = StringBuffer()
                    parse.forEach { comp ->
                        ret.append(comp.toLegacyText())
                    }
                    return@let ret.toString()
                }
                it.asString
            }
            return@withContext ServerInfo(true, online, max, description)
        } catch (e: Throwable) {
            warning("§c| §7Error while parse Json Object: \n $result")
            return@withContext ServerInfo(false, 0, 0, "")
        }
    }
}

private fun sendPacket(out: DataOutputStream, data: ByteArray) {
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

@Suppress("UnstableApiUsage")
fun Player.connect(server: Server) {
    server.bcName
    val out = ByteStreams.newDataOutput()

    out.writeUTF("Connect")
    out.writeUTF(server.bcName)

    this.sendPluginMessage(BukkitPlugin.getInstance(), "BungeeCord", out.toByteArray())
}

val pluginScope = CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { _, except ->
    except.printStackTrace()
})