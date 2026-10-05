package dev.zyverasystems.lodestoneLink.commands

import dev.zyverasystems.lodestoneLink.WarpManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WarpCommands : CommandExecutor {
    private val mm = MiniMessage.miniMessage()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return true
        }

        when (command.name.lowercase()) {
            "setwarp" -> {
                if (!sender.hasPermission("lodestonelink.warp.set")) {
                    sender.sendMessage(mm.deserialize("<red>You do not have permission to set warps.</red>"))
                    return true
                }
                if (args.isEmpty()) {
                    sender.sendMessage(mm.deserialize("<red>Usage: /setwarp <name></red>"))
                    return true
                }
                val warp = WarpManager.setWarp(args[0], sender.location) ?: run {
                    sender.sendMessage(mm.deserialize("<red>Invalid warp name.</red>"))
                    return true
                }
                sender.sendMessage(mm.deserialize("<green>Warp created: ${warp.name}</green>"))
                return true
            }
            "warp" -> {
                if (!sender.hasPermission("lodestonelink.warp.give")) {
                    sender.sendMessage(mm.deserialize("<red>You do not have permission to create warp compasses.</red>"))
                    return true
                }
                if (args.isEmpty()) {
                    sender.sendMessage(mm.deserialize("<red>Usage: /warp <name></red>"))
                    return true
                }
                WarpManager.giveCompass(sender, args[0])
                return true
            }
            "warps" -> {
                if (!sender.hasPermission("lodestonelink.warp.list")) {
                    sender.sendMessage(mm.deserialize("<red>You do not have permission to list warps.</red>"))
                    return true
                }
                WarpManager.listWarps(sender)
                return true
            }
            "delwarp" -> {
                if (!sender.hasPermission("lodestonelink.warp.delete")) {
                    sender.sendMessage(mm.deserialize("<red>You do not have permission to delete warps.</red>"))
                    return true
                }
                if (args.isEmpty()) {
                    sender.sendMessage(mm.deserialize("<red>Usage: /delwarp <name></red>"))
                    return true
                }
                val name = args[0]
                val removed = WarpManager.deleteWarp(name)
                if (removed) {
                    sender.sendMessage(mm.deserialize("<green>Warp removed: $name</green>"))
                } else {
                    sender.sendMessage(mm.deserialize("<red>That warp does not exist.</red>"))
                }
                return true
            }
            else -> return false
        }
    }
}
