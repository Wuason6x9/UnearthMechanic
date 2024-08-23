package dev.wuason.unearthMechanic.config

interface IConfigManager {
    fun loadConfig()

    fun getGenerics(): HashMap<String, IGeneric>

    fun getGenericsBaseItemId(): HashMap<String, HashMap<String, IGeneric>>

    fun validBaseItemId(baseItemId: String): Boolean

    fun validTool(baseItemId: String, tool: String): Boolean

    fun getGeneric(baseItemId: String, tool: String): IGeneric?
}