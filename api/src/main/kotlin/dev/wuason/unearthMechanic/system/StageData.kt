package dev.wuason.unearthMechanic.system

import dev.wuason.libs.jeffmedia.customblockdata.CustomBlockData
import dev.wuason.libs.jeffmedia.morepersistentdatatypes.DataType
import dev.wuason.unearthMechanic.UnearthMechanicPlugin
import dev.wuason.unearthMechanic.config.IGeneric
import dev.wuason.unearthMechanic.utils.Utils
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer

class StageData(private val location: Location, private val stage: Int, private val generic: IGeneric) {
    companion object  {
        val NAMESPACED_LOC_KEY: NamespacedKey = NamespacedKey(UnearthMechanicPlugin.getInstance(), "loc")
        val NAMESPACED_ID_KEY: NamespacedKey = NamespacedKey(UnearthMechanicPlugin.getInstance(), "id")
        val NAMESPACED_CUR_STAGE_KEY: NamespacedKey = NamespacedKey(UnearthMechanicPlugin.getInstance(), "current_stage")
        val NAMESPACED_KEY: NamespacedKey = NamespacedKey(UnearthMechanicPlugin.getInstance(), "stage")
        val NAMESPACED_SAFE_DELETE_KEY: NamespacedKey = NamespacedKey(UnearthMechanicPlugin.getInstance(), "stageSafeRemove")
        val NAMESPACED_MULTIPLE: NamespacedKey = NamespacedKey(UnearthMechanicPlugin.getInstance(), "stageMultiple")

        fun applySafeDelete(loc: Location) {
            val block: Block = loc.block
            applySafeDelete(block)
        }

        fun applySafeDelete(block: Block) {
            val data: PersistentDataContainer = CustomBlockData(block, UnearthMechanicPlugin.getInstance())
            data.set(NAMESPACED_SAFE_DELETE_KEY, DataType.BOOLEAN, true)
        }

        fun hasSafeDelete(loc: Location): Boolean {
            val block: Block = loc.block
            return hasSafeDelete(block)
        }

        fun hasSafeDelete(block: Block): Boolean {
            return CustomBlockData.hasCustomBlockData(block, UnearthMechanicPlugin.getInstance()) && CustomBlockData(block, UnearthMechanicPlugin.getInstance()).has(
                NAMESPACED_SAFE_DELETE_KEY, DataType.BOOLEAN)
        }

        fun removeSafeDelete(loc: Location) {
            val block: Block = loc.block
            removeSafeDelete(block)
        }

        fun removeSafeDelete(block: Block) {
            if (!hasSafeDelete(block)) return
            val data: PersistentDataContainer = CustomBlockData(block, UnearthMechanicPlugin.getInstance())
            data.remove(NAMESPACED_SAFE_DELETE_KEY)
        }

        fun applyMultiple(loc: Location) {
            val block: Block = loc.block
            applyMultiple(block)
        }

        fun applyMultiple(block: Block) {
            val data: PersistentDataContainer = CustomBlockData(block, UnearthMechanicPlugin.getInstance())
            data.set(NAMESPACED_MULTIPLE, DataType.BOOLEAN, true)
        }

        fun hasMultiple(loc: Location): Boolean {
            val block: Block = loc.block
            return hasMultiple(block)
        }

        fun hasMultiple(block: Block): Boolean {
            return CustomBlockData.hasCustomBlockData(block, UnearthMechanicPlugin.getInstance()) && CustomBlockData(block, UnearthMechanicPlugin.getInstance()).has(
                NAMESPACED_MULTIPLE, DataType.BOOLEAN)
        }

        fun removeMultiple(loc: Location) {
            val block: Block = loc.block
            removeMultiple(block)
        }

        fun removeMultiple(block: Block) {
            if (!hasMultiple(block)) return
            val data: PersistentDataContainer = CustomBlockData(block, UnearthMechanicPlugin.getInstance())
            data.remove(NAMESPACED_MULTIPLE)
        }

        fun fromBlock(block: Block): StageData? {
            if(!CustomBlockData.hasCustomBlockData(block, UnearthMechanicPlugin.getInstance())) {
                return null
            }
            val data: PersistentDataContainer = CustomBlockData(block, UnearthMechanicPlugin.getInstance())
            if (!data.has(NAMESPACED_KEY, DataType.BOOLEAN)) {
                return null
            }
            val loc: Location = data.get(NAMESPACED_LOC_KEY, DataType.LOCATION) ?: return null
            val id: String = data.get(NAMESPACED_ID_KEY, DataType.STRING) ?: return null
            val stage: Int = data.get(NAMESPACED_CUR_STAGE_KEY, DataType.INTEGER) ?: return null
            val generic: IGeneric = UnearthMechanicPlugin.getInstance().getConfigManager().getGenerics()[id] ?: return null
            return StageData(loc, stage, generic)
        }

        fun fromLoc(loc: Location): StageData? {
            val block: Block = loc.block
            return fromBlock(block)
        }

        fun saveStageData(loc: Location, StageData: StageData) {
            val block: Block = loc.block
            saveStageData(block, StageData)
        }

        fun saveStageData(block: Block, StageData: StageData) {
            val data: PersistentDataContainer = CustomBlockData(block, UnearthMechanicPlugin.getInstance())
            data.set(NAMESPACED_LOC_KEY, DataType.LOCATION, StageData.location)
            data.set(NAMESPACED_ID_KEY, DataType.STRING, StageData.generic.getId())
            data.set(NAMESPACED_CUR_STAGE_KEY, DataType.INTEGER, StageData.stage)
            data.set(NAMESPACED_KEY, DataType.BOOLEAN, true)
        }

        fun removeStageData(loc: Location) {
            val block: Block = loc.block
            removeStageData(block)
        }

        fun removeStageData(block: Block) {
            if (!hasStageData(block)) return
            val data: PersistentDataContainer = CustomBlockData(block, UnearthMechanicPlugin.getInstance())
            data.remove(NAMESPACED_LOC_KEY)
            data.remove(NAMESPACED_ID_KEY)
            data.remove(NAMESPACED_CUR_STAGE_KEY)
            data.remove(NAMESPACED_KEY)
        }

        fun hasStageData(loc: Location): Boolean {
            val block: Block = loc.block
            return hasStageData(block)
        }

        fun hasStageData(block: Block): Boolean {
            return CustomBlockData.hasCustomBlockData(block, UnearthMechanicPlugin.getInstance()) && CustomBlockData(block, UnearthMechanicPlugin.getInstance()).has(
                NAMESPACED_KEY, DataType.BOOLEAN)
        }
    }

    fun getLocation(): Location {
        return location
    }

    fun getStage(): Int {
        return stage
    }

    fun getGeneric(): IGeneric {
        return generic
    }

    fun getActualItemId(): String {
        return Utils.getActualItemId(this)
    }

    fun getBackStageData(): StageData? {
        if (stage == 0) return null
        return StageData(location, stage - 1, generic)
    }
}