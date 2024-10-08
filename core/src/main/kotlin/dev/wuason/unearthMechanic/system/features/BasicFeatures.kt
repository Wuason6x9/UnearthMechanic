package dev.wuason.unearthMechanic.system.features

import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class BasicFeatures: Feature() {

    override fun onProcess(
        tick: Long,
        p: Player,
        comp: ICompatibility,
        event: Event,
        loc: Location,
        liveTool: ILiveTool,
        iStage: IStage,
        iGeneric: IGeneric
    ) {
    }

    override fun onApply(
        p: Player,
        comp: ICompatibility,
        event: Event,
        loc: Location,
        liveTool: ILiveTool,
        iStage: IStage,
        iGeneric: IGeneric
    ) {
        if (iStage.getDrops().isNotEmpty()) iStage.dropItems(loc)
        if (iStage.getItems().isNotEmpty()) iStage.addItems(p)

        if (iStage.isRemoveItemMainHand()) liveTool.setItemMainHand(ItemStack(Material.AIR))

        if (iStage.getReduceItemHand() != 0) liveTool.getItemMainHand()?.let {
            if (!it.type.isAir) it.subtract(iStage.getReduceItemHand())
            UnearthMechanic.getInstance().getStageManager().getAnimator().getAnimation(p)?.let { anim ->
                anim.updateItemMainHandData()
            }
        }

        if (iStage.getSounds().isNotEmpty()) {
            iStage.getSounds().forEach { sound ->
                if (sound.delay > 0) {
                    Bukkit.getScheduler().runTaskLater(UnearthMechanic.getInstance(), Runnable {
                        loc.world.playSound(
                            Sound.sound(
                                Key.key(sound.soundId),
                                Sound.Source.BLOCK,
                                sound.volume,
                                sound.pitch
                            )
                        )
                    }, sound.delay)
                } else {
                    loc.world.playSound(Sound.sound(Key.key(sound.soundId), Sound.Source.BLOCK, sound.volume, sound.pitch))
                }
            }
        }
    }
}