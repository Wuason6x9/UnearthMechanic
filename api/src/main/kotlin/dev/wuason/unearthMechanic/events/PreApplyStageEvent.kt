package dev.wuason.unearthMechanic.events

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Represents an event that occurs before a stage is applied.
 *
 * @property player The player associated with the event.
 * @property compatibility The compatibility handler for the event.
 * @property event The triggering event.
 * @property loc The location where the event occurs.
 * @property toolUsed The tool used by the player during the event.
 * @property generic The generic object containing event details.
 * @property iStage The stage related to the event.
 */
class PreApplyStageEvent(private val player: Player, private val compatibility: ICompatibility, private val event: Event, private val loc: Location, private val toolUsed: ILiveTool, private val generic: IGeneric, private val iStage: IStage): Event(), Cancellable {

    /**
     * Companion object for handling static members and functions related to event handling.
     */
//handler list
    companion object {
        /**
         * A singleton object to manage and retrieve all registered handlers for the PreApplyStageEvent.
         */
        private val HANDLERS = HandlerList()

        /**
         * Retrieves the list of handlers associated with this event.
         *
         * @return The list of handlers.
         */
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    /**
     * Indicates whether the event has been cancelled.
     *
     * This property is used to check if the current event should be aborted,
     * preventing any further processing or actions associated with it.
     */
    private var isCancelled: Boolean = false

    /**
     * Checks if the event has been cancelled.
     *
     * @return `true` if the event is cancelled, `false` otherwise.
     */

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    /**
     * Sets the cancellation state of the event.
     *
     * @param cancel A boolean value indicating whether the event should be cancelled.
     *               If true, the event is cancelled; otherwise, it is not.
     */

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    /**
     * Retrieves the list of handlers associated with this event.
     *
     * @return The list of handlers for this event.
     */
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    /**
     * Retrieves the player associated with this event.
     *
     * @return The player involved in the event.
     */

    fun getPlayer(): Player {
        return player
    }

    /**
     * Retrieves the compatibility instance associated with this event.
     *
     * @return The ICompatibility instance linked with this event.
     */

    fun getCompatibility(): ICompatibility {
        return compatibility
    }

    /**
     * Retrieves the event executor associated with this event.
     *
     * @return The Event executor instance.
     */

    fun getEventExecutor(): Event {
        return event
    }

    /**
     * Retrieves the location associated with the event.
     *
     * @return The Location where the event occurred.
     */
    fun getLocation(): Location {
        return loc
    }

    /**
     * Retrieves the tool that was used in the event.
     *
     * @return The instance of ILiveTool representing the tool used.
     */

    fun getToolUsed(): ILiveTool {
        return toolUsed
    }

    /**
     * Retrieves an instance of IGeneric associated with this event.
     *
     * @return The IGeneric instance containing configuration and tools.
     */

    fun getGeneric(): IGeneric {
        return generic
    }

    /**
     * Retrieves the current stage of the event.
     *
     * @return The current IStage instance associated with this event.
     */

    fun getIStage(): IStage {
        return iStage
    }

    /**
     * Determines if the current stage is the last stage in the sequence.
     *
     * @return `true` if the current stage is the last stage, `false` otherwise.
     */

    fun isLastStage(): Boolean {
        return iStage.getStage() == generic.getStages().size - 1
    }

    /**
     * Checks whether the current stage is the first stage.
     *
     * @return true if the current stage is the first stage (stage 0), false otherwise.
     */

    fun isFirstStage(): Boolean {
        return getIStage().getStage() == 0
    }
}