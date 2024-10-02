package dev.wuason.unearthMechanic.system.compatibilities

import dev.wuason.mechanics.compatibilities.adapter.Adapter
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.*
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.StageData
import dev.wuason.unearthMechanic.system.StageManager
import dev.wuason.unearthMechanic.utils.Utils
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

class MinecraftImpl(private val core: UnearthMechanic, private val stageManager: StageManager): ICompatibility {
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
        if (event.hasBlock() && event.hand == EquipmentSlot.HAND && event.action == Action.RIGHT_CLICK_BLOCK && event.useInteractedBlock() == Event.Result.ALLOW) {
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

    private fun handleBlockStage(
        player: Player,
        itemId: String,
        event: Event,
        loc: Location,
        toolUsed: ILiveTool,
        generic: IGeneric,
        stage: IStage
    ) {
        loc.block.type = Material.getMaterial(itemId.replace("mc:", "").uppercase(Locale.ENGLISH)) ?: return
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
        throw UnsupportedOperationException("Minecraft does not support furniture stages")
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
        if (generic is IBlock) {
            handleBlockStage(player, itemId, event, loc, toolUsed, generic, stage)
        }
        else if (generic is IFurniture) {
            handleFurnitureStage(player, itemId, event, loc, toolUsed, generic, stage)
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
        if (generic is IBlock) {
            loc.block.type = Material.AIR
        }
        else if (generic is IFurniture) {
            throw UnsupportedOperationException("Minecraft does not support furniture stages")
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
        if (generic is IBlock && event is PlayerInteractEvent) {
            val block: Block = event.clickedBlock!!
            return Utils.calculateHashCode(block.type.hashCode(), block.blockData.hashCode(), block.state.hashCode(), block.hashCode())
        }
        return -1
    }

    override fun getItemHand(event: Event): ItemStack? {
        if (event is PlayerInteractEvent) {
            return event.item
        }
        return null
    }


}