package dev.wuason.unearthMechanic.system.animations

import org.bukkit.inventory.ItemStack

/**
 * The IAnimationRunner interface provides methods to control and interact with animations for players.
 */
interface IAnimationRunner {
    /**
     * Sets the item in the main hand of the player.
     *
     * @param item The `ItemStack` to be set in the main hand.
     */
    fun setItemMainHand(item: ItemStack)

    /**
     * Retrieves the item held in the main hand.
     *
     * @return The ItemStack representing the item that the player is holding in their main hand.
     */
    fun getItemMainHand(): ItemStack

    /**
     * Checks if the animation is currently running.
     *
     * @return true if the animation is running, false otherwise.
     */
    fun isRunning(): Boolean

    /**
     * Checks whether the current state or configuration is valid.
     *
     * @return true if the state or configuration is valid, false otherwise
     */
    fun isValid(): Boolean

    /**
     * Updates the data of the item in the main hand of the player associated with the current animation.
     * This method is typically called after changes are made to the main hand item to ensure the
     * animation state remains in sync with the item's current state.
     */
    fun updateItemMainHandData()
}