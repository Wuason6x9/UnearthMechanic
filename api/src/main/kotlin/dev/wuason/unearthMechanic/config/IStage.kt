package dev.wuason.unearthMechanic.config

import org.bukkit.Location
import org.bukkit.entity.Player

interface IStage {

    fun getStage(): Int

    fun getItemId(): String?

    fun getDurabilityToRemove(): Int

    fun getUsagesIaToRemove(): Int

    fun isOnlyOneDrop(): Boolean

    fun isRemove(): Boolean

    fun isRemoveItemMainHand(): Boolean

    fun getDrops(): List<IDrop>

    fun getReduceItemHand(): Int

    fun getItems(): List<IItem>

    fun isOnlyOneItem(): Boolean

    fun getSounds(): List<ISound>

    fun getDelay(): Long

    fun isToolAnimDelay(): Boolean

    fun dropItems(loc: Location)

    fun addItems(player: Player)

}