package dev.wuason.unearthMechanic.events

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.IValidation
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Represents an event related to applying a stage which implements the Event interface and Cancellable.
 *
 * @property player The player associated with the event.
 * @property compatibility The compatibility handler for the event.
 * @property event The event triggering the stage handling.
 * @property loc The location where the event occurred.
 * @property toolUsed The tool being used by the player in the event.
 * @property generic The generic object containing configuration and tools.
 * @property iStage The current stage to be handled.
 */
class ApplyStageEvent(private val player: Player, private val compatibility: ICompatibility, private val event: Event, private val loc: Location, private val toolUsed: ILiveTool, private val generic: IGeneric, private val iStage: IStage): Event(), Cancellable {

    /**
     * Companion object for ApplyStageEvent providing handler-related functionality.
     */
//handler list
    companion object {
        /**
         * A static HandlerList to manage and retrieve event handlers for the ApplyStageEvent class.
         * This list is used to store all event handlers associated with ApplyStageEvent instances.
         */
        private val HANDLERS = HandlerList()

        /**
         * Retrieves the list of event handlers associated with this event type.
         *
         * @return the handler list for this event type.
         */
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    /**
     * Indicates whether the event has been cancelled.
     * By default, the value is set to `false`, meaning the event is not cancelled.
     * This variable is used to track the cancellation state of the event,
     * and it can be modified through the `setCancelled` method to either
     * cancel or reinstate the event.
     */
    private var isCancelled: Boolean = false

    /**
     * Indicates whether the current event has been cancelled.
     *
     * @return true if the event is cancelled, otherwise false.
     */

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    /**
     * Sets the cancellation state of the event.
     *
     * @param cancel Indicates whether the event should be canceled (`true` if it should be canceled, `false` otherwise).
     */

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    /**
     * Retrieves the HandlerList for the ApplyStageEvent.
     *
     * @return The HandlerList associated with this event.
     */
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    /**
     * Retrieves the player associated with the current event.
     *
     * @return The player involved in the event.
     */

    fun getPlayer(): Player {
        return player
    }

    /**
     * Retrieves the compatibility object associated with this event.
     *
     * @return the `ICompatibility` instance related to this event.
     */

    fun getCompatibility(): ICompatibility {
        return compatibility
    }

    /**
     * Retrieves the event executor associated with this ApplyStageEvent.
     *
     * @return The Event instance representing the event executor.
     */

    fun getEventExecutor(): Event {
        return event
    }

    /**
     * Retrieves the location where the event occurred.
     *
     * @return The location associated with the event.
     */
    fun getLocation(): Location {
        return loc
    }

    /**
     * Retrieves the tool used in the event.
     *
     * @return the tool used, represented as an instance of ILiveTool.
     */

    fun getToolUsed(): ILiveTool {
        return toolUsed
    }

    /**
     * Retrieves the generic configuration and tools for the current stage event.
     *
     * @return An instance of IGeneric containing the generic configuration and tools.
     */

    fun getGeneric(): IGeneric {
        return generic
    }

    /**
     * Retrieves the current stage (`IStage`) associated with this event.
     *
     * @return The current `IStage` instance.
     */

    fun getIStage(): IStage {
        return iStage
    }

    /**
     * Determines if the current stage is the last stage.
     *
     * @return true if the current stage is the last stage, false otherwise.
     */

    fun isLastStage(): Boolean {
        return iStage.getStage() == generic.getStages().size - 1
    }

    /**
     * Checks if the current stage is the first stage.
     *
     * @return `true` if the current stage is the first stage, `false` otherwise.
     */

    fun isFirstStage(): Boolean {
        return getIStage().getStage() == 0
    }
}