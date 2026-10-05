package dev.zyverasystems.lodestoneLink.menu

import com.cjcrafter.foliascheduler.FoliaCompatibility
import dev.zyverasystems.lodestoneLink.TeleportManager
import dev.zyverasystems.lodestoneLink.util.ConfigUtil
import dev.zyverasystems.lodestoneLink.util.ConfigUtil.getStringNn
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.java.JavaPlugin

class MenuClickListener(plugin: JavaPlugin) : Listener {
    private val scheduler = FoliaCompatibility(plugin).serverImplementation

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder as? MenuHolder ?: return
        e.isCancelled = true
        val loc = holder.getLocation(e.slot) ?: return
        scheduler.region(loc).execute(Runnable {
            if (loc.block.type != Material.LODESTONE) {
                e.whoClicked.sendMessage(
                    MiniMessage.miniMessage().deserialize(ConfigUtil.config.getStringNn("messages.unsafe"))
                )
                e.whoClicked.closeInventory()
                return@Runnable
            }

            val player = e.whoClicked as? org.bukkit.entity.Player ?: return@Runnable
            if (!player.isOnGround) return@Runnable
            TeleportManager.start(player, loc.clone().add(0.5, 1.0, 0.5), "home")
            player.closeInventory()
        })
    }
}