package dev.wuason.unearthMechanic.events

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.system.compatibilities.Compatibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ApplyStageEvent(private val player: Player, private val compatibility: Compatibility, private val event: Event, private val loc: Location, private val toolUsed: String, private val generic: IGeneric, private val stage: Int): Event(), Cancellable {

    //handler list
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    private var isCancelled: Boolean = false

    /**
     * Check if the event is cancelled
     * @return if the event is cancelled
     */

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    /**
     * Set the event as cancelled or not
     * @param cancel the state of the event
     */

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    /**
     * Get the player that triggered the event
     * @return the player that triggered the event
     */

    fun getPlayer(): Player {
        return player
    }

    /**
     * Get the compatibility of the event (ItemsAdder, Oraxen, etc.)
     * @return the compatibility of the event
     */

    fun getCompatibility(): Compatibility {
        return compatibility
    }

    /**
     * Get the event executor (OraxenNoteBlockInteract, OraxenStringBlockInteract, CustomBlockInteract....)
     * @return the event executor
     */

    fun getEventExecutor(): Event {
        return event
    }

    fun getLocation(): Location {
        return loc
    }

    /**
     * Get the tool used in the event
     * @return the tool used in the event
     */

    fun getToolUsed(): String {
        return toolUsed
    }

    /**
     * Get the generic data of the event
     * @return the generic data of the event
     */

    fun getGeneric(): IGeneric {
        return generic
    }

    /**
     * Get the current stage of the event
     * @return the current stage of the event
     */

    fun getStage(): Int {
        return stage
    }

    /**
     * Check if the current stage is the last stage
     * @return if the current stage is the last stage
     */

    fun isLastStage(): Boolean {
        return stage == generic.getStages().size - 1
    }

    /**
     * Check if the current stage is the first stage
     * @return if the current stage is the first stage
     */

    fun isFirstStage(): Boolean {
        return stage == 0
    }
}