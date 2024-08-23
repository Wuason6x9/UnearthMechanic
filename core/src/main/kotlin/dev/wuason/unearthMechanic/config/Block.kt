package dev.wuason.unearthMechanic.config

class Block(private val id: String, tools: Set<String>, private val baseItemId: String, private val stages: List<IStage>) : Generic(id, tools, baseItemId, stages), IBlock {
}

