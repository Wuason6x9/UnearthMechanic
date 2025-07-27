package dev.wuason.unearthMechanic.events

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class FakePlayerInteractEvent(
    player: Player,
    clickedBlock: Block?,
    item: ItemStack?,
    hand: EquipmentSlot?
) : PlayerInteractEvent(
    player,
    Action.RIGHT_CLICK_BLOCK,
    item,
    clickedBlock,
    BlockFace.UP,
    hand
)



