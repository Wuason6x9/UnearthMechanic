package dev.wuason.unearthMechanic.config

import dev.wuason.mechanics.items.ItemBuilder
import dev.wuason.mechanics.utils.MathUtils
import org.bukkit.inventory.ItemStack

class Drop(private val itemId: String, private val amount: String, private val chance: Int) : IDrop {

    override fun getItemStackChance(): ItemStack? {
        return if (isDroppable()) ItemBuilder(itemId, getRandAmount()).build() else null
    }

    override fun getItemStack(): ItemStack {
        return ItemBuilder(itemId, getRandAmount()).build()
    }

    private fun isDroppable(): Boolean {
        return MathUtils.chance(chance.toFloat())
    }

    private fun getRandAmount(): Int {
        return MathUtils.randomNumberString(amount)
    }

    override fun getItemId(): String {
        return itemId
    }
}
