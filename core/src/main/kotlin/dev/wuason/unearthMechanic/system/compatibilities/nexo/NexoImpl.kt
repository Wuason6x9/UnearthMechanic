package dev.wuason.unearthMechanic.system.compatibilities.nexo

import com.nexomc.nexo.api.NexoBlocks
import com.nexomc.nexo.api.NexoFurniture
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockBreakEvent
import com.nexomc.nexo.api.events.custom_block.noteblock.NexoNoteBlockInteractEvent
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockBreakEvent
import com.nexomc.nexo.api.events.custom_block.stringblock.NexoStringBlockInteractEvent
import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent
import com.nexomc.nexo.utils.drops.Drop
import dev.wuason.libs.adapter.AdapterComp
import dev.wuason.libs.adapter.AdapterData
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.UnearthMechanicPlugin
import dev.wuason.unearthMechanic.config.*
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.StageData
import dev.wuason.unearthMechanic.system.StageManager
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import dev.wuason.unearthMechanic.utils.Utils
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

import java.util.concurrent.ConcurrentHashMap

class NexoImpl(
    pluginName: String,
    private val core: UnearthMechanicPlugin,
    private val stageManager: StageManager,
    adapterComp: AdapterComp,

    private val locks: MutableMap<String, Any> = ConcurrentHashMap()
): ICompatibility(
    pluginName,
    adapterComp
) {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInteractBlock(event: NexoNoteBlockInteractEvent) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInteractBlock(event: NexoStringBlockInteractEvent) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInteractFurniture(event: NexoFurnitureInteractEvent) {
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
    fun onBreakBlock(event: NexoNoteBlockBreakEvent) {
        StageData.removeStageData(event.block)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBreakBlock(event: NexoStringBlockBreakEvent) {
        StageData.removeStageData(event.block)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onFurnitureBreak(event: NexoFurnitureBreakEvent) {
        StageData.removeStageData(event.baseEntity.location)
    }


    private fun placeBlock(itemAdapterData: AdapterData, location: Location) {
        NexoBlocks.place(itemAdapterData.id, location)
    }

    private fun breakBlock(location: Location, player: Player) {
        NexoBlocks.remove(location, player)
    }

    private fun placeFurniture(
        itemAdapterData: AdapterData,
        location: Location,
        blockFace: BlockFace,
        yaw: Float
    ) {
        NexoFurniture.furnitureMechanic(itemAdapterData.id)?.place(location, yaw, blockFace)
    }
    private fun placeFurniture(
        itemAdapterData: AdapterData,
        location: Location,
    ) {
        NexoFurniture.furnitureMechanic(itemAdapterData.id)?.place(location, 0f, BlockFace.UP)
    }

    private fun breakFurniture(entity: Entity, player: Player, id: String) {
        NexoFurniture.remove(entity, player, Drop(mutableListOf(), silktouch = false, fortune = false, sourceID = id))
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
        val key = "${loc.blockX},${loc.blockY},${loc.blockZ},${loc.world?.name}"
        val mutex = locks.computeIfAbsent(key) { _: String -> Any() }

        synchronized(mutex) {

            if (event is NexoFurnitureInteractEvent) {
                event.isCancelled = true

                val entity = event.baseEntity

                // Verify that the entity is not already processed.
                if (!entity.isValid || entity.isDead) return

                // Safely removes furniture
                breakFurniture(event.baseEntity, player, event.mechanic.itemID)

                // Replaces
                placeFurniture(itemAdapterData, loc, event.baseEntity.facing, event.baseEntity.location.yaw)
            } else {
                placeFurniture(itemAdapterData, loc)
            }
        }
        locks.remove(key)
    }

    override fun handleRemove(
        player: Player,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        if (event is NexoNoteBlockInteractEvent || event is NexoStringBlockInteractEvent) {
            loc.block.type = org.bukkit.Material.AIR
        }
        if (event is NexoFurnitureInteractEvent) {
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
        if (event is NexoNoteBlockInteractEvent) {
            val block: Block = event.block
            return Utils.calculateHashCode(
                block.type.hashCode(),
                block.blockData.hashCode(),
                block.state.hashCode(),
                event.mechanic.itemID.hashCode(),
                block.hashCode()
            )
        }
        if (event is NexoStringBlockInteractEvent) {
            val block: Block = event.block
            return Utils.calculateHashCode(
                block.type.hashCode(),
                block.blockData.hashCode(),
                block.state.hashCode(),
                event.mechanic.itemID.hashCode(),
                block.hashCode()
            )
        }

        if (event is NexoFurnitureInteractEvent) {
            val entity: Entity = event.baseEntity
            return Utils.calculateHashCode(
                entity.type.hashCode(),
                entity.isDead.hashCode(),
                entity.uniqueId.hashCode(),
                entity.hashCode(),
                entity.facing.hashCode(),
                entity.location.hashCode()
            )
        }
        return -1
    }

    override fun getItemHand(event: Event): ItemStack? {
        if (event is NexoNoteBlockInteractEvent) {
            return event.itemInHand
        }
        if (event is NexoStringBlockInteractEvent) {
            return event.itemInHand
        }
        if (event is NexoFurnitureInteractEvent) {
            return event.itemInHand
        }
        return null
    }

    override fun getBlockFace(event: Event): BlockFace? {
        if (event is NexoNoteBlockInteractEvent) {
            return event.blockFace
        }
        if (event is NexoStringBlockInteractEvent) {
            return event.blockFace
        }
        return null
    }

}