package dev.wuason.unearthMechanic

import dev.wuason.mechanics.mechanics.MechanicAddon
import dev.wuason.unearthMechanic.config.IConfigManager
import dev.wuason.unearthMechanic.system.IStageManager

abstract class UnearthMechanicPlugin : MechanicAddon(23153) {
    companion object {
        private lateinit var instance: UnearthMechanicPlugin

        fun getInstance(): UnearthMechanicPlugin {
            return instance
        }
    }

    init {
        this.also { instance = it }
    }

    abstract fun getConfigManager(): IConfigManager

    abstract fun getCommandManager(): ICommandManager

    abstract fun getStageManager(): IStageManager


}