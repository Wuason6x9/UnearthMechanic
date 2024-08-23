package dev.wuason.unearthMechanic.config

open class Generic(private val id: String, private val tools: Set<String>, private val baseItemId: String, private val stages: List<IStage>): IGeneric {

    private val stagesItemsId: HashMap<String, IStage> = HashMap<String, IStage>()

    init {
        for (value in stages) {
            value.getItemId()?.let { stagesItemsId[it] = value }
        }
    }

    override fun getId(): String {
        return id
    }

    override fun getTools(): Set<String> {
        return tools
    }

    override fun getBaseItemId(): String {
        return baseItemId
    }

    override fun getStages(): List<IStage> {
        return stages
    }

    override fun getStagesItemsId(): HashMap<String, IStage> {
        return stagesItemsId
    }
}