package dev.wuason.unearthMechanic.system

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.compatibilities.Compatibility
import org.bukkit.Location

interface IStageManager {

    fun dropItems(loc: Location, stage: IStage)

    fun isLastStage(generic: IGeneric, stage: IStage): Boolean

    fun getCompatibilities(): Array<Compatibility>

    fun getCompatibilitiesLoaded(): MutableList<Compatibility>

}