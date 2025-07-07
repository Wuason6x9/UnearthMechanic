package dev.wuason.unearthMechanic.system

import dev.wuason.libs.adapter.Adapter
import dev.wuason.libs.adapter.AdapterData
import dev.wuason.libs.protectionlib.ProtectionLib
import dev.wuason.mechanics.utils.AdventureUtils
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.compatibilities.WorldGuardComp
import dev.wuason.unearthMechanic.compatibilities.WorldGuardPlugin
import dev.wuason.unearthMechanic.config.*
import dev.wuason.unearthMechanic.events.ApplyStageEvent
import dev.wuason.unearthMechanic.events.PreApplyStageEvent
import dev.wuason.unearthMechanic.system.animations.AnimationManager
import dev.wuason.unearthMechanic.system.animations.IAnimationManager
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import dev.wuason.unearthMechanic.system.compatibilities.MinecraftImpl
import dev.wuason.unearthMechanic.system.compatibilities.ce.CraftEngineImpl
import dev.wuason.unearthMechanic.system.compatibilities.ia.ItemsAdderImpl
import dev.wuason.unearthMechanic.system.compatibilities.nexo.NexoImpl
import dev.wuason.unearthMechanic.system.compatibilities.or.OraxenImpl
import dev.wuason.unearthMechanic.system.features.BasicFeatures
import dev.wuason.unearthMechanic.system.features.DurabilityFeature
import dev.wuason.unearthMechanic.system.features.Features
import dev.wuason.unearthMechanic.system.features.ToolSoundFeature
import dev.wuason.unearthMechanic.utils.Utils
import dev.wuason.unearthMechanic.utils.Utils.Companion.toAdapter
import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitTask
import java.util.*


class StageManager(private val core: UnearthMechanic) : IStageManager {

    companion object {
        init {
            Features.registerFeature(BasicFeatures())
            Features.registerFeature(DurabilityFeature())
            Features.registerFeature(ToolSoundFeature())
        }
    }

    private val compatibilitiesLoaded: MutableList<ICompatibility> = ArrayList()

    private val delays: HashMap<Location, BukkitTask> = HashMap()

    private val animator: AnimationManager = AnimationManager(core)

    init {

        compatibilitiesLoaded.add(MinecraftImpl("Vanilla", core, this, Adapter.getAdapterByName("Vanilla")))

        compCreator("Oraxen") { pluginName ->
            OraxenImpl(pluginName, core, this, Adapter.getAdapterByName(pluginName))
        } ?.let { compatibilitiesLoaded.add(it) }

        compCreator("ItemsAdder") { pluginName ->
            ItemsAdderImpl(pluginName, core, this, Adapter.getAdapterByName(pluginName))
        } ?.let { compatibilitiesLoaded.add(it) }

        compCreator("Nexo") { pluginName ->
            NexoImpl(pluginName, core, this, Adapter.getAdapterByName(pluginName))
        } ?.let { compatibilitiesLoaded.add(it) }

        compCreator("CraftEngine") { pluginName ->
            CraftEngineImpl(pluginName, core, this, Adapter.getAdapterByName(pluginName))
        } ?.let { compatibilitiesLoaded.add(it) }

        compatibilitiesLoaded.forEach { compatibility ->
            Bukkit.getPluginManager().registerEvents(compatibility, core)
        }

    }

    fun interact(player: Player, baseItemId: String, location: Location, event: Event, compatibility: ICompatibility) {
        if (player.isSneaking) return

        //player.sendMessage("el permiso es "+player.hasPermission("unearthMechanic.bypass"))
        //player.sendMessage("ProtectionLib es "+ProtectionLib.canInteract(player, location))
        //player.sendMessage("worldguard es "+WorldGuardPlugin.isWorldGuardEnabled())

        if (StageData.hasStageData(location)) {
            val stageData: StageData = StageData.fromLoc(location) ?: return
            //Bukkit.getConsoleSender().sendMessage("el stagedata es "+stageData.getGeneric().isNotProtect())
            val toolUsed: String = Adapter.getAdapterId(
                animator.getAnimation(player)?.getItemMainHand() ?: player.inventory.itemInMainHand
            )
            interactExist(player, baseItemId, location, event, compatibility, stageData, toolUsed.toAdapter()!!)
            return
        }

        if (core.getConfigManager().validBaseItemId(baseItemId.toAdapter()!!)) {
            val toolUsed: String = Adapter.getAdapterId(
                animator.getAnimation(player)?.getItemMainHand() ?: player.inventory.itemInMainHand
            )
            interactNotExist(player, baseItemId.toAdapter()!!, location, event, compatibility, toolUsed.toAdapter()!!)
            return
        }

    }

    private fun interactExist(
        player: Player,
        itemId: String,
        location: Location,
        event: Event,
        compatibility: ICompatibility,
        stageData: StageData,
        toolUsed: AdapterData
    ) {
        if (!stageData.getGeneric().existsTool(toolUsed)) return

        if (stageData.getActualAdapterData().adapter != compatibility.adapterComp()) return

        if (canInteractExist(player, location, stageData, core)) {

            val iTool: ITool = stageData.getGeneric().getTool(toolUsed) ?: throw NullPointerException(
                "Tool not found for $toolUsed in ${
                    stageData.getGeneric().getId()
                } mabye is duplicated config"
            )

            val liveTool: LiveTool = LiveTool(if (animator.isAnimating(player)) animator.getAnimation(player)!!.getItemMainHand() else player.inventory.itemInMainHand, iTool, player, this)

            if (stageData.getGeneric().getStages().size <= stageData.getStage()) {
                StageData.removeStageData(location)
                interact(player, itemId, location, event, compatibility)
                return
            }

            stageData.getGeneric().getStages()[stageData.getStage()]?.let {
                val stage: Stage = it as Stage
                onPreApplyStage(player, compatibility, event, location, liveTool, stageData.getGeneric(), stage)
            }
        }
    }

    fun canInteractExist(
        player: Player,
        location: Location,
        stageData: StageData,
        core: UnearthMechanic
    ): Boolean {
        val generic = stageData.getGeneric()

        return player.isOp
                || player.hasPermission("unearthMechanic.bypass")
                || generic.isNotProtect()
                || (
                !WorldGuardPlugin.isWorldGuardEnabled() && ProtectionLib.canInteract(player, location)
                )
                || (
                WorldGuardPlugin.isWorldGuardEnabled()
                        && ProtectionLib.canInteract(player, location)
                        && core.getWorldGuardComp().canInteractCustom(player, location)
                )
    }

    private fun interactNotExist(
        player: Player,
        baseAdapterData: AdapterData,
        location: Location,
        event: Event,
        compatibility: ICompatibility,
        toolUsed: AdapterData
    ) {
        if (!core.getConfigManager().validTool(baseAdapterData, toolUsed)) return
        val generic: IGeneric = core.getConfigManager().getGeneric(baseAdapterData, toolUsed) ?: return

        //Bukkit.getConsoleSender().sendMessage("No existe StageData y es "+ generic.isNotProtect())

        if (canInteractNotExist(player, location, generic, core)) {

            val iTool: ITool = generic.getTool(toolUsed)
                ?: throw NullPointerException("Tool not found for $toolUsed in ${generic.getId()} mabye is duplicated config")

            val liveTool: LiveTool = LiveTool(if (animator.isAnimating(player)) animator.getAnimation(player)!!.getItemMainHand() else player.inventory.itemInMainHand, iTool, player, this)

            generic.getStages()[0]?.let {
                val stage: Stage = it as Stage
                onPreApplyStage(player, compatibility, event, location, liveTool, generic, stage)
            }
        }
    }

    fun canInteractNotExist(
        player: Player,
        location: Location,
        generic: IGeneric,
        core: UnearthMechanic
    ): Boolean {
        return player.isOp
                || player.hasPermission("unearthMechanic.bypass")
                || generic.isNotProtect()
                || (
                !WorldGuardPlugin.isWorldGuardEnabled() && ProtectionLib.canInteract(player, location)
                )
                || (
                WorldGuardPlugin.isWorldGuardEnabled()
                        && ProtectionLib.canInteract(player, location)
                        && core.getWorldGuardComp().canInteractCustom(player, location)
                )
    }

    private fun onPreApplyStage(
        player: Player,
        compatibility: ICompatibility,
        event: Event,
        loc: Location,
        toolUsed: LiveTool,
        generic: IGeneric,
        stage: Stage
    ) {
        //send event
        val eventStage: PreApplyStageEvent =
            PreApplyStageEvent(player, compatibility, event, loc, toolUsed, generic, stage)
        Bukkit.getPluginManager().callEvent(eventStage)
        if (eventStage.isCancelled) return

        //try multiple interact
        multipleInteract(compatibility, event, player, loc, toolUsed)

        if (!animator.isAnimating(player) && toolUsed.getITool().getAnimation() != null && toolUsed.getITool().getAnimation()!!.getTicks() > 0) {
            animator.playAnimation(player, toolUsed.getITool().getAnimation()!!)
        }


        Features.getFeatures().forEach { feature ->
            try {
                feature.onPreApply(player, compatibility, event, loc, toolUsed, stage, generic)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (stage.getMaxCorrectDelay(toolUsed) > 0) {
            if (loc in delays) return
            if (event is Cancellable) {
                event.isCancelled = true
            }
            val validation: Validation = Validation(player, compatibility, event, loc, toolUsed, generic, stage)
            validation.start()
            val delayTask: DelayTask =
                DelayTask(this, player, compatibility, event, loc, toolUsed, generic, stage, validation)
            delayTask.start()
        } else {
            onApplyStage(player, compatibility, event, loc, toolUsed, generic, stage)
        }

    }

    fun onProcessStage(
        tick: Long,
        player: Player,
        compatibility: ICompatibility,
        event: Event,
        loc: Location,
        toolUsed: LiveTool,
        generic: IGeneric,
        stage: Stage,
        validation: Validation
    ) {
        Features.getFeatures().forEach { feature ->
            try {
                feature.onProcess(tick, player, compatibility, event, loc, toolUsed, stage, generic)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (tick >= (stage.getMaxCorrectDelay(toolUsed) - 1)) {
            onApplyStage(player, compatibility, event, loc, toolUsed, generic, stage, validation)
            return
        }
    }


    private fun onApplyStage(
        player: Player,
        compatibility: ICompatibility,
        event: Event,
        loc: Location,
        toolUsed: LiveTool,
        generic: IGeneric,
        stage: Stage,
        validation: Validation? = null
    ) {
        if ((validation != null && !validation.validate()) || !toolUsed.isOriginalItem() || !toolUsed.isValid() || !StageData.compare(StageData(loc, stage.getStage(), generic), loc)) return
        val applyStageEvent: ApplyStageEvent = ApplyStageEvent(player, compatibility, event, loc, toolUsed, generic, stage)
        Bukkit.getPluginManager().callEvent(applyStageEvent)
        if (applyStageEvent.isCancelled) return

        if (validation == null) {
            if (event is Cancellable) {
                event.isCancelled = true
            }
        }

        Features.getFeatures().forEach { feature ->
            try {
                feature.onApply(player, compatibility, event, loc, toolUsed, stage, generic)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stage.getAdapterData()?.let {
            if (isSimilarCompatibility(it, compatibility)) {

                if (!generic.getBackStage(stage).javaClass.isInstance(stage)) {
                    compatibility.handleRemove(player, event, loc, toolUsed, generic, stage)
                }
                compatibility.handleStage(player, it, event, loc, toolUsed, generic, stage)

            } else {
                val c: ICompatibility =
                    getCompatibilityByAdapterId(it) ?: throw NullPointerException("Compatibility not found for $it")
                compatibility.handleRemove(player, event, loc, toolUsed, generic, stage)
                c.handleStage(player, it, event, loc, toolUsed, generic, stage)
            }
        }


        if (stage.isRemove() || generic.isLastStage(stage)) {
            if (stage.isRemove()) compatibility.handleRemove(player, event, loc, toolUsed, generic, stage)
            StageData.removeStageData(loc)
            return
        }

        StageData.saveStageData(loc, StageData(loc, stage.getStage() + 1, generic))
    }

    override fun getCompatibilitiesLoaded(): MutableList<ICompatibility> {
        return compatibilitiesLoaded
    }

    override fun getCompatibilityByAdapterId(adapterData: AdapterData): ICompatibility? {
        return compatibilitiesLoaded.firstOrNull { compatibility -> compatibility.adapterComp() == adapterData.adapter }
    }

    override fun isSimilarCompatibility(adapterData: AdapterData, compatibility: ICompatibility): Boolean {
        return compatibility.adapterComp() == adapterData.adapter
    }

    override fun getAnimator(): IAnimationManager {
        return animator
    }

    fun getDelays(): HashMap<Location, BukkitTask> {
        return delays
    }


    private fun multipleInteract(comp: ICompatibility, event: Event, player: Player, location: Location, toolUsed: LiveTool) {
        if (toolUsed.getITool().isMultiple() && !StageData.hasMultiple(location)) {
            val blockFace: BlockFace = comp.getBlockFace(event) ?: player.getTargetBlockFace(999999999, FluidCollisionMode.NEVER) ?: return
            Utils.blockAround(
                location.block,
                toolUsed.getITool().getSize(),
                toolUsed.getITool().getDeep(),
                toolUsed.getITool().getDepth(),
                player,
                blockFace
            ).forEach { block ->
                StageData.applyMultiple(block)
                val playerInteractEvent: Event = PlayerInteractEvent(
                    player,
                    Action.RIGHT_CLICK_BLOCK,
                    toolUsed.getItemMainHand(),
                    block,
                    blockFace,
                    EquipmentSlot.HAND
                )
                Bukkit.getPluginManager().callEvent(playerInteractEvent)
                StageData.removeMultiple(block)
            }
        }
    }

    private fun compCreator(pluginName: String, compatibilityMaker: (pluginName: String) -> ICompatibility ) : ICompatibility? {
        if (Bukkit.getPluginManager().getPlugin(pluginName) == null) return null
        return compatibilityMaker.invoke(pluginName)
    }
}