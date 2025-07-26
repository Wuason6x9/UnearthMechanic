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
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture
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
import java.util.UUID

class NexoImpl(
    pluginName: String,
    private val core: UnearthMechanicPlugin,
    private val stageManager: StageManager,
    adapterComp: AdapterComp
): ICompatibility(
    pluginName,
    adapterComp
) {

    private val removingMap = mutableSetOf<UUID>()

    override fun isRemoving(uuid: UUID): Boolean {
        return removingMap.contains(uuid)
    }

    override fun setRemoving(uuid: UUID) {
        removingMap.add(uuid)
    }

    override fun clearRemoving(uuid: UUID) {
        removingMap.remove(uuid)
    }

    override fun getFurnitureUUID(location: Location): UUID? {
        val world = location.world ?: return null

        val entities = world.getNearbyEntities(location, 1.0, 1.0, 1.0)
        for (entity in entities) {
            try {
                val furniture = NexoFurniture.isFurniture(entity)
                if (furniture != null) {
                    return entity.uniqueId
                }
            } catch (e: Exception) {
                // Si lanza error es porque esa entidad no es un mueble válido
                continue
            }
        }

        return null
    }

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
        if (isRemoving(event.baseEntity.uniqueId)) {
            event.isCancelled = true
            return
        }

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

    @EventHandler(priority = EventPriority.LOWEST)
    fun onFurnitureBreak(event: NexoFurnitureBreakEvent) {
        val uuid = event.baseEntity.uniqueId

        if (isRemoving(uuid)) {
            event.isCancelled = true
            return
        }

        StageData.removeStageData(event.baseEntity.location)

        Bukkit.getScheduler().runTaskLater(core, Runnable {
            clearRemoving(uuid)
        }, 2L)
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
        if (event is NexoFurnitureInteractEvent) {
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