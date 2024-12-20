package dev.wuason.unearthMechanic.config

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event

open class Generic(private val id: String, private val tools: Set<ITool>, private val baseStage: IStage, private val stages: List<IStage> = mutableListOf(), private val notProtected: Boolean): IGeneric {

    private val stagesItemsId: HashMap<String, IStage> = HashMap<String, IStage>()

    init {
        for (value in stages) {
            value.getItemId()?.let { stagesItemsId[it] = value }
        }
    }

    override fun getId(): String {
        return id
    }

    override fun getTools(): Set<ITool> {
        return tools
    }

    override fun getBaseStage(): IStage {
        return baseStage;
    }

    override fun getStages(): List<IStage> {
        return stages
    }

    override fun getStagesItemsId(): HashMap<String, IStage> {
        return stagesItemsId
    }

    override fun getTool(toolId: String): ITool? {
        return tools.find { it.getItemId().equals(toolId, true) }
    }

    override fun existsTool(toolId: String): Boolean {
        return tools.any { it.getItemId().equals(toolId, true) }
    }

    override fun isLastStage(stage: IStage): Boolean {
        return stage.getStage() == stages.size - 1
    }

    override fun isNotProtect(): Boolean {
        return notProtected
    }

    override fun getBackStage(currentStage: IStage): IStage {
        if (currentStage.getStage() <= 0) return baseStage
        return stages[currentStage.getStage() - 1]
    }
}