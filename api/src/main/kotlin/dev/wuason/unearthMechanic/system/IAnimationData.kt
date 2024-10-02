package dev.wuason.unearthMechanic.system

import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

interface IAnimationData {
    fun getItemMainHand(): ItemStack?
    fun setItemMainHand(itemMainHand: ItemStack?)
    fun getTask(): BukkitTask?
    fun setTask(task: BukkitTask)
    fun getAnimation(): IAnimation
    fun isAnimating(): Boolean
    fun setAnimating(animating: Boolean)
    fun running(tick: Long, task: BukkitRunnable)
    fun checkMainHand(): Boolean
    fun returnItemOriginal()
    fun check()
    fun isOriginal(): Boolean
}