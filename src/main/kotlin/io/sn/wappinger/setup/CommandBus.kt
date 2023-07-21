package io.sn.wappinger.setup

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.BooleanArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import io.sn.wappinger.WapCore
import io.sn.wappinger.utils.GuiUtils
import io.sn.wappinger.utils.WarpUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

class CommandBus(private val plug: WapCore) {

    @Suppress("SENSELESS_COMPARISON")
    fun init() {
        CommandAPICommand("wappinger").withAliases("warp", "wap").withSubcommands(
            CommandAPICommand("create").withArguments(StringArgument("id"))
                .withOptionalArguments(BooleanArgument("follow"))
                .withOptionalArguments(BooleanArgument("hidden")).withPermission(CommandPermission.OP)
                .executesPlayer(PlayerCommandExecutor { sender, args ->
                    val id = args[0] as String
                    val follow = args.getOptional("follow").orElse(false) as Boolean
                    val hidden = args.getOptional("hidden").orElse(false) as Boolean

                    val hand = sender.inventory.itemInMainHand

                    if (hand == null || hand.type == Material.AIR) {
                        plug.sendmsg(sender, "<red>你需要拿着一个当作图标的物品才能创建地标")
                        return@PlayerCommandExecutor
                    }

                    WarpUtils.save(plug, hand, sender.location, id, follow, hidden)
                    plug.sendmsg(sender, "<green>新增地标: <white>$id")
                }),
            CommandAPICommand("to").withArguments(StringArgument("id").replaceSuggestions(ArgumentSuggestions.stringsAsync { sender ->
                CompletableFuture.supplyAsync {
                    if (sender.sender is Player) {
                        val waps = GuiUtils.fetch(sender.sender as Player) ?: throw Exception("目前还没有可用的地标")
                        waps.map {
                            it.getString("id")
                        }.toTypedArray()
                    } else {
                        emptyArray()
                    }
                }
            })).executesPlayer(PlayerCommandExecutor { sender, args ->
                val id = args[0] as String

                try {
                    WarpUtils.teleport(plug, sender, id)
                } catch (ex: Exception) {
                    plug.sendmsg(sender, "<red>" + ex.message)
                }
            }),
            CommandAPICommand("gui").withOptionalArguments(IntegerArgument("page").replaceSuggestions(ArgumentSuggestions.stringsAsync { sender ->
                CompletableFuture.supplyAsync {
                    if (sender.sender is Player) {
                        val waps = GuiUtils.fetch(sender.sender as Player) ?: throw Exception("目前还没有可用的地标")
                        val totalPage = if (waps.size % 45 == 0) waps.size / 45 else waps.size / 45 + 1
                        (0 until totalPage).toList().map {
                            it.inc().toString()
                        }.toTypedArray()
                    } else {
                        emptyArray()
                    }
                }
            })).executesPlayer(PlayerCommandExecutor { sender, args ->
                val page = args.getOptional("page").orElse(1) as Int

                try {
                    GuiUtils.openGuiFor(plug, sender, page - 1)
                } catch (ob: IndexOutOfBoundsException) {
                    plug.sendmsg(sender, "<red>不存在这一页")
                } catch (ex: Exception) {
                    plug.sendmsg(sender, "<red>" + ex.message)
                }
            })
        ).register()
    }

}