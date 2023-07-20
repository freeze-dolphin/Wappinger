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
                    it.displayName(WapCore.minimsg.deserialize("<white>$id"))
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

    private fun getDestination(plug: WapCore, id: String, plr: Player): Location {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + id + ".yml")) {
            if (!exists()) {
                throw IOException("找不到这个地标")
            }

            val yml = YamlConfiguration.loadConfiguration(this)
            val dest = yml.getLocation("destination") ?: return plr.location
            val follow = yml.getBoolean("followp")

            if (follow) {
                dest.yaw = plr.location.yaw
                dest.pitch = plr.location.pitch
            }
            return dest
        }
    }

    fun teleport(plug: WapCore, plr: Player, id: String) {
        with(File(plug.dataFolder.path + File.separator + "storage" + File.separator + id + ".yml")) {
            if (!exists()) {
                throw IOException("找不到这个地标")
            }
        }

        val dest = getDestination(plug, id, plr)
        val delay = PlayerPermUtils.getWarpDelay(plr, plug.config.getInt("default-delay"))

        if (delay == 0) {
            plr.teleport(dest)
            plug.sendtitle(
                plr,
                "<green><bold>✔",
                "<white>|<color:#9AFF9A>${plug.getIndicator()}</color> |</white>"
            )
            plr.playSound(plr.location, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F)
            return
        }

        val timer = CountdownTimer(plug, delay,
            { // before
            }, { // end
                plr.teleport(dest)
                plug.sendtitle(
                    plr,
                    "<green><bold>✔",
                    "<white>|<green>${
                        plug.getIndicator().repeat(
                            PlayerPermUtils.getWarpDelay(
                                plr, plug.config.getInt("default-delay")
                            )
                        )
                    }</green> |</white>"
                )
                plr.playSound(plr.location, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F)
                moveDetectMap.remove(plr.uniqueId)
            }) { // each sec
            val pre = plug.getIndicator().repeat(it.totalSeconds - it.secondsLeft)
            val suf = plug.getIndicator().repeat(it.secondsLeft)
            plug.sendtitle(
                plr,
                "<white>${StringUtils.toSBC(it.secondsLeft.toString())}",
                "<white>|<green>$pre</green><gray>$suf</gray> |</white>"
            )
            plr.playSound(plr.location, Sound.ENTITY_ITEM_PICKUP, 1F, 1F)
        }
        moveDetectMap[plr.uniqueId] = timer
        moveDetectMap[plr.uniqueId]?.scheduleTimer()
    }

}
