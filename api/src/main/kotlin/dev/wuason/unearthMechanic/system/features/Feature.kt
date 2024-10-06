package dev.wuason.unearthMechanic.system.features

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event

abstract class Feature {

    open fun onPreApply(p: Player, comp: ICompatibility, event: Event, loc: Location, liveTool: ILiveTool, iStage: IStage, iGeneric: IGeneric) {

    }

    open fun onProcess(tick: Long, p: Player, comp: ICompatibility, event: Event, loc: Location, liveTool: ILiveTool, iStage: IStage, iGeneric: IGeneric) {

    }

    open fun onApply(p: Player, comp: ICompatibility, event: Event, loc: Location, liveTool: ILiveTool, iStage: IStage, iGeneric: IGeneric) {

    }
}