package io.sn.wappinger.setup

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.BooleanArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.sn.wappinger.WapCore
import io.sn.wappinger.utils.WarpUtils
import org.bukkit.Material
import java.io.File
import java.util.concurrent.CompletableFuture

class CommandBus(private val plug: WapCore) {

    @Suppress("SENSELESS_COMPARISON")
    fun init() {
        CommandAPICommand("wappinger")
            .withAliases("warp")
            .withAliases("wap")
            .withSubcommands(
                CommandAPICommand("create")
                    .withArguments(StringArgument("id"))
                    .withArguments(BooleanArgument("follow"))
                    .withArguments(BooleanArgument("hidden"))
                    .executesPlayer(PlayerCommandExecutor { sender, args ->
                        val id = args[0] as String
                        val follow = args[1] as Boolean
                        val hidden = args[2] as Boolean

                        val hand = sender.inventory.itemInMainHand

                        if (hand == null || hand.type == Material.AIR) {
                            plug.sendmsg(sender, "<red>你需要拿着一个当作图标的物品才能创建地标")
                            return@PlayerCommandExecutor
                        }

                        WarpUtils.save(plug, hand, sender.location, id, follow, hidden)
                    }),
                CommandAPICommand("to")
                    .withArguments(StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsAsync { _ ->
                        CompletableFuture.supplyAsync {
                            val storage = File(plug.dataFolder.path + File.separator + "storage")
                            storage.list { _, name ->
                                name.endsWith(".yml")
                            }
                        }
                    }))
                    .executesPlayer(PlayerCommandExecutor { sender, args ->
                        val id = args[0] as String

                        try {
                            WarpUtils.teleport(plug, sender, id)
                        } catch (ex: Exception) {
                            plug.sendmsg(sender, "<red>" + ex.message)
                        }
                    })
            )
            .register()
    }

}