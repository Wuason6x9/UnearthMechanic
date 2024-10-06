package dev.wuason.unearthMechanic.system.animations

import dev.wuason.unearthMechanic.config.IAnimation
import org.bukkit.entity.Player
import java.util.*

interface IAnimationManager {
    fun isAnimating(player: Player): Boolean
    fun getAnimation(player: Player): IAnimationRunner?
    fun playAnimation(player: Player, animation: IAnimation)
    fun stopAnimation(player: Player)
    fun getAnimations(): WeakHashMap<Player, IAnimationRunner>
}