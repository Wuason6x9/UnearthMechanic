package dev.wuason.unearthMechanic.config

import dev.wuason.mechanics.items.ItemBuilder
import dev.wuason.unearthMechanic.system.animations.AnimationManager
import org.bukkit.inventory.ItemStack

class Animation(
    private val ticks: Long,
    private val animationItem: String
): IAnimation {

    override fun getTicks(): Long {
        return ticks
    }

    override fun getAnimationItem(): ItemStack {
        return ItemBuilder(animationItem, 1).addPersistentData(AnimationManager.ANIM_NAMESPACED_KEY, animationItem).build()
    }

    override fun toString(): String {
        return "Animation(ticks=$ticks, animationItem=$animationItem)"
    }
}