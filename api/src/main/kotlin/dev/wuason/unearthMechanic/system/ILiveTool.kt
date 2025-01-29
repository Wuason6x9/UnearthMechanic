package dev.wuason.unearthMechanic.system

import dev.wuason.unearthMechanic.config.ITool
import org.bukkit.inventory.ItemStack

/**
 * Interface representing a live tool that can be used by a player.
 */
interface ILiveTool {
    /**
     * Retrieves the item currently held in the player's main hand.
     *
     * @return The ItemStack representing the item in the player's main hand, or null if the hand is empty.
     */
    fun getItemMainHand(): ItemStack?
    /**
     * Retrieves the tool information associated with the player's main hand item.
     *
     * @return an instance of ITool representing the tool's meta-information.
     */
    fun getITool(): ITool
    /**
     * Sets the item held in the main hand of the player.
     *
     * @param item The ItemStack to be set in the player's main hand.
     */
    fun setItemMainHand(item: ItemStack)
    /**
     * Checks if the current state of this tool is valid.
     *
     * @return true if the tool is in a valid state; false otherwise.
     */
    fun isValid(): Boolean

    fun isOriginalItem(): Boolean
}