package dev.zyverasystems.lodestoneLink

import dev.zyverasystems.lodestoneLink.util.ConfigUtil
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

object WarpCompass {
    private lateinit var itemKey: NamespacedKey
    private val mm = MiniMessage.miniMessage()

    fun init(plugin: JavaPlugin) {
        itemKey = NamespacedKey(plugin, "warp-compass-id")
    }

    fun identifierKey(): NamespacedKey = itemKey

    fun isWarpCompass(item: ItemStack): Boolean {
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(itemKey, PersistentDataType.STRING)
    }

    fun getWarpId(item: ItemStack): String? {
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(itemKey, PersistentDataType.STRING)
    }

    fun item(warp: WarpManager.Warp): ItemStack {
        val item = ItemStack(Material.COMPASS)
        val meta = item.itemMeta ?: return item
        meta.displayName(
            mm.deserialize(
                ConfigUtil.config.getString("warp.displayname", "<gold>Warp Compass</gold>")
                    ?: "<gold>Warp Compass</gold>"
            )
        )
        meta.lore(
            ConfigUtil.config.getStringList("warp.lore").map { line ->
                mm.deserialize(line.replace("{NAME}", warp.name))
            }
        )
        meta.persistentDataContainer.set(itemKey, PersistentDataType.STRING, warp.id)
        item.itemMeta = meta
        return item
    }
}
