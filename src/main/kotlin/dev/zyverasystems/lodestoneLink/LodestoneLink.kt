package dev.zyverasystems.lodestoneLink

import dev.zyverasystems.lodestoneLink.commands.WarpCommands
import dev.zyverasystems.lodestoneLink.listeners.AddWaystoneListener
import dev.zyverasystems.lodestoneLink.listeners.CancelCraftListener
import dev.zyverasystems.lodestoneLink.listeners.ClickOpenListener
import dev.zyverasystems.lodestoneLink.menu.MenuClickListener
import dev.zyverasystems.lodestoneLink.menu.TeleportMenu
import dev.zyverasystems.lodestoneLink.util.ConfigUtil
import dev.zyverasystems.lodestoneLink.util.NamedLodestoneManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

class LodestoneLink : JavaPlugin() {

    override fun onEnable() {
        instance = this
        saveDefaultConfig()

        ConfigUtil.init(this)
        SpecialCompass.init(this)
        WarpCompass.init(this)
        WarpManager.init(this)
        CombatManager.init(this)
        TeleportManager.init(this)
        TeleportMenu.init(this)
        registerCrafting()

        server.pluginManager.registerEvents(AddWaystoneListener(), this)
        server.pluginManager.registerEvents(ClickOpenListener(), this)
        server.pluginManager.registerEvents(NamedLodestoneManager(), this)
        server.pluginManager.registerEvents(MenuClickListener(this), this)
        server.pluginManager.registerEvents(CancelCraftListener(), this)
        server.pluginManager.registerEvents(TeleportProtectionListener(), this)

        val warpCommands = WarpCommands()
        getCommand("setwarp")?.setExecutor(warpCommands)
        getCommand("warp")?.setExecutor(warpCommands)
        getCommand("warps")?.setExecutor(warpCommands)
        getCommand("delwarp")?.setExecutor(warpCommands)
    }

    fun registerCrafting() {
        val shape = ConfigUtil.config.getStringList("crafting.shape")
            .map { it.padEnd(3, ' ').take(3) }

        if (shape.size != 3) {
            logger.warning("Invalid crafting recipe in config, crafting will not be possible")
            return
        }

        val ingredients =
            ConfigUtil.config.getConfigurationSection("crafting.ingredients")?.getKeys(false)?.mapNotNull { key ->
                Pair(
                    key.first(), Material.matchMaterial(
                        ConfigUtil.config.getString("crafting.ingredients.$key") ?: "BARRIER"
                    ) ?: Material.BARRIER
                )
            }

        ingredients?.forEach { (char, mat) ->
            if (mat == Material.BARRIER) {
                logger.warning("Invalid crafting ingredient at char $char, crafting will not be possible")
            }
        }

        Bukkit.addRecipe(
            ShapedRecipe(
                NamespacedKey(this, "custom_compass"),
                SpecialCompass.item()
            ).shape(
                shape[0],
                shape[1],
                shape[2]
            ).apply {
                ingredients?.forEach { (char, mat) ->
                    setIngredient(char, mat)
                }
            }
        )
    }

    companion object {
        lateinit var instance: LodestoneLink private set
    }
}
