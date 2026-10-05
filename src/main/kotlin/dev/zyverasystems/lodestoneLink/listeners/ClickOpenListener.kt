package dev.zyverasystems.lodestoneLink.listeners

import com.cjcrafter.foliascheduler.FoliaCompatibility
import dev.zyverasystems.lodestoneLink.LodestoneLink
import dev.zyverasystems.lodestoneLink.SpecialCompass
import dev.zyverasystems.lodestoneLink.WarpCompass
import dev.zyverasystems.lodestoneLink.WarpManager
import dev.zyverasystems.lodestoneLink.menu.TeleportMenu
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class ClickOpenListener : Listener {
    val schedler = FoliaCompatibility(LodestoneLink.instance).serverImplementation

    @EventHandler
    fun onClickOpen(e: PlayerInteractEvent) {
        if (!e.action.isRightClick) return
        val item = e.item ?: return

        if (WarpCompass.isWarpCompass(item)) {
            e.isCancelled = true
            WarpManager.useCompass(e.player, item)
            return
        }

        if (!SpecialCompass.isItem(item)) return

        if (!e.player.hasPermission("lodestonelink.use")) return

        SpecialCompass.getLocationsFromItem(item).forEach { loc ->
            schedler.region(loc).run(Runnable {
                if (loc.block.type != Material.LODESTONE) {
                    val new = SpecialCompass.removeLocationFromItem(item, loc)
                    e.player.inventory.setItemInMainHand(new)
                }
            })
        }

        TeleportMenu.open(e.player, item)
    }
}