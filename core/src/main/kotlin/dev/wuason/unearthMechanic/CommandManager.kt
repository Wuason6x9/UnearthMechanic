package dev.wuason.unearthMechanic

import dev.wuason.libs.commandapi.CommandAPICommand
import dev.wuason.libs.commandapi.executors.CommandArguments
import dev.wuason.libs.commandapi.executors.CommandExecutor
import dev.wuason.mechanics.utils.AdventureUtils
import org.bukkit.command.CommandSender

class CommandManager(private val core: UnearthMechanic) : ICommandManager {

    fun loadCommands() {
        CommandAPICommand("unearthmechanic")
            .withPermission("unearthmechanic.main")
            .withAliases("unearth", "unearthm", "uth", "uthmechanic")
            .withSubcommands(
                CommandAPICommand("reload")
                    .withAliases("r")
                    .withPermission("unearthmechanic.reload")
                    .executes(CommandExecutor { commandSender: CommandSender?, commandArguments: CommandArguments? ->
                        AdventureUtils.sendMessage(commandSender, "<red>Reloading...")
                        core.getConfigManager().loadConfig()
                    })
            )
            .register()
    }

}