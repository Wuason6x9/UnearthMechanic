package dev.wuason.unearthMechanic.config

import dev.wuason.mechanics.items.ItemBuilder
import dev.wuason.mechanics.utils.MathUtils
import org.bukkit.inventory.ItemStack

class Drop(private val itemId: String, private val amount: String, private val chance: Int) : Item(itemId, amount, chance), IDrop {
}
