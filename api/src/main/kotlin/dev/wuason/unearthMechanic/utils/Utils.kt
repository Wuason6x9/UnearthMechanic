package dev.wuason.unearthMechanic.utils

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.StageData
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import kotlin.math.absoluteValue

class Utils {
    companion object {
        fun isLastStage(stage: Int, generic: IGeneric): Boolean {
            return stage == generic.getStages().size - 1
        }

        fun isLastStage(stage: IStage, generic: IGeneric): Boolean {
            return stage.getStage() == generic.getStages().size - 1
        }

        fun getActualItemId(stageData: StageData): String {
            for (stage in stageData.getStage() downTo 0) {
                stageData.getGeneric().getStages()[stage].getItemId()?.let { return it }
            }
            return stageData.getGeneric().getBaseItemId()
        }

        fun blockAround(block: Block, size: Int, deep: Int, depth: Int, player: Player, blockFace: BlockFace): List<Block> {
            if (depth == 0) throw IllegalArgumentException("Depth cannot be 0")
            val blocks: MutableList<Block> = mutableListOf()
            for (d in 0 until depth.absoluteValue) {
                for (i in -deep..deep) {
                    for (y in -size..size) {

                        if (blockFace.direction.blockY != 0) {
                            if (player.facing.direction.blockZ != 0) blocks.add(block.getRelative(i, d * (blockFace.direction.blockY * -1), y))
                            else blocks.add(block.getRelative(y, d * (blockFace.direction.blockY * -1), i))
                        }
                        else {
                            if (player.facing.direction.blockZ != 0) blocks.add(block.getRelative(i, y, d * player.facing.direction.blockZ))
                            else blocks.add(block.getRelative(d * player.facing.direction.blockX, y, i))
                        }

                    }

                }
            }
            return blocks
        }
    }
}