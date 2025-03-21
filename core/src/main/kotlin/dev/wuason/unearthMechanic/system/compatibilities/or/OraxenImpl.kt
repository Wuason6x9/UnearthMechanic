package dev.wuason.unearthMechanic.system.compatibilities.or

import dev.wuason.libs.adapter.Adapter
import dev.wuason.libs.adapter.AdapterComp
import dev.wuason.libs.adapter.AdapterData
import dev.wuason.unearthMechanic.UnearthMechanicPlugin
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

class OraxenImpl(
    pluginName: String,
    private val core: UnearthMechanicPlugin,
    private val stageManager: StageManager,
    adapterComp: AdapterComp
): ICompatibility(
    pluginName,
    adapterComp
) {

    @EventHandler
    fun onInteractBlock(event: OraxenNoteBlockInteractEvent) {
        if (event.hand == EquipmentSlot.HAND && event.action == Action.RIGHT_CLICK_BLOCK) {
            stageManager.interact(
                event.player,
                getPath(event.mechanic.itemID),
                event.block.location,
                event,
                this
            )
        }
    }

    @EventHandler
    fun onInteractBlock(event: OraxenStringBlockInteractEvent) {
        if (event.hand == EquipmentSlot.HAND) {
            stageManager.interact(
                event.player,
                getPath(event.mechanic.itemID),
                event.block.location,
                event,
                this
            )
        }
    }

    @EventHandler
    fun onInteractFurniture(event: OraxenFurnitureInteractEvent) {
        if (event.hand == EquipmentSlot.HAND) {
            stageManager.interact(
                event.player,
                getPath(event.mechanic.itemID),
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


    private fun placeBlock(itemAdapterData: AdapterData, location: Location) {
        OraxenBlocks.place(itemAdapterData.id, location)
    }

    private fun breakBlock(location: Location, player: Player) {
        OraxenBlocks.remove(location, player)
    }

    private fun placeFurniture(
        itemAdapterData: AdapterData,
        location: Location,
        blockFace: BlockFace,
        yaw: Float
    ) {
        OraxenFurniture.getFurnitureMechanic(itemAdapterData.id).place(location, yaw, blockFace)
    }
    private fun placeFurniture(
        itemAdapterData: AdapterData,
        location: Location,
    ) {
        OraxenFurniture.getFurnitureMechanic(itemAdapterData.id).place(location, 0f, BlockFace.UP)
    }

    private fun breakFurniture(entity: Entity, player: Player, id: String) {
        OraxenFurniture.remove(entity, player, Drop(mutableListOf(), false, false, id))
    }

    override fun handleStage(
        player: Player,
        itemAdapterData: AdapterData,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        if (stage is IBlockStage) {
            handleBlockStage(player, itemAdapterData, event, loc, toolUsed, generic, stage)
        } else if (stage is IFurnitureStage) {
            handleFurnitureStage(player, itemAdapterData, event, loc, toolUsed, generic, stage)
        }
    }

    private fun handleBlockStage(
        player: Player,
        itemAdapterData: AdapterData,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        placeBlock(itemAdapterData, loc)
    }

    private fun handleFurnitureStage(
        player: Player,
        itemAdapterData: AdapterData,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        if (event is OraxenFurnitureInteractEvent) {
            breakFurniture(event.baseEntity, player, event.mechanic.itemID)
            placeFurniture(itemAdapterData, loc, event.baseEntity.facing, event.baseEntity.location.yaw)
        } else {
            placeFurniture(itemAdapterData, loc)
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
        if (event is OraxenNoteBlockInteractEvent || event is OraxenStringBlockInteractEvent) {
            loc.block.type = org.bukkit.Material.AIR
        }
        if (event is OraxenFurnitureInteractEvent) {
            breakFurniture(event.baseEntity, player, event.mechanic.itemID)
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

        if (event is OraxenFurnitureInteractEvent) {
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

    override fun getBlockFace(event: Event): BlockFace? {
        if (event is OraxenNoteBlockInteractEvent) {
            return event.blockFace
        }
        if (event is OraxenStringBlockInteractEvent) {
            return event.blockFace
        }
        return null
    }

}