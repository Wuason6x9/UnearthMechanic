package dev.wuason.unearthMechanic.system

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.compatibilities.Compatibility
import org.bukkit.Location
import org.bukkit.entity.Player

interface IStageManager {

    fun dropItems(loc: Location, stage: IStage)

    fun isLastStage(generic: IGeneric, stage: IStage): Boolean

    fun getCompatibilities(): Array<Compatibility>

    fun getCompatibilitiesLoaded(): MutableList<Compatibility>

    fun getCompatibilityByAdapterId(adapterId: String): Compatibility?

    fun isSimilarCompatibility(adapterId: String, compatibility: Compatibility): Boolean

    fun addItems(player: Player, stage: IStage)

}