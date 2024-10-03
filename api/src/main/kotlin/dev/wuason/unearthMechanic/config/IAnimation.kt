package dev.wuason.unearthMechanic.config

import org.bukkit.inventory.ItemStack

interface IAnimation {
    fun getAnimationItem(): ItemStack
    fun getTicks(): Long
}