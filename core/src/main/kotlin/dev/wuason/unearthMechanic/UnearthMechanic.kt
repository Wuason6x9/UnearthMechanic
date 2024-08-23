package dev.wuason.unearthMechanic

import dev.wuason.mechanics.utils.AdventureUtils
import dev.wuason.unearthMechanic.config.ConfigManager
import dev.wuason.unearthMechanic.system.IStageManager
import dev.wuason.unearthMechanic.system.StageManager
import org.bukkit.Bukkit

class UnearthMechanic : UnearthMechanicPlugin() {

    companion object {

        val COMPATIBILITIES: Array<String> = arrayOf(
            "ItemsAdder",
            "Oraxen"
        )

        private lateinit var instance: UnearthMechanic

        fun getInstance(): UnearthMechanic {
            return instance
        }
    }

    init {
        instance = this
    }

    private lateinit var commandManager: CommandManager
    private lateinit var configManager: ConfigManager
    private lateinit var stageManager: StageManager

    override fun onMechanicEnable() {

        AdventureUtils.sendMessagePluginConsole(this, " <gold>Starting UnearthMechanic...")
        AdventureUtils.sendMessagePluginConsole(this, "<gray>-----------------------------------------------------------")
        AdventureUtils.sendMessagePluginConsole(this, "<gray>-----------------------------------------------------------")
        AdventureUtils.sendMessagePluginConsole(this, "<gold>                         $name");
        AdventureUtils.sendMessagePluginConsole(this, "");
        AdventureUtils.sendMessagePluginConsole(this, "<gold> Selected compatibility: <aqua>${checkCompatibility()}");

        if (check()) return

        configManager = ConfigManager(this)
        configManager.loadConfig()

        commandManager = CommandManager(this)
        commandManager.loadCommands()

        stageManager = StageManager(this)
        AdventureUtils.sendMessagePluginConsole(this, "<gray>-----------------------------------------------------------")
        AdventureUtils.sendMessagePluginConsole(this, "<gray>-----------------------------------------------------------")

    }

    override fun onMechanicDisable() {

    }

    override fun getConfigManager(): ConfigManager {
        return configManager
    }

    override fun getCommandManager(): CommandManager {
        return commandManager
    }

    override fun getStageManager(): IStageManager {
        return stageManager
    }

    private fun checkCompatibility(): String? {
        for (compatibility in COMPATIBILITIES) {
            if (Bukkit.getPluginManager().getPlugin(compatibility) != null) {
                return compatibility
            }
        }
        return null
    }

    private fun check(): Boolean {
        if (checkCompatibility() == null) {
            logger.severe("-----------------------------------------------------------")
            logger.severe("-----------------------------------------------------------")
            logger.severe("                 UnearthMechanic is disabled               ")
            logger.severe("       None of the required dependencies were detected     ")
            logger.severe("      " + COMPATIBILITIES.joinToString(" or ") + " are required")
            logger.severe("-----------------------------------------------------------")
            logger.severe("-----------------------------------------------------------")
            Bukkit.getPluginManager().disablePlugin(this)
            return true
        }
        return false
    }

}
