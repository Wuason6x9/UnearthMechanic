package dev.wuason.unearthMechanic.system

import dev.wuason.unearthMechanic.config.ITool
import org.bukkit.inventory.ItemStack

interface ILiveTool {
    fun getItemMainHand(): ItemStack?
    fun getITool(): ITool
    fun setItemMainHand(item: ItemStack)
    fun isValid(): Boolean
}