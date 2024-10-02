package dev.wuason.unearthMechanic.system

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event

class Validation(
    private val player: Player,
    private val compatibility: ICompatibility,
    private val event: Event,
    private val loc: Location,
    private val toolUsed: LiveTool,
    private val generic: IGeneric,
    private val stage: IStage
): IValidation {
    private var valid = false
    private var first: Int? = null
    private var last: Int? = null

    override fun start() {
        first = compatibility.hashCode(player, event, loc, toolUsed, generic, stage.getStage())
    }

    override fun validate(): Boolean {
        valid = false
        if (first == null || first == -1) return false
        last = compatibility.hashCode(player, event, loc, toolUsed, generic, stage.getStage())
        if (last == -1) return false
        if (first == last) {
            valid = true
        }
        return isValid()
    }

    override fun isValid(): Boolean {
        return valid
    }
}