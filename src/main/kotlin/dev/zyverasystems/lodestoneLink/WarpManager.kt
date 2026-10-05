package dev.zyverasystems.lodestoneLink

import dev.zyverasystems.lodestoneLink.util.ConfigUtil
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

object WarpManager {
    private lateinit var plugin: JavaPlugin
    private val mm = MiniMessage.miniMessage()

    data class Warp(
        val id: String,
        val name: String,
        val location: Location
    )

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun allWarps(): List<Warp> {
        val section = plugin.config.getConfigurationSection("warps") ?: return emptyList()
        return section.getKeys(false).mapNotNull { key ->
            val warpSection = section.getConfigurationSection(key) ?: return@mapNotNull null
            parseWarp(warpSection)
        }.sortedBy { it.name.lowercase() }
    }

    fun getWarp(name: String): Warp? {
        return allWarps().firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    fun getWarpById(id: String): Warp? {
        return allWarps().firstOrNull { it.id == id }
    }

    fun setWarp(name: String, location: Location): Warp? {
        val normalized = name.trim()
        if (normalized.isEmpty()) {
            return null
        }

        val warp = Warp(
            UUID.nameUUIDFromBytes("lodestonelink:warp:${normalized.lowercase()}".toByteArray()).toString(),
            normalized,
            location.clone()
        )

        val section = plugin.config.getConfigurationSection("warps") ?: plugin.config.createSection("warps")
        val warpSection = section.getConfigurationSection(normalized.lowercase()) ?: section.createSection(normalized.lowercase())
        warpSection.set("id", warp.id)
        warpSection.set("name", warp.name)
        warpSection.set("world", warp.location.world?.name ?: "")
        warpSection.set("x", warp.location.x)
        warpSection.set("y", warp.location.y)
        warpSection.set("z", warp.location.z)
        warpSection.set("yaw", warp.location.yaw)
        warpSection.set("pitch", warp.location.pitch)
        plugin.saveConfig()
        return warp
    }

    fun deleteWarp(name: String): Boolean {
        val section = plugin.config.getConfigurationSection("warps") ?: return false
        val key = section.getKeys(false).firstOrNull { it.equals(name, ignoreCase = true) } ?: return false
        section.set(key, null)
        plugin.saveConfig()
        return true
    }

    fun listWarps(player: Player) {
        val warps = allWarps()
        if (warps.isEmpty()) {
            player.sendMessage(mm.deserialize(ConfigUtil.config.getString("messages.no-warps", "<red>No public warps were found.</red>") ?: "<red>No public warps were found.</red>"))
            return
        }
        player.sendMessage(
            mm.deserialize(
                (ConfigUtil.config.getString("messages.warp-list", "<gold>Warps: {LIST}</gold>")
                    ?: "<gold>Warps: {LIST}</gold>")
                    .replace("{LIST}", warps.joinToString(", ") { it.name })
            )
        )
    }

    fun giveCompass(player: Player, name: String): Boolean {
        val warp = getWarp(name) ?: run {
            player.sendMessage(
                mm.deserialize(
                    ConfigUtil.config.getString("messages.invalid-warp", "<red>That warp no longer exists.</red>")
                        ?: "<red>That warp no longer exists.</red>"
                )
            )
            return false
        }

        val item = WarpCompass.item(warp)
        val slot = player.inventory.firstEmpty()
        if (slot >= 0) {
            player.inventory.setItem(slot, item)
        } else {
            player.world.dropItemNaturally(player.location, item)
        }
        player.sendMessage(
            mm.deserialize(
                (ConfigUtil.config.getString("messages.warp-given", "<green>Warp compass created for {NAME}</green>")
                    ?: "<green>Warp compass created for {NAME}</green>")
                    .replace("{NAME}", warp.name)
            )
        )
        return true
    }

    fun useCompass(player: Player, item: org.bukkit.inventory.ItemStack): Boolean {
        val warpId = WarpCompass.getWarpId(item) ?: run {
            player.sendMessage(
                mm.deserialize(
                    ConfigUtil.config.getString("messages.warp-invalid-item", "<red>That warp compass is invalid.</red>")
                        ?: "<red>That warp compass is invalid.</red>"
                )
            )
            return false
        }

        val warp = getWarpById(warpId) ?: run {
            player.sendMessage(
                mm.deserialize(
                    ConfigUtil.config.getString("messages.invalid-warp", "<red>That warp no longer exists.</red>")
                        ?: "<red>That warp no longer exists.</red>"
                )
            )
            return false
        }

        if (warp.location.world == null || warp.location.world!!.getBlockAt(warp.location).type == Material.AIR) {
            player.sendMessage(
                mm.deserialize(
                    ConfigUtil.config.getString("messages.invalid-warp", "<red>That warp no longer exists.</red>")
                        ?: "<red>That warp no longer exists.</red>"
                )
            )
            return false
        }

        return TeleportManager.start(player, warp.location.clone(), "warp", warp.name)
    }

    private fun parseWarp(section: org.bukkit.configuration.ConfigurationSection): Warp? {
        val worldName = section.getString("world") ?: return null
        val world = Bukkit.getWorld(worldName) ?: return null
        val name = section.getString("name") ?: return null
        val id = section.getString("id") ?: UUID.nameUUIDFromBytes("lodestonelink:warp:${name.lowercase()}".toByteArray()).toString()

        return Warp(
            id,
            name,
            Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                section.getDouble("yaw").toFloat(),
                section.getDouble("pitch").toFloat()
            )
        )
    }
}
