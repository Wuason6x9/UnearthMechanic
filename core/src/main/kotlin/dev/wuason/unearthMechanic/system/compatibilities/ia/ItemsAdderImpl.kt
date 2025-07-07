package dev.wuason.unearthMechanic.system.compatibilities.ia

import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomFurniture
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent
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
import net.momirealms.craftengine.core.util.Cancellable
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
import org.bukkit.scheduler.BukkitScheduler

import java.util.concurrent.ConcurrentHashMap

class ItemsAdderImpl(
    pluginName: String,
    private val core: UnearthMechanicPlugin,
    private val stageManager: StageManager,
    adapterComp: AdapterComp,
) : ICompatibility(
    pluginName,
    adapterComp
) {
    private enum class BlockState { NONE, PENDING_REPLACE, REMOVED }

    private val blockStates = ConcurrentHashMap<String, Pair<BlockState, Long>>()

    private fun generateKey(loc: Location): String =
        "${loc.blockX},${loc.blockY},${loc.blockZ},${loc.world?.name}"

    init {
        Bukkit.getScheduler().runTaskTimer(core, Runnable {
            processQueue()
        }, 20L, 20L)
    }

    private fun processQueue() {
        val now = Bukkit.getServer().currentTick.toLong()
        val toRemove = mutableListOf<String>()

        for ((key, value) in blockStates.entries) {
            val (state, tick) = value
            if (now - tick > 10L) {
                if (state == BlockState.PENDING_REPLACE) {
                    locationFromKey(key)?.let { loc ->
                        loc.world.getNearbyEntities(loc, 0.5, 0.5, 0.5).forEach { e ->
                            if (CustomFurniture.byAlreadySpawned(e) != null) {
                                CustomFurniture.remove(e, false)
                                Bukkit.getConsoleSender().sendMessage("[UnearthMechanic] Cleaned leftover at $key")
                            }
                        }
                    }
                }
                toRemove.add(key)
            }
        }

        toRemove.forEach { blockStates.remove(it) }
    }

    private fun locationFromKey(key: String): Location? {
        val parts = key.split(",")
        if (parts.size != 4) return null
        val x = parts[0].toIntOrNull() ?: return null
        val y = parts[1].toIntOrNull() ?: return null
        val z = parts[2].toIntOrNull() ?: return null
        val world = Bukkit.getWorld(parts[3]) ?: return null
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onFurnitureBreak(event: FurnitureBreakEvent) {
        StageData.removeStageData(event.bukkitEntity.location)
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
        val key = generateKey(loc)
        val block = loc.block
        val currentTick = Bukkit.getServer().currentTick.toLong()

        if (event is FurnitureInteractEvent) {
            //event.isCancelled = true
            val entity = event.bukkitEntity
            blockStates[key] = BlockState.PENDING_REPLACE to currentTick

            // Remove existing furniture
            event.furniture?.remove(false)
            player.sendMessage("[UnearthMechanic] [$key] Furniture removed")

            // Postpone spawn one tick to avoid conflicts with possible simultaneous breaks.
            Bukkit.getScheduler().runTaskLater(core, Runnable {
                if (blockStates[key]?.first == BlockState.PENDING_REPLACE) {
                    CustomFurniture.spawn(itemAdapterData.id, block)?.entity?.let { newEntity ->
                        newEntity.setRotation(entity.location.yaw, entity.location.pitch)
                        if (entity is ItemFrame && newEntity is ItemFrame) {
                            newEntity.rotation = entity.rotation
                        }
                    }
                }
                blockStates.remove(key)
                player.sendMessage("[UnearthMechanic] [$key] State cleared")
                player.sendMessage("------------------------------------------------")
            }, 1L)
        } else {
            player.sendMessage("[UnearthMechanic] [$key] No furniture event, direct spawn")
            // Direct spawn in case of no furniture event
            CustomFurniture.spawn(itemAdapterData.id, block)
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
        val key = generateKey(loc)
        //if (blockStates[key]?.first == BlockState.PENDING_REPLACE) return
        if (blockStates[key]?.first == BlockState.PENDING_REPLACE) {
            Bukkit.getConsoleSender().sendMessage("[UnearthMechanic] [$key] Remove skipped due to PENDING_REPLACE")
            return
        }

        if (event is CustomBlockInteractEvent) {
            breakBlock(loc)
        }
        if (event is FurnitureInteractEvent) {
            event.furniture?.remove(false)
            Bukkit.getConsoleSender().sendMessage("[UnearthMechanic] [$key] Furniture removed on break")
        }

        blockStates[key] = BlockState.REMOVED to Bukkit.getServer().currentTick.toLong()
        Bukkit.getConsoleSender().sendMessage("[UnearthMechanic] [$key] Set to REMOVED")
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