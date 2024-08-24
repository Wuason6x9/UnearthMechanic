package dev.wuason.unearthMechanic.system.compatibilities

import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomFurniture
import dev.lone.itemsadder.api.CustomStack
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent
import dev.lone.itemsadder.api.Events.FurnitureBreakEvent
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent
import dev.wuason.mechanics.utils.VersionDetector
import dev.wuason.mechanics.utils.VersionDetector.ServerVersion
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.IBlock
import dev.wuason.unearthMechanic.config.IFurniture
import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.StageData
import dev.wuason.unearthMechanic.system.StageManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import kotlin.math.min

class ItemsAdderImpl(private val core: UnearthMechanic, private val stageManager: StageManager): Compatibility {


    @EventHandler
    fun onInteractBlock(event: CustomBlockInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.player != null && event.hand == EquipmentSlot.HAND) {
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
        if (event.player != null && event.bukkitEntity != null) {
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

    override fun handleOthersFeatures(
        player: Player,
        event: Event,
        loc: Location,
        toolUsed: String,
        generic: IGeneric,
        stage: IStage
    ) {
        val itemMainHand: ItemStack = player.inventory.itemInMainHand
        if (stage.getUsagesIaToRemove() > 0 && !itemMainHand.type.isAir) {
            CustomStack.byItemStack(itemMainHand)?.let { customStack ->
                customStack.reduceUsages(stage.getUsagesIaToRemove())
                itemMainHand.itemMeta = customStack.itemStack.itemMeta
            }
        }
        if (stage.getDurabilityToRemove() > 0) {
            val itemMainHand: ItemStack = player.inventory.itemInMainHand
            if (!itemMainHand.type.isAir) {
                itemMainHand.editMeta { meta ->
                    if (meta is Damageable) {

                        if (VersionDetector.getServerVersion().isLessThan(ServerVersion.v1_20_5)) {
                            meta.damage += stage.getDurabilityToRemove()
                            if (meta.damage >= itemMainHand.type.maxDurability) {
                                player.inventory.setItemInMainHand(ItemStack(Material.AIR))
                            }
                        }
                        else {
                            if (meta.hasMaxDamage()) {

                                meta.damage += min(stage.getDurabilityToRemove(), meta.maxDamage - meta.damage)

                                if (meta.damage >= meta.maxDamage) {
                                    player.inventory.setItemInMainHand(ItemStack(Material.AIR))
                                }
                            }
                            else {

                                meta.damage += stage.getDurabilityToRemove()

                                if (meta.damage >= itemMainHand.type.maxDurability) {

                                    player.inventory.setItemInMainHand(ItemStack(Material.AIR))

                                }
                            }
                        }
                    }
                }
            }

        }


    }

    override fun handleBlockStage(
        player: Player,
        itemId: String,
        event: Event,
        loc: Location,
        toolUsed: String,
        generic: IGeneric,
        stage: IStage
    ) {
        placeBlock(itemId.replace("ia:", ""), loc)
    }

    override fun handleFurnitureStage(
        player: Player,
        itemId: String,
        event: Event,
        loc: Location,
        toolUsed: String,
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
        toolUsed: String,
        generic: IGeneric,
        stage: IStage
    ) {
        if (generic is IBlock) {
            breakBlock(loc, player)
        }
        else if (generic is IFurniture) {
            if (event is FurnitureInteractEvent) {
                event.furniture?.remove(false)
            }
        }
    }

}