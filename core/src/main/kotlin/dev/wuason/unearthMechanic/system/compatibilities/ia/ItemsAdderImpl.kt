package dev.wuason.unearthMechanic.system.compatibilities.ia

import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomFurniture
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent
import dev.wuason.libs.adapter.AdapterComp
import dev.wuason.libs.adapter.AdapterData
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
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.Collections
import java.util.UUID
import kotlin.collections.set

class ItemsAdderImpl(
    pluginName: String,
    private val core: UnearthMechanicPlugin,
    private val stageManager: StageManager,
    adapterComp: AdapterComp
) : ICompatibility(
    pluginName,
    adapterComp
) {

    private val removedLocations = Collections.synchronizedSet(mutableSetOf<Location>())

    override fun isRemoving(location: Location): Boolean {
        return removedLocations.contains(location)
    }

    override fun setRemoving(location: Location) {
        removedLocations.add(location)
    }

    override fun clearRemoving(location: Location) {
        removedLocations.remove(location)
    }

    companion object {
        private val rotationMap = mutableMapOf<Location, Pair<Float, Float>>()
        val itemFrameRotationMap = mutableMapOf<Location, org.bukkit.Rotation>()
    }

    override fun getFurnitureUUID(location: Location): UUID? {
        val world = location.world ?: return null

        val entities = world.getNearbyEntities(location, 1.0, 1.0, 1.0)

        for (entity in entities) {
            try {
                val furniture = CustomFurniture.byAlreadySpawned(entity)
                if (furniture != null) {
                    return entity.uniqueId
                }
            } catch (e: Exception) {
                continue
            }
        }

        return null
    }

    override fun isValid(loc: Location, expectedAdapterId: String?): Boolean {
        val world = loc.world ?: return false

        // Check for furniture
        val nearby = world.getNearbyEntities(loc, 0.5, 1.0, 0.5)
        for (entity in nearby) {
            try {
                val furniture = CustomFurniture.byAlreadySpawned(entity)
                if (furniture != null && entity.isValid && !entity.isDead) {
                    //Bukkit.getConsoleSender().sendMessage("[UM][ItemsAdderImpl] isValid: furniture válido encontrado en $location (${furniture.namespace}:${furniture.id})")
                    return true
                }
            } catch (_: Exception) {
                continue
            }
        }
        if (loc.block.type != org.bukkit.Material.AIR) return true

        return false
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
        /*event.bukkitEntity?.let { entity ->
            rotationMap[entity.location] = Pair(entity.location.yaw, entity.location.pitch)
        }*/

        val uuid = event.bukkitEntity.uniqueId
        //val currentTick = Bukkit.getCurrentTick().toLong()
        //Bukkit.getConsoleSender().sendMessage("[UM] onInteractFurniture aplicado para $uuid en $currentTick")

        if (event.bukkitEntity != null && event.bukkitEntity.uniqueId == uuid) {
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
        if (stageManager.activeSequences.contains(event.bukkitEntity.location.block.location)) {
            //Bukkit.getConsoleSender().sendMessage("[UM] onFurnitureBreak Cancelado por SECUENCIA en ${event.bukkitEntity.location}")
            stageManager.cancelSequence(this, event.bukkitEntity.location.block.location)
            event.isCancelled = true
            return
        }

        val loc = event.bukkitEntity.location.block.location

        StageData.removeStageData(event.bukkitEntity.location)
        setRemoving(loc)
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
        //Bukkit.getConsoleSender().sendMessage("[UM][ItemsAdderImpl] handleStage ejecutado con adapterData: ${stage.getAdapterData()?.adapter?.type}:${stage.getAdapterData()?.id}")
        //Bukkit.getConsoleSender().sendMessage("[UM] handleStage en $loc - TICK: ${Bukkit.getCurrentTick()}")
        if (stage is IBlockStage) {
            handleBlockStage(player, itemAdapterData, event, loc, toolUsed, generic, stage)
        } else if (stage is IFurnitureStage) {
            Bukkit.getScheduler().runTaskLater(core, Runnable {
                handleFurnitureStage(player, itemAdapterData, event, loc, toolUsed, generic, stage)
            }, 2L)
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
        if(isRemoving(loc.block.location)){
            if(!stageManager.activeSequences.contains(loc.block.location)){
                clearRemoving(loc.block.location) }

            //Bukkit.getConsoleSender().sendMessage("[UM] Bloqueada recolocación definitiva en ${loc.block.location}")
            return
        }

        if (event is FurnitureInteractEvent) {
            val uuid = event.bukkitEntity.uniqueId

            event.isCancelled = true

            val entityEvent: Entity = event.bukkitEntity
            if(!entityEvent.isValid) {
                //Bukkit.getConsoleSender().sendMessage(" NO ES VALIDO EL FURNITURE" +loc)
                return
            }
            event.furniture?.remove(false)

            //if(isRemoving(loc.block.location)) return
            if(isRemoving(loc.block.location)){
                if(!stageManager.activeSequences.contains(event.bukkitEntity.location.block.location)){
                    clearRemoving(event.bukkitEntity.location.block.location)
                }

                //Bukkit.getConsoleSender().sendMessage("Spawn cancelado en $loc - adapter ${itemAdapterData.id}")
                return
            }
            CustomFurniture.spawn(itemAdapterData.id, loc.block)?.let { customFurniture ->
                //Bukkit.getConsoleSender().sendMessage("[IA] spawn furniture at $loc - adapter ${itemAdapterData.id}")
                val entity: Entity = customFurniture.entity ?: return

                entity.setRotation(entityEvent.location.yaw, entityEvent.location.pitch)

                CustomFurniture.byAlreadySpawned(loc.block)?.entity?.let { entity ->
                    rotationMap[loc] = Pair(entityEvent.location.yaw, entityEvent.location.pitch)
                }

                if (entityEvent is ItemFrame && entity is ItemFrame) {
                    entity.rotation = entityEvent.rotation
                    itemFrameRotationMap[loc] = entityEvent.rotation
                }

            }
            Bukkit.getScheduler().runTaskLater(core, Runnable {
                if(!stageManager.activeSequences.contains(event.bukkitEntity.location.block.location)){
                    //Bukkit.getConsoleSender().sendMessage("clearRemoving "+event.bukkitEntity.location.block.location)
                    clearRemoving(event.bukkitEntity.location.block.location)
                }
            }, 3L)
        } else {
            // Sequence System
            val rotation = rotationMap.remove(loc)
            val cachedFrameRotation = itemFrameRotationMap[loc]
            CustomFurniture.spawn(itemAdapterData.id, loc.block)?.let { customFurniture ->
                val entity: Entity = customFurniture.entity ?: return

                if (rotation != null) entity.setRotation(rotation.first, rotation.second)
                if (cachedFrameRotation != null && entity is ItemFrame) {
                    entity.rotation = cachedFrameRotation
                }
            }
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
        /*CustomFurniture.byAlreadySpawned(loc.block)?.entity?.let { entity ->
            rotationMap[loc] = Pair(entity.location.yaw, entity.location.pitch)
        }*/
        if (event is CustomBlockInteractEvent) {
            breakBlock(loc)
            return
        }
        if (event is FurnitureInteractEvent) {
            event.bukkitEntity?.let { entity ->
                rotationMap[entity.location] = Pair(entity.location.yaw, entity.location.pitch)
            }
            setRemoving(event.bukkitEntity.location.block.location)

            //Bukkit.getConsoleSender().sendMessage("[IA] Furniture removido en $loc")
            val uuid = event.bukkitEntity.uniqueId
            event.furniture?.remove(false)
            return
        }

        // Sequence System
        val furniture = CustomFurniture.byAlreadySpawned(loc.block)
        furniture?.remove(false)
        if (loc.block.type != org.bukkit.Material.AIR) {
            breakBlock(loc)
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