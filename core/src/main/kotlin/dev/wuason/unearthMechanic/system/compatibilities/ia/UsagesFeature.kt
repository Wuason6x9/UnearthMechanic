package dev.wuason.unearthMechanic.system.compatibilities.ia

import dev.lone.itemsadder.api.CustomStack
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import dev.wuason.unearthMechanic.system.features.Feature
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class UsagesFeature: Feature() {

    override fun onApply(
        p: Player,
        comp: ICompatibility,
        event: Event,
        loc: Location,
        liveTool: ILiveTool,
        iStage: IStage,
        iGeneric: IGeneric
    ) {
        val itemMainHand: ItemStack = liveTool.getItemMainHand()?: return
        if (p.gameMode == GameMode.CREATIVE) return
        if (iStage.getUsagesIaToRemove() > 0 && !itemMainHand.type.isAir) {
            CustomStack.byItemStack(itemMainHand)?.let { customStack ->
                customStack.reduceUsages(iStage.getUsagesIaToRemove())
                itemMainHand.itemMeta = customStack.itemStack.itemMeta
                UnearthMechanic.getInstance().getStageManager().getAnimator().getAnimation(p)?.let { anim ->
                    anim.updateItemMainHandData()
                }
            }
        }
    }

}