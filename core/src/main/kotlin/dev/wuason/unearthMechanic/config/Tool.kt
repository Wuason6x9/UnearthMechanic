package dev.wuason.unearthMechanic.config

import java.util.*

class Tool(private val itemId: String, private val size: Int, private val deep: Int, private val depth: Int, private val sound: Sound?, private val animation: Animation?, private val delay: Long, private val replaceOnBreak: String?): ITool {
    companion object {

        fun Tool(itemId: String): Tool {
            return Tool(itemId, 0, 0, 0, null, null, 0, null)
        }

        fun parseTool(tool: String): Tool {
            val split: List<String> = tool.split(";")
            if (split.size == 1) return Tool(split[0].trim(), 0, 0, 0, null, null, 0, null)
            var size: Int = 0
            var deep: Int = 0
            var depth: Int = 0
            var sound: Sound? = null
            var anim: String? = null
            var delayAnim: Long = 0
            var delay: Long = 0
            var replaceOnBreak: String? = null
            split.filter { split.indexOf(it) != 0 }.forEach {
                val x = it.split("=")
                if (x.size != 2) throw IllegalArgumentException("Invalid tool format")
                when (x[0].lowercase(Locale.ENGLISH).trim()) {
                    "size" -> size = x[1].trim().toInt()
                    "deep" -> deep = x[1].trim().toInt()
                    "depth" -> depth = x[1].trim().toInt()
                    "sound" -> sound = Sound(x[1].trim(), 1.0f, 1.0f, 0)
                    "anim" -> anim = x[1].trim()
                    "delayanim" -> delayAnim = x[1].trim().toLong()
                    "delay" -> delay = x[1].trim().toLong()
                    "replaceonbreak" -> replaceOnBreak = x[1].trim()
                }
            }
            if (depth > 0) {
                if (size < 1) size = 1
                if (deep < 1) deep = 1
            }
            if (size > 0) {
                if (deep < 1) deep = 1
                if (depth < 1) depth = 1
            }
            if (deep > 0) {
                if (size < 1) size = 1
                if (depth < 1) depth = 1
            }
            val animation: Animation? = if (anim != null) Animation(if (delayAnim > 0) delayAnim else -1, anim!!) else null
            return Tool(split[0], size, deep, depth, sound, animation, delay, replaceOnBreak)
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
        val builder: StringBuilder = StringBuilder()
        builder.append(itemId)
        if (size > 0) {
            builder.append(";size=${size}")
        }
        if (deep > 0) {
            builder.append(";deep=${deep}")
        }
        if (depth > 0) {
            builder.append(";depth=${depth}")
        }
        if (sound != null) {
            builder.append(";sound=${sound}")
        }
        if (animation != null) {
            builder.append(";anim=${animation}")
        }
        if (delay > 0) {
            builder.append(";delay=${delay}")
        }
        if (replaceOnBreak != null) {
            builder.append(";replaceonbreak=${replaceOnBreak}")
        }
        return builder.toString()
    }

    override fun isMultiple(): Boolean {
        return size > 0 || deep > 0 || depth > 0
    }

    override fun equals(other: Any?): Boolean {
        if (other is String) return this.itemId == other
        if (this.itemId == (other as Tool).itemId) return true
        return super.equals(other)
    }

    override fun getSound(): ISound? {
        return sound
    }

    override fun getAnimation(): IAnimation? {
        return animation
    }

    override fun getDelay(): Long {
        return delay
    }

    override fun getReplaceOnBreak(): String? {
        return replaceOnBreak
    }

}