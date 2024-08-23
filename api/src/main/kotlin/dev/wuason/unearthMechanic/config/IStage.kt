package dev.wuason.unearthMechanic.config

interface IStage {

    fun getStage(): Int

    fun getItemId(): String?

    fun getDurabilityToRemove(): Int

    fun getUsagesIaToRemove(): Int

    fun isOnlyOneDrop(): Boolean

    fun isRemove(): Boolean

    fun isRemoveItemMainHand(): Boolean

    fun getDrops(): List<IDrop>
}