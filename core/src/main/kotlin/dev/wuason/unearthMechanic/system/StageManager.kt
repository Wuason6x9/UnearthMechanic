package dev.wuason.unearthMechanic.system

import dev.wuason.mechanics.compatibilities.adapter.Adapter
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.*
import dev.wuason.unearthMechanic.events.ApplyStageEvent
import dev.wuason.unearthMechanic.system.compatibilities.Compatibility
import dev.wuason.unearthMechanic.system.compatibilities.ItemsAdderImpl
import dev.wuason.unearthMechanic.system.compatibilities.MinecraftImpl
import dev.wuason.unearthMechanic.system.compatibilities.OraxenImpl
import io.th0rgal.oraxen.api.OraxenItems
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.util.*

class StageManager(private val core: UnearthMechanic) : IStageManager {

    private val compatibilitiesLoaded: MutableList<Compatibility> = arrayListOf(MinecraftImpl(core, this))

    private val compatibilities: Array<Compatibility> = arrayOf(
        ItemsAdderImpl(core, this),
        OraxenImpl(core, this)
    )


    init {
        for (compatibility in compatibilities) {
            if (compatibility.loaded()) {
                compatibilitiesLoaded.add(compatibility)
            }
        }

        compatibilitiesLoaded.forEach { compatibility ->
            Bukkit.getPluginManager().registerEvents(compatibility, core)
        }
    }

    fun interact(player: Player, baseItemId: String, location: Location, event: Event, compatibility: Compatibility) {

        if (StageData.hasStageData(location)) {
            val stageData: StageData = StageData.fromLoc(location) ?: return
            val toolUsed: String = Adapter.getAdapterIdBasic(player.inventory.itemInMainHand)
            interactExist(player, baseItemId, location, event, compatibility, stageData, toolUsed)
            return
        }

        if (core.getConfigManager().validBaseItemId(baseItemId)) {
            val toolUsed: String = Adapter.getAdapterIdBasic(player.inventory.itemInMainHand)
            interactNotExist(player, baseItemId, location, event, compatibility, toolUsed)
            return
        }

    }

    private fun interactExist(player: Player, itemId: String, location: Location, event: Event, compatibility: Compatibility, storageData: StageData, toolUsed: String) {
        if (!storageData.getGeneric().getTools().contains(toolUsed)) return
        if (event is Cancellable) {
            event.isCancelled = true
        }
        applyStage(player, compatibility, event, location, toolUsed, storageData.getGeneric(), storageData.getStage())
    }

    private fun interactNotExist(player: Player, baseItemId: String, location: Location, event: Event, compatibility: Compatibility, toolUsed: String) {
        if (!core.getConfigManager().validTool(baseItemId, toolUsed)) return
        val generic: IGeneric = core.getConfigManager().getGeneric(baseItemId, toolUsed) ?: return
        if (event is Cancellable) {
            event.isCancelled = true
        }
        applyStage(player, compatibility, event, location, toolUsed, generic, 0)
    }

    private fun applyStage(player: Player, compatibility: Compatibility, event: Event, loc: Location, toolUsed: String, generic: IGeneric, stage: Int) {
        //event start
        val eventStage: ApplyStageEvent = ApplyStageEvent(player, compatibility, event, loc, toolUsed, generic, stage)
        Bukkit.getPluginManager().callEvent(eventStage)
        if (eventStage.isCancelled) return
        //event end
        if (generic.getStages().isEmpty() || generic.getStages().getOrNull(stage) == null) return
        val stage: IStage = generic.getStages()[stage]
        if (stage.getDrops().isNotEmpty()) dropItems(loc, stage)
        if (stage.isRemoveItemMainHand()) player.inventory.setItemInMainHand(ItemStack(Material.AIR))
        compatibility.handleOthersFeatures(player, event, loc, toolUsed, generic, stage)
        stage.getItemId()?.let {
            if (generic is IBlock) {

                if (isSimilarCompatibility(it, compatibility)) {
                    compatibility.handleBlockStage(player, it, event, loc, toolUsed, generic, stage)
                }
                else {
                    val c: Compatibility = getCompatibilityByAdapterId(it)?: throw NullPointerException("Compatibility not found for $it")
                    compatibility.handleRemove(player, event, loc, toolUsed, generic, stage)
                    c.handleBlockStage(player, it, event, loc, toolUsed, generic, stage)
                }

            }

            else if (generic is IFurniture) {

                if (isSimilarCompatibility(it, compatibility)) {
                    compatibility.handleFurnitureStage(player, it, event, loc, toolUsed, generic, stage)
                }
                else {
                    val c: Compatibility = getCompatibilityByAdapterId(it)?: throw NullPointerException("Compatibility not found for $it")
                    compatibility.handleRemove(player, event, loc, toolUsed, generic, stage)
                    c.handleFurnitureStage(player, it, event, loc, toolUsed, generic, stage)
                }
            }
        }


        if (stage.isRemove() || isLastStage(generic, stage)) {
            if (stage.isRemove()) compatibility.handleRemove(player, event, loc, toolUsed, generic, stage)
            StageData.removeStageData(loc)
            return
        }

        StageData.saveStageData(loc, StageData(loc, stage.getStage() + 1, generic))

    }

    override fun isLastStage(generic: IGeneric, stage: IStage): Boolean {
        return stage.getStage() == generic.getStages().size - 1
    }



    override fun dropItems(loc: Location, stage: IStage) {

        if (stage.isOnlyOneDrop() && stage.getDrops().isNotEmpty()) {
            val randomIndex: Int = Random().nextInt(stage.getDrops().size)
            val dropItem: ItemStack = stage.getDrops()[randomIndex].getItemStack()
            loc.world.dropItem(loc, dropItem)
            return
        }

        stage.getDrops().forEach { drop ->
            drop.getItemStackChance()?.let { loc.world.dropItem(loc, it) }
        }
    }

    override fun getCompatibilitiesLoaded(): MutableList<Compatibility> {
        return compatibilitiesLoaded
    }

    override fun getCompatibilities(): Array<Compatibility> {
        return compatibilities
    }

    override fun getCompatibilityByAdapterId(adapterId: String): Compatibility? {
        return compatibilitiesLoaded.find { adapterId.contains(it.adapterId(), true) }
    }

    override fun isSimilarCompatibility(adapterId: String, compatibility: Compatibility): Boolean {
        return adapterId.contains(compatibility.adapterId(), true)
    }




}