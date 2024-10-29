package dev.wuason.unearthMechanic.system.compatibilities.ia

import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomFurniture
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.*
import dev.wuason.unearthMechanic.system.ILiveTool
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

class ItemsAdderImpl(private val core: UnearthMechanic, private val stageManager: StageManager): ICompatibility {


    override fun onLoad() {
        Features.registerFeature(UsagesFeature())
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
            stageManager.interact(
                event.player,
                "ia:" + event.namespacedID,
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

    private fun breakBlock(location: Location?, player: Player?) {
        CustomBlock.remove(location)
    }

    private fun replaceFurniture(adapterId: String, entity: Entity?) {
        val customFurniture = CustomFurniture.byAlreadySpawned(entity)
        customFurniture!!.replaceFurniture(adapterId.replace("ia:", ""))
    }

    private fun breakFurniture(entity: Entity?, player: Player?) {
        CustomFurniture.remove(entity, false)
    }

    override fun loaded(): Boolean {
        return Bukkit.getPluginManager().getPlugin("ItemsAdder") != null
    }

    override fun enabled(): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")
    }

    override fun name(): String {
        return "ItemsAdder"
    }

    override fun adapterId(): String {
        return "ia"
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
        if (stage is IBlockStage) {

        }
        if (generic is IBlock) {
            handleBlockStage(player, itemId, event, loc, toolUsed, generic, stage)
        }
        else if (generic is IFurniture) {
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
        placeBlock(itemId.replace("ia:", ""), loc)
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
        if (event is FurnitureInteractEvent) {
            event.furniture?.remove(false)
            CustomFurniture.spawn(itemId.replace("ia:", ""), loc.block)?.let { customFurniture ->
                val entity: Entity = customFurniture.entity?: return
                val entityEvent: Entity = event.bukkitEntity
                entity.setRotation(entityEvent.location.yaw, entityEvent.location.pitch)

                if (entityEvent is ItemFrame && entity is ItemFrame) {
                    entity.rotation = entityEvent.rotation
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
        if (stage is IBlockStage) {
            loc.block.type = org.bukkit.Material.AIR
        }
        else if (stage is IFurnitureStage) {
            if (event is FurnitureInteractEvent) {
                event.furniture?.remove(false)
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
            if (event is CustomBlockInteractEvent) {
                val block: Block = event.blockClicked
                return Utils.calculateHashCode(block.location.hashCode(), block.hashCode(), block.type.hashCode(), block.blockData.hashCode(), block.state.hashCode())
            }
        }
        else if (generic is IFurniture) {
            if (event is FurnitureInteractEvent) {
                val entity: Entity = event.bukkitEntity
                return Utils.calculateHashCode(entity.location.hashCode(), entity.hashCode(), entity.type.hashCode(), entity.uniqueId.hashCode(), entity.isDead.hashCode(), entity.facing.hashCode())
            }
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