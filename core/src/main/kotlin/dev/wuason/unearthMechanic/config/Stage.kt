package dev.wuason.unearthMechanic.config

class Stage(
    private val stage: Int, private val itemId: String?, private val drops: List<Drop>, private val remove: Boolean, private val removeItemMainHand: Boolean,
    private val durabilityToRemove: Int,
    private val usagesIaToRemove: Int, private val onlyOneDrop: Boolean,
    private val reduceItemHand: Int, private val items: List<Item>, private val onlyOneItem: Boolean
) : IStage {

    override fun isRemoveItemMainHand(): Boolean {
        return removeItemMainHand
    }

    override fun getUsagesIaToRemove(): Int {
        return usagesIaToRemove
    }

    override fun isRemove(): Boolean {
        return remove
    }

    override fun isOnlyOneDrop(): Boolean {
        return onlyOneDrop
    }

    override fun getDurabilityToRemove(): Int {
        return durabilityToRemove
    }

    override fun getItemId(): String? {
        return itemId
    }

    override fun getStage(): Int {
        return stage
    }

    override fun getDrops(): List<Drop> {
        return drops
    }

    override fun getReduceItemHand(): Int {
        return reduceItemHand
    }

    override fun getItems(): List<Item> {
        return items
    }

    override fun isOnlyOneItem(): Boolean {
        return onlyOneItem
    }
}
