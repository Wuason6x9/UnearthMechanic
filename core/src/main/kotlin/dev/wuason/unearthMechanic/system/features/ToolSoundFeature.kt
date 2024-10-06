package dev.wuason.unearthMechanic.system.features

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.ILiveTool
import dev.wuason.unearthMechanic.system.compatibilities.ICompatibility
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event

class ToolSoundFeature: Feature() {

    override fun onPreApply(
        p: Player,
        comp: ICompatibility,
        event: Event,
        loc: Location,
        liveTool: ILiveTool,
        iStage: IStage,
        iGeneric: IGeneric
    ) {
        liveTool.getITool().getSound()?.let { sound ->
            loc.world.playSound(
                Sound.sound(
                    Key.key(sound.soundId),
                    Sound.Source.BLOCK,
                    sound.volume,
                    sound.pitch
                )
            )
        }
    }
}