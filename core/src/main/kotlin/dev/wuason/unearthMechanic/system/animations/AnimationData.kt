package dev.wuason.unearthMechanic.system.animations

import dev.wuason.mechanics.items.ItemBuilder
import dev.wuason.mechanics.utils.StorageUtils
import dev.wuason.unearthMechanic.system.IAnimation
import dev.wuason.unearthMechanic.system.IAnimationData
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class AnimationData(
    private val animation: IAnimation,
    private val animator: Animator
): IAnimationData {
    private var itemMainHand: ItemStack? = null
    private var itemOriginal: ItemStack? = null
    private var task: BukkitTask? = null
    private var animating: Boolean = false

    override fun getItemMainHand(): ItemStack? {
        return itemMainHand
    }

    override fun setItemMainHand(itemMainHand: ItemStack?) {
        if (itemOriginal == null) itemOriginal = itemMainHand
        this.itemMainHand = itemMainHand
    }


    override fun getTask(): BukkitTask? {
        return task
    }

    override fun setTask(task: BukkitTask) {
        this.task = task
    }

    override fun getAnimation(): IAnimation {
        return animation
    }

    override fun isAnimating(): Boolean {
        return animating
    }

    override fun setAnimating(animating: Boolean) {
        this.animating = animating
    }

    override fun running(tick: Long, task: BukkitRunnable) {
        if (!isAnimating()) {
            animator.removeAnimation(animation.getPlayer())
            if (task.isCancelled) return
            task.cancel()
            return
        }
        if (!checkMainHand()) {
            println("Not main hand")
            task.cancel()
            return
        }
        if (tick >= animation.getTicks()) {
            setAnimating(false)
            check()
            returnItemOriginal()
            animator.removeAnimation(animation.getPlayer())
            task.cancel()
        }
    }



    override fun checkMainHand(): Boolean {
        if (animation.getPlayer().inventory.itemInMainHand != animation.getAnimationItem()) {
            println("Not main hand11111")
            check()
            returnItemOriginal()
            animator.removeAnimation(animation.getPlayer())
            setAnimating(false)
            return false
        }
        return true
    }

    override fun returnItemOriginal() {
        itemMainHand?.let { itemMainHand ->
            if (animation.getPlayer().inventory.itemInMainHand.type.isAir) {
                animation.getPlayer().inventory.setItemInMainHand(itemMainHand)
            } else {
                StorageUtils.addItemToInventoryOrDrop(animation.getPlayer(), itemMainHand)
            }
        }
    }

    override fun check() {
        animation.getPlayer().inventory.contents.withIndex().forEach { (index, item) ->
            item?.let {
                if (ItemBuilder.from(item).hasPersistentData(Animator.ANIM_NAMESPACED_KEY)) {
                    animation.getPlayer().inventory.setItem(index, null)
                }
            }
        }
    }

    override fun isOriginal(): Boolean {
        return itemMainHand == itemOriginal
    }
}