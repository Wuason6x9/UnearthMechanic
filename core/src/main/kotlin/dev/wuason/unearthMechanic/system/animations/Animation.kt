package dev.wuason.unearthMechanic.system.animations

import dev.wuason.mechanics.items.ItemBuilder
import dev.wuason.unearthMechanic.system.IAnimation
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Animation(
    private val player: Player,
    private val location: Location,
    animationItem: ItemStack,
    private val ticks: Long
): IAnimation {

    private val animationItem: ItemStack = ItemBuilder.copyOf(animationItem).addPersistentData(Animator.ANIM_NAMESPACED_KEY, player.uniqueId.toString()).setAmount(1).build()

    override fun getPlayer(): Player {
        return player
    }

    override fun getLocation(): Location {
        return location
    }

    override fun getAnimationItem(): ItemStack {
        return animationItem
    }

    override fun getTicks(): Long {
        return ticks
    }
}