package dev.wuason.unearthMechanic.compatibilities

import net.luckperms.api.LuckPermsProvider
import org.bukkit.Bukkit
import org.bukkit.entity.Player


class LuckPermsPlugin {
    companion object {
        private var NAME: String = "LuckPerms"

        fun isLuckPermsLoaded(): Boolean {
            return Bukkit.getPluginManager().getPlugin(NAME) != null
        }

        fun isLuckPermsEnabled(): Boolean {
            return Bukkit.getPluginManager().getPlugin(NAME) != null && Bukkit.getPluginManager().getPlugin(NAME)!!
                .isEnabled
        }
    }
}