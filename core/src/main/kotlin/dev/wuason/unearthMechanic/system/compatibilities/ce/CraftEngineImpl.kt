package dev.wuason.unearthMechanic.system.compatibilities.ce

import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomFurniture
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

class CraftEngineImpl(
    pluginName: String,
    private val core: UnearthMechanicPlugin,
    private val stageManager: StageManager,
    adapterComp: AdapterComp,

    private val locks: MutableMap<String, Any> = ConcurrentHashMap()
) : ICompatibility(
    pluginName,
    adapterComp
) {


    @EventHandler
    fun onInteractBlock(event: CustomBlockInteractEvent) {
        if (event.hand() != InteractionHand.MAIN_HAND) return
        val id = event.customBlock().id().toString()

        stageManager.interact(event.player(), "ce:$id", event.location(), event, this)

    }

    @EventHandler
    fun onInteractFurniture(event: FurnitureInteractEvent) {
        if (event.hand() != InteractionHand.MAIN_HAND) return
        val id = event.furniture().id().toString()

        stageManager.interact(event.player(), "ce:$id", event.location(), event, this)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBlockBreak(event: CustomBlockBreakEvent) {
        StageData.removeStageData(event.bukkitBlock())
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onFurnitureBreak(event: FurnitureBreakEvent) {
        StageData.removeStageData(event.location())
    }

    private fun placeBlock(adapterId: String, location: Location?) {
        CustomBlock.place(adapterId.replace("ce:", ""), location)
    }

    private fun breakBlock(location: Location?) {
        CustomBlock.remove(location)
    }

    private fun replaceFurniture(adapterId: String, entity: Entity?) {
        val customFurniture = CustomFurniture.byAlreadySpawned(entity)
        customFurniture!!.replaceFurniture(adapterId.replace("ce:", ""))
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
        if (stage is IBlockStage) {
            handleBlockStage(player, itemAdapterData, event, loc, toolUsed, generic, stage)
        } else if (stage is IFurnitureStage) {
            val key = "${loc.blockX},${loc.blockY},${loc.blockZ},${loc.world?.name}"
            val mutex = locks.computeIfAbsent(key) { _: String -> Any() }

            synchronized(mutex) {
                handleFurnitureStage(player, itemAdapterData, event, loc, toolUsed, generic, stage)
            }
            locks.remove(key)
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
            true
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
        val mutex = locks.computeIfAbsent(key) { _: String -> Any() }

        synchronized(mutex) {
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
                    true)?.let { customFurniture ->

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
                    true)
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
        if (event is CustomBlockInteractEvent) {
            //breakBlock(loc)
            CraftEngineBlocks.remove(event.bukkitBlock())
        }
        if (event is FurnitureInteractEvent) {
            CraftEngineFurniture.remove(event.furniture().baseEntity())
        }
        if (event is FurnitureBreakEvent) {
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