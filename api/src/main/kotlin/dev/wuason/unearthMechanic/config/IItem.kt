package dev.wuason.unearthMechanic.config

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface IItem {
    fun getItemStack(): ItemStack

    fun getItemStackChance(): ItemStack?

    fun getItemId(): String

    fun addItem(player: Player, applyChance: Boolean)
}