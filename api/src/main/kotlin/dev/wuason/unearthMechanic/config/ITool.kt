package dev.wuason.unearthMechanic.config


interface ITool {
    fun getItemId(): String
    fun getSize(): Int
    fun getDeep(): Int
    fun getDepth(): Int
    fun isMultiple(): Boolean
    fun getSound(): ISound?
    fun getAnim(): String?
    fun getDelayAnim(): Long
    fun getDelay(): Long
    fun getReplaceOnBreak(): String?
}