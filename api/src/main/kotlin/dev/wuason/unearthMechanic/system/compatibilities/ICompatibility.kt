package dev.wuason.unearthMechanic.system.compatibilities

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

/**
 * Interface representing compatibility handling in the system. This interface must be implemented
 * by classes that handle compatibility-related events and actions within the system.
 */
interface ICompatibility : Listener {
    /**
     * Checks if the compatibility is loaded.
     *
     * @return `true` if the compatibility is loaded, `false` otherwise.
     */
    fun loaded(): Boolean
    /**
     * Checks if the compatibility is enabled.
     *
     * @return true if the compatibility is enabled, false otherwise.
     */
    fun enabled(): Boolean
    /**
     * Returns the name associated with this compatibility interface.
     *
     * @return A string representing the name.
     */
    fun name(): String
    /**
     * Returns a unique identifier for the adapter.
     *
     * @return A string representing the adapter's unique identifier.
     */
    fun adapterId(): String
    /**
     * Handles the progression of stages for a given player and item within a specified location.
     *
     * @param player The player associated with the event.
     * @param itemId The ID of the item being used.
     * @param event The event triggering the stage handling.
     * @param loc The location where the event occurred.
     * @param toolUsed The tool being used by the player in the event.
     * @param generic The generic object containing configuration and tools.
     * @param stage The current stage to be handled.
     */
    fun handleStage(player: Player, itemId: String, event: Event, loc: Location, toolUsed: ILiveTool, generic: IGeneric, stage: IStage)
    /**
     * Handles the removal of an item from a specific stage when an event occurs.
     *
     * @param player The player involved in the event.
     * @param event The event that triggered the removal.
     * @param loc The location where the event took place.
     * @param toolUsed The tool used by the player during the event.
     * @param generic The generic item involved in the stage.
     * @param stage The specific stage from which the item is to be removed.
     */
    fun handleRemove(player: Player, event: Event, loc: Location, toolUsed: ILiveTool, generic: IGeneric, stage: IStage)
    /**
     * Computes the hash code for the provided parameters.
     *
     * @param player The player involved in the event.
     * @param event The event that triggered the hash code computation.
     * @param loc The location related to the event.
     * @param toolUsed The tool used by the player.
     * @param generic The generic item related to the event.
     * @param stage The current stage of the event.
     * @return The computed hash code as an integer.
     */
    fun hashCode(player: Player, event: Event, loc: Location, toolUsed: ILiveTool, generic: IGeneric, stage: Int): Int
    /**
     * Retrieves the ItemStack in the player's hand during an event.
     *
     * @param event The event during which the item in hand is to be retrieved.
     * @return The ItemStack in the player's hand, or null if not available.
     */
    fun getItemHand(event: Event) : ItemStack?
    /**
     * Retrieves the block face for the given event.
     *
     * @param event The event for which the block face should be determined.
     * @return The block face associated with the event, or null if none is found.
     */
    fun getBlockFace(event: Event) : org.bukkit.block.BlockFace?
    /**
     * This method is triggered when the class implementing the ICompatibility interface is loaded.
     * It can be used to perform any necessary initialization procedures.
     */
    open fun onLoad() {}


}