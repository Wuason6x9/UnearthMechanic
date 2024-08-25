package dev.wuason.unearthMechanic.system.compatibilities

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.config.ITool
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.Listener

interface Compatibility : Listener {
    fun loaded(): Boolean
    fun enabled(): Boolean
    fun name(): String
    fun adapterId(): String

    fun handleOthersFeatures(player: Player, event: Event, loc: Location, toolUsed: ITool, generic: IGeneric, stage: IStage)

    fun handleBlockStage(player: Player, itemId: String, event: Event, loc: Location, toolUsed: ITool, generic: IGeneric, stage: IStage)
    fun handleFurnitureStage(player: Player, itemId: String, event: Event, loc: Location, toolUsed: ITool, generic: IGeneric, stage: IStage)

    fun handleRemove(player: Player, event: Event, loc: Location, toolUsed: ITool, generic: IGeneric, stage: IStage)
}