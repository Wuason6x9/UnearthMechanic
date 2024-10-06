package dev.wuason.unearthMechanic.config


interface ITool {
    fun getItemId(): String
    fun getSize(): Int
    fun getDeep(): Int
    fun getDepth(): Int
    fun isMultiple(): Boolean
    fun getSound(): ISound?
    fun getAnimation(): IAnimation?
    fun getDelay(): Long
    fun getReplaceOnBreak(): String?
}