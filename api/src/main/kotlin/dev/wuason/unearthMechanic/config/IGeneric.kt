package dev.wuason.unearthMechanic.config

interface IGeneric {

    fun getId(): String

    fun getTools(): Set<String>

    fun getBaseItemId(): String

    fun getStages(): List<IStage>

    fun getStagesItemsId(): HashMap<String, IStage>

}