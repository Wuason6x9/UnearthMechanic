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

class WorldGuardComp(private val core: UnearthMechanic) {

    companion object {
        private var FLAG: String = "unearth-interact"
    }

    private var unearthInteractFlag: StateFlag? = null

    init {
        core.logger.info("WorldGuard found! Enabling compatibility...")
        val registry = WorldGuard.getInstance().flagRegistry
        try {
            val flag = StateFlag(FLAG, false)
            registry.register(flag)
            unearthInteractFlag = flag
        } catch (e: FlagConflictException) {
            val existing = registry[FLAG]
            if (existing is StateFlag) {
                unearthInteractFlag = existing
            } else {
                throw RuntimeException("Another plugin is using the flag name " + FLAG)
            }
        }
    }

    fun canInteract(player: Player, target: Location?): Boolean {
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        return WorldGuard.getInstance().platform.regionContainer.createQuery()
            .testBuild(BukkitAdapter.adapt(target), localPlayer, unearthInteractFlag)
                || WorldGuard.getInstance().platform.sessionManager.hasBypass(
            localPlayer,
            BukkitAdapter.adapt(player.world)
        )
    }
}