package dev.wuason.unearthMechanic.system.compatibilities.ce

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
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture
import net.momirealms.craftengine.bukkit.api.event.CustomBlockBreakEvent
import net.momirealms.craftengine.bukkit.api.event.CustomBlockInteractEvent
import net.momirealms.craftengine.bukkit.api.event.FurnitureBreakEvent
import net.momirealms.craftengine.bukkit.api.event.FurnitureInteractEvent
import net.momirealms.craftengine.core.entity.player.InteractionHand
import net.momirealms.craftengine.libraries.nbt.CompoundTag
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

import java.util.concurrent.ConcurrentHashMap
import net.momirealms.craftengine.core.util.Key
import net.momirealms.craftengine.core.entity.furniture.AnchorType
import net.momirealms.craftengine.core.entity.furniture.CustomFurniture
import java.util.UUID

class CraftEngineImpl(
    pluginName: String,
    private val core: UnearthMechanicPlugin,
    private val stageManager: StageManager,
    adapterComp: AdapterComp,
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

        val entities = world.getNearbyEntities(location, 1.0, 1.0, 1.0)
        for (entity in entities) {
            try {
                val furniture = CraftEngineFurniture.getLoadedFurnitureByBaseEntity(entity)
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

    override fun isValid(location: Location): Boolean {
        val world = location.world ?: return false
        val nearby = world.getNearbyEntities(location, 1.0, 1.0, 1.0)

        for (entity in nearby) {
            try {
                val furniture = CraftEngineFurniture.getLoadedFurnitureByBaseEntity(entity)
                if (furniture != null && entity.isValid && !entity.isDead) {
                    return true
                }
            } catch (_: Exception) {
                continue
            }
        }

        return false
    }

    @EventHandler
    fun onInteractBlock(event: CustomBlockInteractEvent) {
        if (event.hand() != InteractionHand.MAIN_HAND) return
        val adapterId = "ce:" + event.customBlock().id()

        stageManager.interact(event.player(),
            adapterId,
            event.location(),
            event,
            this)
    }

    @EventHandler
    fun onInteractFurniture(event: FurnitureInteractEvent) {
        if (isRemoving(event.furniture().baseEntity().uniqueId)) {
            event.isCancelled = true
            return
        }

        Bukkit.getScheduler().runTaskLater(core, Runnable {
            if (event.furniture().baseEntity() != null) {
                val adapterId = "ce:" + event.furniture().id()
                stageManager.interact(
                    event.player,
                    adapterId,
                    event.location(),
                    event,
                    this)
            }
        }, 2L)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: CustomBlockBreakEvent) {
        StageData.removeStageData(event.bukkitBlock())
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onFurnitureBreak(event: FurnitureBreakEvent) {
        val uuid = event.furniture().baseEntity().uniqueId

        if (isRemoving(uuid)) {
            event.isCancelled = true
            return
        }

        setRemoving(uuid)

        StageData.removeStageData(event.furniture().baseEntity().location)

        Bukkit.getScheduler().runTaskLater(core, Runnable {
            clearRemoving(uuid)
        }, 2L)
    }

    private fun placeBlock(adapterId: String, location: Location?) {
        val furnitureId = Key.of(adapterId.replace("ce:", ""))
        val furniture = CraftEngineFurniture.byId(furnitureId)
        val anchor = furniture?.getAnyAnchorType() ?: AnchorType.GROUND

        CraftEngineFurniture.place(location,
            furnitureId,
            anchor,
            false)
    }

    private fun breakBlock(location: Location?) {
        if (location != null) {
            CraftEngineBlocks.remove(location.block)
        }
    }

    private fun replaceFurniture(adapterId: String, entity: Entity) {
        //val customFurniture = CustomFurniture.byAlreadySpawned(entity)
        //customFurniture!!.replaceFurniture(adapterId.replace("ce:", ""))
    }

    private fun breakFurniture(entity: Entity, player: Player?) {
        CraftEngineFurniture.remove(entity)
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
        CraftEngineBlocks.place(
            loc,
            Key.of(itemAdapterData.id.removePrefix("ce:")),
            CompoundTag(),
            false
        )
        //placeBlock(itemAdapterData.id, loc)
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

        if (event is FurnitureInteractEvent) {
            event.isCancelled = true

            val entityEvent: Entity = event.furniture().baseEntity()

            if (!entityEvent.isValid || entityEvent.isDead) return

            // Cancel automatic drop (just in case)
            CraftEngineFurniture.remove(event.furniture().baseEntity())

            // Spawn of the new furniture
            val furnitureId = Key.of(itemAdapterData.id.removePrefix("ce:"))
            val furniture = CraftEngineFurniture.byId(furnitureId)
            val anchor = furniture?.getAnyAnchorType() ?: AnchorType.GROUND

            CraftEngineFurniture.place(loc,
                furnitureId,
                anchor,
                false)?.let { customFurniture ->

                val entity: Entity = customFurniture.baseEntity() ?: return
                entity.setRotation(entity.location.yaw, entity.location.pitch)
                if (entity is ItemFrame && entity is ItemFrame) {
                    entity.rotation = entity.rotation
                }
            }
        }else{
            val furnitureId = Key.of(itemAdapterData.id.removePrefix("ce:"))
            val furniture = CraftEngineFurniture.byId(furnitureId)
            val anchor = furniture?.getAnyAnchorType() ?: AnchorType.GROUND

            CraftEngineFurniture.place(loc,
                furnitureId,
                anchor,
                false)
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
            //breakBlock(loc)
            CraftEngineBlocks.remove(event.bukkitBlock())
        }
        if (event is FurnitureInteractEvent) {
            CraftEngineFurniture.remove(event.furniture().baseEntity())
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
            val block: Block = event.bukkitBlock()
            return Utils.calculateHashCode(
                block.location.hashCode(),
                block.hashCode(),
                block.type.hashCode(),
                block.blockData.hashCode(),
                block.state.hashCode()
            )
        }
        if (event is FurnitureInteractEvent) {
            val entity: Entity = event.furniture().baseEntity()
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
            return event.item()
        }
        return null
    }

    override fun getBlockFace(event: Event): BlockFace? {
        if (event is CustomBlockInteractEvent) {
            return event.clickedFace()
        }
        return null
    }

}