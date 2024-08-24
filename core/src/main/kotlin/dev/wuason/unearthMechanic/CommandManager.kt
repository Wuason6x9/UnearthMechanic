package dev.wuason.unearthMechanic

import dev.wuason.libs.commandapi.CommandAPICommand
import dev.wuason.libs.commandapi.executors.CommandArguments
import dev.wuason.libs.commandapi.executors.CommandExecutor
import dev.wuason.libs.commandapi.executors.PlayerCommandExecutor
import dev.wuason.mechanics.utils.AdventureUtils
import dev.wuason.unearthMechanic.system.StageData
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
                    }),
                CommandAPICommand("debug")
                    .withPermission("unearthmechanic.debug")
                    .withSubcommands(
                        CommandAPICommand("block_info")
                            .executesPlayer(PlayerCommandExecutor { player, args ->
                                val block = player.rayTraceBlocks(10.0)?.hitBlock?: run {
                                    AdventureUtils.sendMessage(player, "<red>You need to look at a block")
                                    return@PlayerCommandExecutor
                                }
                                val stageData = StageData.fromBlock(block)?: run {
                                    AdventureUtils.sendMessage(player, "<red>This block doesn't have stage data")
                                    return@PlayerCommandExecutor
                                }
                                AdventureUtils.sendMessage(player, "<yellow>StageData Info:")
                                AdventureUtils.sendMessage(player, "<yellow>Stage actual: <aqua>" + stageData.getStage())
                                AdventureUtils.sendMessage(player, "<yellow>Generic id: <aqua>" + stageData.getGeneric().getId())
                                AdventureUtils.sendMessage(player, "<yellow>BaseItem id: <aqua>" + stageData.getGeneric().getBaseItemId())
                                AdventureUtils.sendMessage(player, "<yellow>Actual item id: <aqua>" + stageData.getActualItemId())
                                AdventureUtils.sendMessage(player, "<yellow>Loc: <aqua>" + stageData.getLocation())
                            })
                    )
            )
            .register()
    }

}