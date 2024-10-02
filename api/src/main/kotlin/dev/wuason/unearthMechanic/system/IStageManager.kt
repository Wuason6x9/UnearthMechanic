package dev.wuason.unearthMechanic.system

import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility

interface IStageManager {

    fun getCompatibilities(): Array<ICompatibility>

    fun getCompatibilitiesLoaded(): MutableList<ICompatibility>

    fun getCompatibilityByAdapterId(adapterId: String): ICompatibility?

    fun isSimilarCompatibility(adapterId: String, compatibility: ICompatibility): Boolean

    fun getAnimator(): IAnimator

}