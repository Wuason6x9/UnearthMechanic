package dev.wuason.unearthMechanic.system.compatibilities.ia

import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomFurniture
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent
import dev.lone.itemsadder.api.ItemsAdder
import dev.wuason.libs.adapter.Adapter
import dev.wuason.libs.adapter.AdapterComp
import dev.wuason.libs.adapter.AdapterData
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.UnearthMechanicPlugin
import dev.wuason.unearthMechanic.config.*
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.IStageManager
import dev.wuason.unearthMechanic.system.StageData
import dev.wuason.unearthMechanic.system.StageManager
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import dev.wuason.unearthMechanic.system.features.Features
import dev.wuason.unearthMechanic.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.UUID

class ItemsAdderImpl(
    pluginName: String,
    private val core: UnearthMechanicPlugin,
    private val stageManager: StageManager,
    adapterComp: AdapterComp
) : ICompatibility(
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

        // Revisamos entidades en el radio cercano (1 bloque)
        val entities = world.getNearbyEntities(location, 1.0, 1.0, 1.0)

        for (entity in entities) {
            try {
                val furniture = CustomFurniture.byAlreadySpawned(entity)
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

    @EventHandler
    fun onInteractBlock(event: CustomBlockInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.hand == EquipmentSlot.HAND) {
            stageManager.interact(
                event.player,
                "ia:" + event.namespacedID,
                event.blockClicked.location,
                event,
                this
            )
        }
    }

    @EventHandler
    fun onInteractFurniture(event: FurnitureInteractEvent) {
        if (isRemoving(event.bukkitEntity.uniqueId)) {
            event.isCancelled = true
            return
        }

        val currentTick = Bukkit.getCurrentTick().toLong()
        val furnitureUuid = getFurnitureUUID(event.bukkitEntity.location) ?: return
        Bukkit.getConsoleSender().sendMessage("[UM] onInteractFurniture aplicado para $furnitureUuid en $currentTick")

        if (event.bukkitEntity != null) {
            val adapterId = "ia:" + event.namespacedID
            stageManager.interact(
                event.player,
                adapterId,
                event.bukkitEntity.location,
                event,
                this
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: CustomBlockBreakEvent) {
        StageData.removeStageData(event.block)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onFurnitureBreak(event: FurnitureBreakEvent) {
        val uuid = event.bukkitEntity.uniqueId

        if (isRemoving(uuid)) {
            event.isCancelled = true
            return
        }

        StageData.removeStageData(event.bukkitEntity.location)

        val currentTick = Bukkit.getCurrentTick().toLong()
        val furnitureUuid = getFurnitureUUID(event.bukkitEntity.location) ?: return
        Bukkit.getConsoleSender().sendMessage("[UM] FurnitureBreakEvent aplicado para $furnitureUuid en $currentTick")

        Bukkit.getScheduler().runTaskLater(core, Runnable {
            clearRemoving(uuid)
        }, 2L)
    }

    private fun placeBlock(adapterId: String, location: Location?) {
        CustomBlock.place(adapterId.replace("ia:", ""), location)
    }

    private fun breakBlock(location: Location?) {
        CustomBlock.remove(location)
    }

    private fun replaceFurniture(adapterId: String, entity: Entity?) {
        val customFurniture = CustomFurniture.byAlreadySpawned(entity)
        customFurniture!!.replaceFurniture(adapterId.replace("ia:", ""))
    }

    private fun breakFurniture(entity: Entity?, player: Player?) {
        CustomFurniture.remove(entity, false)
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
        Bukkit.getConsoleSender().sendMessage("[UM] handleStage en $loc - TICK: ${Bukkit.getCurrentTick()}")
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
        placeBlock(itemAdapterData.id, loc)
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
        if (event is FurnitureInteractEvent) {
            event.isCancelled = true

            val entityEvent: Entity = event.bukkitEntity
            if(!entityEvent.isValid) {
                //Bukkit.getConsoleSender().sendMessage(" NO ES VALIDO EL FURNITURE" +loc)
                return
            }
            event.furniture?.remove(false)
            CustomFurniture.spawn(itemAdapterData.id, loc.block)?.let { customFurniture ->
                //Bukkit.getConsoleSender().sendMessage("[IA] spawn furniture at $loc - adapter ${itemAdapterData.id}")
                val entity: Entity = customFurniture.entity ?: return
                entity.setRotation(entityEvent.location.yaw, entityEvent.location.pitch)

                if (entityEvent is ItemFrame && entity is ItemFrame) {
                    entity.rotation = entityEvent.rotation
                }
            }
        } else {
            CustomFurniture.spawn(itemAdapterData.id, loc.block)
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
        if (event is CustomBlockInteractEvent) {
            breakBlock(loc)
        }
        if (event is FurnitureInteractEvent) {
            //Bukkit.getConsoleSender().sendMessage("[IA] Furniture removido en $loc")
            val uuid = event.bukkitEntity.uniqueId
            clearRemoving(uuid)

            event.furniture?.remove(false)
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
        if (event is CustomBlockInteractEvent) {
            val block: Block = event.blockClicked
            return Utils.calculateHashCode(
                block.location.hashCode(),
                block.hashCode(),
                block.type.hashCode(),
                block.blockData.hashCode(),
                block.state.hashCode()
            )
        }
        if (event is FurnitureInteractEvent) {
            val entity: Entity = event.bukkitEntity
            return Utils.calculateHashCode(
                entity.location.hashCode(),
                entity.hashCode(),
                entity.type.hashCode(),
                entity.uniqueId.hashCode(),
                entity.isDead.hashCode(),
                entity.facing.hashCode()
            )
        }
        return -1
    }

    override fun getItemHand(event: Event): ItemStack? {
        if (event is CustomBlockInteractEvent) {
            return event.item
        }
        return null
    }

    override fun getBlockFace(event: Event): BlockFace? {
        if (event is CustomBlockInteractEvent) {
            return event.blockFace
        }
        return null
    }

}