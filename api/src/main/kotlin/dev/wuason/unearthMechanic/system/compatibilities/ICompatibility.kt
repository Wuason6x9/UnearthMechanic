package dev.wuason.unearthMechanic.system.compatibilities

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

interface ICompatibility : Listener {
    fun loaded(): Boolean
    fun enabled(): Boolean
    fun name(): String
    fun adapterId(): String
    fun handleStage(player: Player, itemId: String, event: Event, loc: Location, toolUsed: ILiveTool, generic: IGeneric, stage: IStage)
    fun handleRemove(player: Player, event: Event, loc: Location, toolUsed: ILiveTool, generic: IGeneric, stage: IStage)
    fun hashCode(player: Player, event: Event, loc: Location, toolUsed: ILiveTool, generic: IGeneric, stage: Int): Int
    fun getItemHand(event: Event) : ItemStack?
    open fun onLoad() {}


}