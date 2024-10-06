package dev.wuason.unearthMechanic.system.animations

import org.bukkit.inventory.ItemStack

interface IAnimationRunner {
    fun setItemMainHand(item: ItemStack)

    fun getItemMainHand(): ItemStack

    fun isRunning(): Boolean

    fun isValid(): Boolean

    fun updateItemMainHandData()
}