package dev.wuason.unearthMechanic.system.features

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event

/**
 * Abstract class representing a general feature with lifecycle methods
 * that can be overridden to implement custom behavior.
 */
abstract class AbstractFeature {

    /**
     * Invoked before applying a specific feature to a player. This method can be
     * overridden to add custom pre-apply logic.
     *
     * @param p The player to whom the feature is being applied.
     * @param comp The compatibility interface associated with the feature.
     * @param event The event triggering the feature.
     * @param loc The location where the event occurred.
     * @param liveTool The tool being used by the player.
     * @param iStage The current stage of the feature.
     * @param iGeneric General feature-related data.
     */
    open fun onPreApply(p: Player, comp: ICompatibility, event: Event, loc: Location, liveTool: ILiveTool, iStage: IStage, iGeneric: IGeneric) {

    }

    /**
     * Processes a stage for a player interaction with a live tool at a specific location.
     *
     * @param tick The current tick count.
     * @param p The player interacting with the stage.
     * @param comp The compatibility interface for handling different game systems.
     * @param event The event that triggered the process.
     * @param loc The location where the interaction is taking place.
     * @param liveTool The live tool the player is using.
     * @param iStage The stage being processed.
     * @param iGeneric Generic interface for retrieving various contextual elements.
     */
    open fun onProcess(tick: Long, p: Player, comp: ICompatibility, event: Event, loc: Location, liveTool: ILiveTool, iStage: IStage, iGeneric: IGeneric) {

    }

    /**
     * This method is invoked to apply a feature based on various game parameters.
     *
     * @param p the player involved in the event
     * @param comp the compatibility interface that interacts with the feature
     * @param event the event that triggered the feature application
     * @param loc the location where the event is happening
     * @param liveTool the tool currently being used by the player
     * @param iStage the stage of the feature being applied
     * @param iGeneric a generic interface that holds various feature-related data
     */
    open fun onApply(p: Player, comp: ICompatibility, event: Event, loc: Location, liveTool: ILiveTool, iStage: IStage, iGeneric: IGeneric) {

    }
}