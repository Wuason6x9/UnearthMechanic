package dev.wuason.unearthMechanic.system

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface IAnimation {
    fun getPlayer(): Player
    fun getLocation(): Location
    fun getAnimationItem(): ItemStack
    fun getTicks(): Long
}