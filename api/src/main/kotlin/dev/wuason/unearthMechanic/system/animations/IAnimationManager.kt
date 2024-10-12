package dev.wuason.unearthMechanic.system.animations

import dev.wuason.unearthMechanic.config.IAnimation
import org.bukkit.entity.Player
import java.util.*

/**
 * Interface defining the contract for managing animations for players.
 */
interface IAnimationManager {
    /**
     * Checks if the specified player is currently playing any animation.
     *
     * @param player the player to check for active animations
     * @return true if the player is currently playing an animation, false otherwise
     */
    fun isAnimating(player: Player): Boolean
    /**
     * Retrieves the ongoing animation for a specified player.
     *
     * @param player The player whose animation is to be retrieved.
     * @return The animation runner for the specified player, or null if the player is not currently undergoing any animation.
     */
    fun getAnimation(player: Player): IAnimationRunner?
    /**
     * Plays the specified animation for the given player.
     *
     * @param player The player for whom the animation will be played.
     * @param animation The animation to be played.
     */
    fun playAnimation(player: Player, animation: IAnimation)
    /**
     * Stops the currently running animation for the given player.
     *
     * @param player The player whose animation is to be stopped.
     */
    fun stopAnimation(player: Player)
    /**
     * Retrieves the current map of players to their associated animation runners.
     *
     * @return A WeakHashMap that maps each Player to their respective IAnimationRunner.
     */
    fun getAnimations(): WeakHashMap<Player, IAnimationRunner>
}