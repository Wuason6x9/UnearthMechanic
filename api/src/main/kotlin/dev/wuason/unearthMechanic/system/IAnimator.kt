package dev.wuason.unearthMechanic.system

import org.bukkit.entity.Player
import java.util.*

interface IAnimator {
    fun isAnimating(player: Player): Boolean
    fun getAnimation(player: Player): IAnimationData?
    fun removeAnimation(player: Player)
    fun playAnimation(animation: IAnimation)
    fun stopAnimation(player: Player)
    fun getAnimations(): WeakHashMap<Player, IAnimationData>
}