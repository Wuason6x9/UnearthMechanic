package dev.wuason.unearthMechanic.config

class FurnitureStage(
    stage: Int,
    itemId: String?,
    drops: List<Drop>,
    remove: Boolean,
    removeItemMainHand: Boolean,
    durabilityToRemove: Int,
    usagesIaToRemove: Int,
    onlyOneDrop: Boolean,
    reduceItemHand: Int,
    items: List<Item>,
    onlyOneItem: Boolean,
    sounds: List<Sound>,
    delay: Long,
    toolAnimDelay: Boolean
) : Stage(
    stage, itemId, drops, remove, removeItemMainHand, durabilityToRemove, usagesIaToRemove,
    onlyOneDrop, reduceItemHand, items, onlyOneItem, sounds, delay, toolAnimDelay
), IFurnitureStage