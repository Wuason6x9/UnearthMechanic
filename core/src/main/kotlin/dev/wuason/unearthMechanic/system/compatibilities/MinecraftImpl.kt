package dev.wuason.unearthMechanic.system.compatibilities

import dev.wuason.mechanics.compatibilities.adapter.Adapter
import dev.wuason.mechanics.utils.VersionDetector
import dev.wuason.mechanics.utils.VersionDetector.ServerVersion
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.*
import dev.wuason.unearthMechanic.system.StageData
import dev.wuason.unearthMechanic.system.StageManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.util.*
import kotlin.math.min

class MinecraftImpl(private val core: UnearthMechanic, private val stageManager: StageManager): Compatibility {
    override fun loaded(): Boolean {
        return true
    }

    override fun enabled(): Boolean {
        return true
    }

    override fun name(): String {
        return "Minecraft"
    }

    override fun adapterId(): String {
        return "mc"
    }

    @EventHandler
    fun onInteractBlock(event: PlayerInteractEvent) {
        if (event.hasBlock() && event.hand == EquipmentSlot.HAND && event.action.isRightClick) {
            val block: Block = event.clickedBlock?: return
            val adapterId = Adapter.getAdapterIdBasic(block)?: throw NullPointerException("Adapter ID not found")
            if (adapterId.contains("mc:")) stageManager.interact(event.player, adapterId, block.location, event, this)
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val stageData: StageData = StageData.fromBlock(event.block) ?: return
        val stageDataBack: StageData = stageData.getBackStageData() ?: return
        if(stageDataBack.getActualItemId().startsWith("mc:", true)) {
            StageData.removeStageData(event.block)
        }
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
        loc.block.type = Material.getMaterial(itemId.replace("mc:", "").uppercase(Locale.ENGLISH)) ?: return
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
        throw UnsupportedOperationException("Minecraft does not support furniture stages")
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
            loc.block.type = Material.AIR
        }
        else if (generic is IFurniture) {
            throw UnsupportedOperationException("Minecraft does not support furniture stages")
        }
    }


}