package dev.wuason.unearthMechanic.system

import dev.wuason.unearthMechanic.system.animations.IAnimationManager
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility

/**
 * Manages stages and their compatibilities, providing functionalities to retrieve and check compatibility information,
 * handle animations, and interact with loaded compatibilities.
 */
interface IStageManager {

    /**
     * Retrieves an array of available compatibilities.
     *
     * @return An array of `ICompatibility` instances representing the available compatibilities.
     */
    fun getCompatibilities(): Array<ICompatibility>

    /**
     * Retrieves a mutable list of loaded compatibility interfaces.
     *
     * @return A MutableList containing instances of ICompatibility that represent the loaded compatibilities.
     */
    fun getCompatibilitiesLoaded(): MutableList<ICompatibility>

    /**
     * Retrieves the compatibility information associated with the specified adapter ID.
     *
     * @param adapterId The unique identifier of the adapter whose compatibility is to be fetched.
     * @return The ICompatibility instance corresponding to the provided adapter ID, or null if no matching compatibility is found.
     */
    fun getCompatibilityByAdapterId(adapterId: String): ICompatibility?

    /**
     * Checks if the given compatibility is similar to the compatibility associated with the specified adapter ID.
     *
     * @param adapterId The unique identifier of the adapter.
     * @param compatibility The compatibility instance to be checked against.
     * @return `true` if the given compatibility matches the compatibility associated with the adapter ID, `false` otherwise.
     */
    fun isSimilarCompatibility(adapterId: String, compatibility: ICompatibility): Boolean

    /**
     * Retrieves the animation manager responsible for handling player animations.
     *
     * @return the animation manager instance
     */
    fun getAnimator(): IAnimationManager

}