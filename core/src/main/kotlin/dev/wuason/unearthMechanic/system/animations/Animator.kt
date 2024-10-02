package dev.wuason.unearthMechanic.system.animations

import dev.wuason.mechanics.items.remover.ItemRemoverManager
import dev.wuason.unearthMechanic.UnearthMechanic
import dev.wuason.unearthMechanic.system.IAnimation
import dev.wuason.unearthMechanic.system.IAnimationData
import dev.wuason.unearthMechanic.system.IAnimator
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class Animator(
    private val core: UnearthMechanic
): IAnimator {

    companion object {
        val ANIM_NAMESPACED_KEY: NamespacedKey = NamespacedKey(UnearthMechanic.getInstance(), "anim")
    }

    private val animations: WeakHashMap<Player, IAnimationData> = WeakHashMap()

    init {
        ItemRemoverManager.addCheck { item ->
            if (!item.type.isAir && item.hasItemMeta()) return@addCheck item.itemMeta.persistentDataContainer.has(
                ANIM_NAMESPACED_KEY
            )
            return@addCheck false
        }
    }

    override fun isAnimating(player: Player): Boolean {
        return animations.containsKey(player) && animations[player]!!.isAnimating()
    }

    override fun getAnimation(player: Player): IAnimationData? {
        return animations[player]
    }

    override fun removeAnimation(player: Player) {
        animations.remove(player)
    }

    override fun playAnimation(animation: IAnimation) {

        if(isAnimating(animation.getPlayer())) return

        val player: Player = animation.getPlayer();

        val itemInMainHand: ItemStack = player.inventory.itemInMainHand
        val animationData: AnimationData = AnimationData(animation, this)
        animationData.setItemMainHand(itemInMainHand)
        animationData.setAnimating(true)
        animations[player] = animationData
        player.inventory.setItemInMainHand(animation.getAnimationItem())
        if (animation.getTicks() > 0) {
            var i: Long = 0
            val task: BukkitRunnable = object : BukkitRunnable() {
                override fun run() {
                    animationData.running(i++, this)
                }
            }
            animationData.setTask(task.runTaskTimer(core, 0, 1))
        }
    }

    override fun stopAnimation(player: Player) {
        if (!isAnimating(player)) return
        val animationData: IAnimationData = animations[player]!!
        animationData.setAnimating(false)
        animationData.getTask()?.cancel()
        animationData.returnItemOriginal()
        animations.remove(player)
    }

    override fun getAnimations(): WeakHashMap<Player, IAnimationData> {
        return animations
    }
}