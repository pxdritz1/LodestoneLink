package dev.zyverasystems.lodestoneLink

import dev.zyverasystems.lodestoneLink.util.ConfigUtil
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

object CombatManager {
    private lateinit var plugin: JavaPlugin
    private val combatEnds = mutableMapOf<UUID, Long>()

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun defaultDurationSeconds(): Int = 30

    fun durationMillis(): Long = ConfigUtil.config.getLong("combat-duration", defaultDurationSeconds().toLong()) * 1000L

    fun tagPlayer(player: Player) {
        combatEnds[player.uniqueId] = System.currentTimeMillis() + durationMillis()
    }

    fun isInCombat(player: Player): Boolean {
        val expiry = combatEnds[player.uniqueId] ?: return false
        if (expiry <= System.currentTimeMillis()) {
            combatEnds.remove(player.uniqueId)
            return false
        }
        return true
    }

    fun remainingSeconds(player: Player): Int {
        val expiry = combatEnds[player.uniqueId] ?: return 0
        val remaining = (expiry - System.currentTimeMillis()) / 1000L
        return remaining.coerceAtLeast(0).toInt()
    }

    fun clear(player: Player) {
        combatEnds.remove(player.uniqueId)
    }

    fun message(player: Player): String {
        return (ConfigUtil.config.getString("messages.in-combat", "<red>You are in combat.</red>")
            ?: "<red>You are in combat.</red>")
            .replace("{TIME}", remainingSeconds(player).toString())
    }
}
