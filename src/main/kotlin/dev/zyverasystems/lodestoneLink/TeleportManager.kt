package dev.zyverasystems.lodestoneLink

import dev.zyverasystems.lodestoneLink.util.ConfigUtil
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

object TeleportManager {
    private lateinit var plugin: JavaPlugin
    private val mm = MiniMessage.miniMessage()
    private val pendingTeleports = mutableMapOf<UUID, PendingTeleport>()
    private val pendingTasks = mutableMapOf<UUID, Int>()

    data class PendingTeleport(
        val playerId: UUID,
        val destination: Location,
        val source: String,
        val origin: Location,
        val warpName: String? = null
    )

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun defaultDelaySeconds(): Int = 5

    fun hasPending(player: Player): Boolean = pendingTeleports.containsKey(player.uniqueId)

    fun start(player: Player, destination: Location, source: String, warpName: String? = null): Boolean {
        val uuid = player.uniqueId
        if (!player.isOnline || player.isDead || !player.isValid) {
            return false
        }
        if (hasPending(player)) {
            player.sendMessage(
                mm.deserialize(
                    ConfigUtil.config.getString("messages.already-running", "<red>A teleport is already in progress.</red>")
                        ?: "<red>A teleport is already in progress.</red>"
                )
            )
            return false
        }
        if (CombatManager.isInCombat(player)) {
            player.sendMessage(mm.deserialize(CombatManager.message(player)))
            return false
        }

        val delaySeconds = ConfigUtil.config.getInt("teleport-delay", defaultDelaySeconds())
        val pending = PendingTeleport(uuid, destination.clone(), source, player.location.clone(), warpName)
        pendingTeleports[uuid] = pending

        var remaining = delaySeconds
        val task = object : BukkitRunnable() {
            override fun run() {
                val active = pendingTeleports[uuid] ?: run {
                    cancelTask(uuid)
                    return
                }

                if (!player.isOnline || player.isDead || !player.isValid) {
                    cancelPending(
                        player,
                        ConfigUtil.config.getString("messages.disconnect-cancel", "<red>Teleport cancelled because you disconnected.</red>")
                            ?: "<red>Teleport cancelled because you disconnected.</red>"
                    )
                    return
                }

                if (CombatManager.isInCombat(player)) {
                    cancelPending(
                        player,
                        ConfigUtil.config.getString("messages.combat-block", "<red>You cannot teleport while in combat.</red>")
                            ?: "<red>You cannot teleport while in combat.</red>"
                    )
                    return
                }

                if (active.origin.distanceSquared(player.location) > 1.2) {
                    cancelPending(
                        player,
                        ConfigUtil.config.getString("messages.movement-cancel", "<red>Teleport cancelled because you moved.</red>")
                            ?: "<red>Teleport cancelled because you moved.</red>"
                    )
                    return
                }

                if (remaining <= 0) {
                    finalizeTeleport(player, active)
                    cancelTask(uuid)
                    return
                }

                if (remaining == 1) {
                    announceCountdown(player, remaining)
                    finalizeTeleport(player, active)
                    cancelTask(uuid)
                    return
                }

                announceCountdown(player, remaining)
                remaining--
            }
        }.runTaskTimer(plugin, 0L, 20L)
        pendingTasks[uuid] = task.taskId
        return true
    }

    fun cancelPending(player: Player, reason: String? = null) {
        val uuid = player.uniqueId
        if (pendingTeleports.remove(uuid) != null) {
            cancelTask(uuid)
        }
        if (!reason.isNullOrBlank()) {
            player.sendMessage(mm.deserialize(reason))
        }
    }

    fun finalizeTeleport(player: Player, pending: PendingTeleport) {
        val uuid = player.uniqueId
        if (pendingTeleports[uuid] != pending) {
            return
        }

        if (!player.isOnline || player.isDead || !player.isValid) {
            pendingTeleports.remove(uuid)
            return
        }
        if (CombatManager.isInCombat(player)) {
            cancelPending(
                player,
                ConfigUtil.config.getString("messages.combat-block", "<red>You cannot teleport while in combat.</red>")
                    ?: "<red>You cannot teleport while in combat.</red>"
            )
            return
        }
        if (pending.destination.world == null || !pending.destination.world.isChunkLoaded(pending.destination.blockX shr 4, pending.destination.blockZ shr 4)) {
            cancelPending(
                player,
                ConfigUtil.config.getString("messages.unsafe", "<red>This waypoint is unsafe to teleport to.</red>")
                    ?: "<red>This waypoint is unsafe to teleport to.</red>"
            )
            return
        }
        if (pending.warpName != null && WarpManager.getWarp(pending.warpName) == null) {
            cancelPending(
                player,
                ConfigUtil.config.getString("messages.invalid-warp", "<red>That warp no longer exists.</red>")
                    ?: "<red>That warp no longer exists.</red>"
            )
            return
        }
        if (pending.origin.distanceSquared(player.location) > 1.2) {
            cancelPending(
                player,
                ConfigUtil.config.getString("messages.movement-cancel", "<red>Teleport cancelled because you moved.</red>")
                    ?: "<red>Teleport cancelled because you moved.</red>"
            )
            return
        }

        pendingTeleports.remove(uuid)
        player.teleportAsync(pending.destination.clone().add(0.5, 0.0, 0.5))
        player.sendMessage(mm.deserialize(ConfigUtil.config.getString("messages.teleported", "<green>Teleported.</green>") ?: "<green>Teleported.</green>"))
    }

    private fun announceCountdown(player: Player, second: Int) {
        val message = ConfigUtil.config.getConfigurationSection("messages.countdown")
            ?.getString(second.toString(), second.toString())
            ?: second.toString()
        player.sendMessage(mm.deserialize(message))
    }

    private fun cancelTask(uuid: UUID) {
        val taskId = pendingTasks.remove(uuid) ?: return
        Bukkit.getScheduler().cancelTask(taskId)
    }
}
