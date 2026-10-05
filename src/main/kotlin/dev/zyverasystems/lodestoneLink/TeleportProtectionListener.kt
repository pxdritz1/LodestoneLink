package dev.zyverasystems.lodestoneLink

import dev.zyverasystems.lodestoneLink.util.ConfigUtil
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class TeleportProtectionListener : Listener {
    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (!event.hasChangedPosition()) return
        if (TeleportManager.hasPending(event.player)) {
            TeleportManager.cancelPending(event.player, ConfigUtil.config.getString("messages.movement-cancel", "<red>Teleport cancelled because you moved.</red>"))
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (TeleportManager.hasPending(player)) {
            TeleportManager.cancelPending(player, ConfigUtil.config.getString("messages.damage-cancel", "<red>Teleport cancelled because you took damage.</red>"))
        }

        val byEntity = event as? EntityDamageByEntityEvent ?: return
        val attacker = byEntity.damager as? Player ?: findProjectileShooter(byEntity.damager)
        ?: return
        if (attacker == player) return
        CombatManager.tagPlayer(attacker)
        CombatManager.tagPlayer(player)
        if (TeleportManager.hasPending(player)) {
            TeleportManager.cancelPending(player, ConfigUtil.config.getString("messages.combat-block", "<red>You cannot teleport while in combat.</red>"))
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        TeleportManager.cancelPending(event.player, ConfigUtil.config.getString("messages.death-cancel", "<red>Teleport cancelled because you died.</red>"))
        CombatManager.clear(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        TeleportManager.cancelPending(event.player, ConfigUtil.config.getString("messages.disconnect-cancel", "<red>Teleport cancelled because you disconnected.</red>"))
        CombatManager.clear(event.player)
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        TeleportManager.cancelPending(event.player, ConfigUtil.config.getString("messages.world-cancel", "<red>Teleport cancelled because you changed worlds.</red>"))
    }

    private fun findProjectileShooter(entity: Entity): Player? {
        if (entity is Projectile && entity.shooter is Player) {
            return entity.shooter as Player
        }
        return null
    }
}
