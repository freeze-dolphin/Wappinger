package io.sn.wappinger.utils

import io.sn.wappinger.WapCore
import org.bukkit.Sound
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

object WarpUtils {

    val moveDetectMap: HashMap<UUID, CountdownTimer> = hashMapOf()

    fun save(
        plug: Plugin, icon: ItemStack, destination: Location, id: String, follow: Boolean, hidden: Boolean
    ) {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + id + ".yml")) {
            if (!exists()) {
                createNewFile()
            }

            val yml = YamlConfiguration.loadConfiguration(this)

            val ficon = ItemStack(icon)
            if (!ficon.itemMeta.hasDisplayName()) {
                ficon.editMeta {
                    it.displayName(WapCore.mini("<!italic><white>$id"))
                }
            }

            yml.set("icon", ficon)
            yml.set("destination", destination)
            yml.set("id", id)
            yml.set("followp", follow)
            yml.set("hiddenp", hidden)

            yml.save(this)
        }
    }

    private fun getDestination(yml: YamlConfiguration, plr: Player): Location {
        val dest = yml.getLocation("destination") ?: return plr.location
        val follow = yml.getBoolean("followp")

        if (follow) {
            dest.yaw = plr.location.yaw
            dest.pitch = plr.location.pitch
        }
        return dest
    }

    fun teleport(plug: WapCore, plr: Player, yml: YamlConfiguration) {
        val dest = getDestination(yml, plr)
        val delay = PlayerPermUtils.getWarpDelay(plr, plug.config.getInt("default-delay"))

        if (delay == 0) {
            plr.fallDistance = 0F
            plr.teleport(dest)
            plug.sendtitle(
                plr,
                "<green><bold>✔",
                "<color:#9AFF9A>${plug.getIndicator()}</color></white>"
            )
            plr.playSound(plr.location, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F)
            return
        }

        val timer = CountdownTimer(plug, delay,
            { // before
            }, { // end
                plr.fallDistance = 0F
                plr.teleport(dest)
                plug.sendtitle(
                    plr,
                    "<green><bold>✔",
                    "<green>${
                        plug.getIndicator().repeat(
                            PlayerPermUtils.getWarpDelay(
                                plr, plug.config.getInt("default-delay")
                            )
                        )
                    }</green>"
                )
                plr.playSound(plr.location, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F)
                moveDetectMap.remove(plr.uniqueId)
            }) { // each sec
            val pre = plug.getIndicator().repeat(it.totalSeconds - it.secondsLeft)
            val suf = plug.getIndicator().repeat(it.secondsLeft)
            plug.sendtitle(
                plr,
                "<white>${StringUtils.toSBC(it.secondsLeft.toString())}",
                "<green>$pre</green><gray>$suf</gray>"
            )
            plr.playSound(plr.location, Sound.ENTITY_ITEM_PICKUP, 1F, 1F)
        }
        moveDetectMap[plr.uniqueId] = timer
        moveDetectMap[plr.uniqueId]?.scheduleTimer()
    }

    fun teleport(plug: WapCore, plr: Player, id: String) {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + id + ".yml")) {
            if (!exists()) {
                throw IOException("找不到地标 $id")
            }

            val yml = YamlConfiguration.loadConfiguration(this)

            if (yml.getBoolean("hiddenp") && plr.hasPermission("wappinger.view.${yml.getString("id")}")) {
                teleport(plug, plr, yml)
            } else {
                throw Exception("你没有传送到这个地标的权限")
            }
        }
    }

}
