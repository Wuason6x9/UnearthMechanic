package dev.wuason.unearthMechanic.config

import java.util.*

class Tool(private val itemId: String, private val size: Int, private val deep: Int, private val depth: Int): ITool {
    companion object {

        fun Tool(itemId: String): Tool {
            return Tool(itemId, 0, 0, 0)
        }

        fun parseTool(tool: String): Tool {
            val split: List<String> = tool.split(";")
            if (split.size == 1) return Tool(split[0].trim(), 0, 0, 0)
            var size: Int = 1
            var deep: Int = 1
            var depth: Int = 1
            split.filter { split.indexOf(it) != 0 }.forEach {
                val x = it.split("=")
                if (x.size != 2) throw IllegalArgumentException("Invalid tool format")
                when (x[0].lowercase(Locale.ENGLISH).trim()) {
                    "size" -> size = x[1].trim().toInt()
                    "deep" -> deep = x[1].trim().toInt()
                    "depth" -> depth = x[1].trim().toInt()
                }
            }
            return Tool(split[0], size, deep, depth)
        }
    }

    override fun getItemId(): String {
        return itemId
    }

    override fun getSize(): Int {
        return size
    }

    override fun getDeep(): Int {
        return deep
    }

    override fun getDepth(): Int {
        return depth
    }

    override fun toString(): String {
        return "$itemId;depth=$depth;deep=$deep;size=$size"
    }

    override fun isMultiple(): Boolean {
        return size > 0 || deep > 0 || depth > 0
    }

    override fun equals(other: Any?): Boolean {
        if (other is String) return this.itemId == other
        if (this.itemId == (other as Tool).itemId) return true
        return super.equals(other)
    }

}