package dev.wuason.unearthMechanic.config

import dev.wuason.unearthMechanic.events.PreApplyStageEvent
import dev.wuason.unearthMechanic.system.LiveTool
import dev.wuason.unearthMechanic.system.StageData
import dev.wuason.unearthMechanic.system.Validation
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import dev.wuason.unearthMechanic.system.features.Features
import dev.wuason.unearthMechanic.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.RayTraceResult
import kotlin.random.Random

class Stage(
    private val stage: Int, private val itemId: String?, private val drops: List<Drop>, private val remove: Boolean, private val removeItemMainHand: Boolean,
    private val durabilityToRemove: Int,
    private val usagesIaToRemove: Int, private val onlyOneDrop: Boolean,
    private val reduceItemHand: Int, private val items: List<Item>, private val onlyOneItem: Boolean, private val sounds: List<Sound>,
    private val delay: Long, private val toolAnimDelay: Boolean
) : IStage {

    override fun isRemoveItemMainHand(): Boolean {
        return removeItemMainHand
    }

    override fun getUsagesIaToRemove(): Int {
        return usagesIaToRemove
    }

    override fun isRemove(): Boolean {
        return remove
    }

    override fun isOnlyOneDrop(): Boolean {
        return onlyOneDrop
    }

    override fun getDurabilityToRemove(): Int {
        return durabilityToRemove
    }

    override fun getItemId(): String? {
        return itemId
    }

    override fun getStage(): Int {
        return stage
    }

    override fun getDrops(): List<Drop> {
        return drops
    }

    override fun getReduceItemHand(): Int {
        return reduceItemHand
    }

    override fun getItems(): List<Item> {
        return items
    }

    override fun isOnlyOneItem(): Boolean {
        return onlyOneItem
    }

    override fun getSounds(): List<Sound> {
        return sounds
    }

    override fun getDelay(): Long {
        return delay
    }

    override fun isToolAnimDelay(): Boolean {
        return toolAnimDelay
    }

    override fun dropItems(loc: Location) {
        if(drops.isEmpty()) return
        if (isOnlyOneDrop()) {
            drops[Random.nextInt(drops.size)].dropItem(loc, true)
        }
        drops.forEach { it.dropItem(loc, true) }
    }

    override fun addItems(player: Player) {
        if(items.isEmpty()) return
        if (isOnlyOneItem()) {
            items[Random.nextInt(items.size)].addItem(player, true)
        }
        items.forEach { it.addItem(player, true) }
    }

    fun getMaxCorrectDelay(toolUsed: LiveTool): Long {
        return if (toolUsed.getITool().getDelay() > 0) {
            toolUsed.getITool().getDelay()
        } else if (getDelay() > 0) {
            getDelay()
        } else {
            return -1
        }
    }
}
