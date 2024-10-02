package dev.wuason.unearthMechanic.system.compatibilities.or

import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.*
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.StageData
import dev.wuason.unearthMechanic.system.StageManager
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import dev.wuason.unearthMechanic.utils.Utils
import io.th0rgal.oraxen.api.OraxenBlocks
import io.th0rgal.oraxen.api.OraxenFurniture
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureBreakEvent
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureInteractEvent
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockBreakEvent
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockInteractEvent
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockBreakEvent
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockInteractEvent
import io.th0rgal.oraxen.utils.drops.Drop
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class OraxenImpl(private val core: UnearthMechanic, private val stageManager: StageManager) : ICompatibility {

    @EventHandler
    fun onInteractBlock(event: OraxenNoteBlockInteractEvent) {
        if (event.player != null && event.hand == EquipmentSlot.HAND && event.action == Action.RIGHT_CLICK_BLOCK) {
            stageManager.interact(
                event.player,
                "or:" + event.mechanic.itemID,
                event.block.location,
                event,
                this
            )
        }
    }

    @EventHandler
    fun onInteractBlock(event: OraxenStringBlockInteractEvent) {
        if (event.player != null && event.hand == EquipmentSlot.HAND) {
            stageManager.interact(
                event.player,
                "or:" + event.mechanic.itemID,
                event.block.location,
                event,
                this
            )
        }
    }

    @EventHandler
    fun onInteractFurniture(event: OraxenFurnitureInteractEvent) {
        if (event.player != null && event.hand == EquipmentSlot.HAND) {
            stageManager.interact(
                event.player,
                "or:" + event.mechanic.itemID,
                event.baseEntity.location,
                event,
                this
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBreakBlock(event: OraxenNoteBlockBreakEvent) {
        StageData.removeStageData(event.block)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBreakBlock(event: OraxenStringBlockBreakEvent) {
        StageData.removeStageData(event.block)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onFurnitureBreak(event: OraxenFurnitureBreakEvent) {
        StageData.removeStageData(event.baseEntity.location)
    }


    private fun placeBlock(adapterId: String, location: Location) {
        OraxenBlocks.place(adapterId.replace("or:", ""), location)
    }

    private fun breakBlock(location: Location, player: Player) {
        OraxenBlocks.remove(location, player)
    }

    private fun placeFurniture(
        adapterId: String,
        location: Location,
        blockFace: BlockFace,
        yaw: Float
    ) {
        OraxenFurniture.getFurnitureMechanic(adapterId.replace("or:", "")).place(location, yaw, blockFace)
    }

    private fun breakFurniture(entity: Entity, player: Player, id: String) {
        OraxenFurniture.remove(entity, player, Drop(mutableListOf(), false, false, id))
    }

    override fun loaded(): Boolean {
        return Bukkit.getPluginManager().getPlugin("Oraxen") != null
    }

    override fun enabled(): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled("Oraxen")
    }

    override fun name(): String {
        return "Oraxen"
    }

    override fun adapterId(): String {
        return "or"
    }

    override fun handleStage(
        player: Player,
        itemId: String,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        if (generic is IBlock) {
            handleBlockStage(player, itemId, event, loc, toolUsed, generic, stage)
        } else if (generic is IFurniture) {
            handleFurnitureStage(player, itemId, event, loc, toolUsed, generic, stage)
        }
    }

    private fun handleBlockStage(
        player: Player,
        itemId: String,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        placeBlock(itemId, loc)
    }

    private fun handleFurnitureStage(
        player: Player,
        itemId: String,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        if (event is OraxenFurnitureInteractEvent) {
            breakFurniture(event.baseEntity, player, event.mechanic.itemID)
            placeFurniture(itemId, loc, event.baseEntity.facing, event.baseEntity.location.yaw)
        }
    }

    override fun handleRemove(
        player: Player,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        if (generic is IBlock) {
            breakBlock(loc, player)
        } else if (generic is IFurniture) {
            if (event is OraxenFurnitureInteractEvent) {
                breakFurniture(event.baseEntity, player, event.mechanic.itemID)
            }
        }
    }

    override fun hashCode(
        player: Player,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: Int
    ): Int {
        if (generic is IBlock) {
            if (event is OraxenNoteBlockInteractEvent) {
                val block: Block = event.block
                return Utils.calculateHashCode(
                    block.type.hashCode(),
                    block.blockData.hashCode(),
                    block.state.hashCode(),
                    event.mechanic.itemID.hashCode(),
                    block.hashCode()
                )
            }
            if (event is OraxenStringBlockInteractEvent) {
                val block: Block = event.block
                return Utils.calculateHashCode(
                    block.type.hashCode(),
                    block.blockData.hashCode(),
                    block.state.hashCode(),
                    event.mechanic.itemID.hashCode(),
                    block.hashCode()
                )
            }
        }

        if (generic is IFurniture && event is OraxenFurnitureInteractEvent) {
            val entity: Entity = event.baseEntity
            var result: Int = entity.type.hashCode()
            result = 31 * result + entity.isDead.hashCode()
            result = 31 * result + entity.uniqueId.hashCode()
            result = 31 * result + entity.hashCode()
            result = 31 * result + entity.facing.hashCode()
            result = 31 * result + entity.location.hashCode()
            event.block?.let {
                result = 31 * result + it.type.hashCode()
                result = 31 * result + it.blockData.hashCode()
                result = 31 * result + it.state.hashCode()
                result = 31 * result + it.hashCode()
            }
            event.interactionEntity?.let {
                result = 31 * result + it.type.hashCode()
                result = 31 * result + it.isDead.hashCode()
                result = 31 * result + it.uniqueId.hashCode()
                result = 31 * result + it.hashCode()
                result = 31 * result + it.facing.hashCode()
                result = 31 * result + it.location.hashCode()
            }
            return result
        }
        return -1
    }

    override fun getItemHand(event: Event): ItemStack? {
        if (event is OraxenNoteBlockInteractEvent) {
            return event.itemInHand
        }
        if (event is OraxenStringBlockInteractEvent) {
            return event.itemInHand
        }
        if (event is OraxenFurnitureInteractEvent) {
            return event.itemInHand
        }
        return null
    }

}