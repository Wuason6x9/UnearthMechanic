package dev.wuason.unearthMechanic.config

import org.bukkit.Location
import org.bukkit.inventory.ItemStack

class Drop(private val itemId: String, private val amount: String, private val chance: Int) : Item(itemId, amount, chance), IDrop {

    override fun dropItem(loc: Location, applyChance: Boolean): org.bukkit.entity.Item? {
        val item: ItemStack? = if (applyChance) getItemStackChance() else getItemStack()
        return if (item != null) loc.world.dropItem(loc, item) else null
    }

}
