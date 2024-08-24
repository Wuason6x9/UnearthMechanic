package dev.wuason.unearthMechanic.utils

import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.config.IStage
import dev.wuason.unearthMechanic.system.StageData

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
    }
}