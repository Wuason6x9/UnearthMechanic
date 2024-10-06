package dev.wuason.unearthMechanic.config

interface IGeneric {

    fun getId(): String

    fun getTools(): Set<ITool>

    fun getBaseItemId(): String

    fun getStages(): List<IStage>

    fun getStagesItemsId(): HashMap<String, IStage>

    fun getTool(toolId: String): ITool?

    fun existsTool(toolId: String): Boolean

    fun isLastStage(stage: IStage): Boolean

    fun isNotProtect(): Boolean
}