package dev.wuason.unearthMechanic.config

import org.bukkit.inventory.ItemStack

interface IItem {
    fun getItemStack(): ItemStack

    fun getItemStackChance(): ItemStack?

    fun getItemId(): String
}