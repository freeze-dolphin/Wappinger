@file:Suppress("DEPRECATION")

package io.sn.wappinger.utils

import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import io.sn.slimefun4.ChestMenuTexture
import io.sn.wappinger.WapCore
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

object GuiUtils {

    private val UI_BACKGROUND: ItemStack = CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " ").setCustomModel(4000)
    private val ARROW_LEFT: ItemStack = CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " ").setCustomModel(4008)
    private val ARROW_RIGHT: ItemStack = CustomItemStack(Material.GRAY_STAINED_GLASS_PANE, " ").setCustomModel(4009)

    fun fetch(plr: Player): List<YamlConfiguration>? {
        val storage = File(Bukkit.getPluginManager().getPlugin("Wappinger")!!.dataFolder.path + File.separator + "storage")
        return storage.listFiles { _, name ->
            name.endsWith(".yml")
        }?.map {
            YamlConfiguration.loadConfiguration(it)
        }?.sortedBy {
            it.getString("id")
        }?.filter {
            if (it.getBoolean("hiddenp")) {
                plr.hasPermission("wappinger.view.${it.getString("id")}")
            } else true
        }
    }

    /**
     * @param page zero-indexed page number
     */
    private fun drawGui(plr: Player, page: Int): Pair<List<YamlConfiguration>, PageState> {
        val waps = fetch(plr) ?: throw Exception("目前还没有可用的地标")
        val totalPage = if (waps.size % 45 == 0) waps.size / 45 else waps.size / 45 + 1

        if (totalPage == 0) throw Exception("目前还没有可用的地标")

        if (page !in 0..totalPage) throw Exception("不存在这一页")

        var state: PageState = PageState.NORMAL
        val endIdx: Int

        if (page == totalPage - 1) {
            endIdx = waps.size
            state = PageState.LAST
        } else {
            endIdx = 45 * (page + 1)
        }

        if (page == 0) state = PageState.HOME

        if (totalPage == 1) state = PageState.ONLY_ONE

        return Pair(waps.slice(45 * page until endIdx), state)
    }

    /**
     * @param page zero-indexed page number
     */
    fun openGuiFor(plug: WapCore, plr: Player, page: Int) {
        val inv = ChestMenu("&5可用传送点列表 &7- &8${page + 1}", ChestMenuTexture("dumortierite", "warp_list"))

        val (ymls, state) = drawGui(plr, page)

        ymls.forEachIndexed { index, yml ->
            inv.addItem(index, yml.getItemStack("icon")) { p, _, _, _ ->
                try {
                    WarpUtils.teleport(plug, plr, yml)
                } catch (ex: Exception) {
                    plug.sendmsg(p, "<red>${ex.message}")
                }
                p.closeInventory()
                Bukkit.getScheduler().runTaskLater(plug, Runnable {
                    @Suppress("UnstableApiUsage")
                    p.updateInventory()
                }, 10L)
                return@addItem false
            }
        }

        (45..53).forEach {
            when (it) {
                46 -> {
                    inv.addItem(
                        it,
                        if (state != PageState.ONLY_ONE && state != PageState.HOME) ARROW_LEFT else UI_BACKGROUND,
                        if (state != PageState.ONLY_ONE && state != PageState.HOME) {
                            ChestMenu.MenuClickHandler { _, _, _, _ ->
                                openGuiFor(plug, plr, page - 1)
                                return@MenuClickHandler false
                            }
                        } else {
                            ChestMenuUtils.getEmptyClickHandler()
                        }
                    )
                }

                52 -> {
                    inv.addItem(it,
                        if (state != PageState.ONLY_ONE && state != PageState.LAST) ARROW_RIGHT else UI_BACKGROUND,
                        if (state != PageState.ONLY_ONE && state != PageState.LAST) {
                            ChestMenu.MenuClickHandler { _, _, _, _ ->
                                openGuiFor(plug, plr, page + 1)
                                return@MenuClickHandler false
                            }
                        } else {
                            ChestMenuUtils.getEmptyClickHandler()
                        })
                }

                else -> {
                    inv.addItem(it, UI_BACKGROUND, ChestMenuUtils.getEmptyClickHandler())
                }
            }
        }

        inv.open(plr)
    }

}

enum class PageState {
    HOME, LAST, NORMAL, ONLY_ONE
}
