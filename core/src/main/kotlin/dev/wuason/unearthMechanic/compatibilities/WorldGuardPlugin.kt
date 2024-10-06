package dev.wuason.unearthMechanic.compatibilities

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException
import dev.wuason.unearthMechanic.UnearthMechanic
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class WorldGuardPlugin {
    companion object {
        private var NAME: String = "WorldGuard"

        fun isWorldGuardLoaded(): Boolean {
            return Bukkit.getPluginManager().getPlugin(NAME) != null
        }

        fun isWorldGuardEnabled(): Boolean {
            return Bukkit.getPluginManager().getPlugin(NAME) != null && Bukkit.getPluginManager().getPlugin(NAME)!!
                .isEnabled
        }
    }
}