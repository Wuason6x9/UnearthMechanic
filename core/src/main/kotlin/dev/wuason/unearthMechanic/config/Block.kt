package dev.wuason.unearthMechanic.config

class Block(private val id: String, tools: Set<ITool>, private val baseStage: BlockStage, private val stages: List<IStage>, notProtected: Boolean) : Generic(id, tools, baseStage, stages, notProtected), IBlock {
}

