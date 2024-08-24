package dev.wuason.unearthMechanic.system.compatibilities

import dev.wuason.mechanics.utils.VersionDetector
import dev.wuason.mechanics.utils.VersionDetector.ServerVersion
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.IBlock
import dev.wuason.unearthMechanic.config.IFurniture
import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.StageData
import dev.wuason.unearthMechanic.system.StageManager
import io.th0rgal.oraxen.api.OraxenBlocks
import io.th0rgal.oraxen.api.OraxenFurniture
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureBreakEvent
import io.th0rgal.oraxen.api.events.furniture.OraxenFurnitureInteractEvent
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockBreakEvent
import io.th0rgal.oraxen.api.events.noteblock.OraxenNoteBlockInteractEvent
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockBreakEvent
import io.th0rgal.oraxen.api.events.stringblock.OraxenStringBlockInteractEvent
import io.th0rgal.oraxen.utils.drops.Drop
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import kotlin.math.min

class OraxenImpl(private val core: UnearthMechanic, private val stageManager: StageManager): Compatibility {

    @EventHandler
    fun onInteractBlock(event: OraxenNoteBlockInteractEvent) {
        if (event.player != null && event.hand == EquipmentSlot.HAND) {
            stageManager.interact(
                event.player,
                "or:" + event.mechanic.itemID,
                event.block.location,
                event,
                this
            )
        }
    }

    @EventHandler
    fun onInteractBlock(event: OraxenStringBlockInteractEvent) {
        if (event.player != null && event.hand == EquipmentSlot.HAND) {
            stageManager.interact(
                event.player,
                "or:" + event.mechanic.itemID,
                event.block.location,
                event,
                this
            )
        }
    }

    @EventHandler
    fun onInteractFurniture(event: OraxenFurnitureInteractEvent) {
        if (event.player != null && event.hand == EquipmentSlot.HAND) {
            stageManager.interact(
                event.player,
                "or:" + event.mechanic.itemID,
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


    private fun placeBlock(adapterId: String, location: Location) {
        OraxenBlocks.place(adapterId.replace("or:", ""), location)
    }

    private fun breakBlock(location: Location, player: Player) {
        OraxenBlocks.remove(location, player)
    }

    private fun placeFurniture(
        adapterId: String,
        location: Location,
        blockFace: BlockFace,
        yaw: Float
    ) {
        OraxenFurniture.getFurnitureMechanic(adapterId.replace("or:", "")).place(location, yaw, blockFace)
    }

    private fun breakFurniture(entity: Entity, player: Player, id: String) {
        OraxenFurniture.remove(entity, player, Drop(mutableListOf(), false, false, id))
    }

    /*private fun getRotation(yaw: Double, restricted: Boolean): Rotation {
        var id = (((Location.normalizeYaw(yaw.toFloat()) + 180) * 8 / 360) + 0.5).toInt() % 8
        if (restricted && id % 2 != 0) id -= 1
        return Rotation.entries[id]
    }*/

    override fun loaded(): Boolean {
        return Bukkit.getPluginManager().getPlugin("Oraxen") != null
    }

    override fun enabled(): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled("Oraxen")
    }

    override fun name(): String {
        return "Oraxen"
    }

    override fun adapterId(): String {
        return "or"
    }

    override fun handleOthersFeatures(
        player: Player,
        event: Event,
        loc: Location,
        toolUsed: String,
        generic: IGeneric,
        stage: IStage
    ) {
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
        placeBlock(itemId, loc)
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
        if (event is OraxenFurnitureInteractEvent) {
            breakFurniture(event.baseEntity, player, event.mechanic.itemID)
            placeFurniture(itemId, loc, event.baseEntity.facing, event.baseEntity.location.yaw)
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
            if (event is OraxenFurnitureInteractEvent) {
                breakFurniture(event.baseEntity, player, event.mechanic.itemID)
            }
        }
    }

}