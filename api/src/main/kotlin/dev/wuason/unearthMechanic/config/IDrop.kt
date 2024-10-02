package dev.wuason.unearthMechanic.config

import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

interface IDrop : IItem {
    fun dropItem(loc: Location, applyChance: Boolean): Item?
}