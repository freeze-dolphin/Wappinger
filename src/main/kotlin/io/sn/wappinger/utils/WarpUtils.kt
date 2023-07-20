package io.sn.wappinger.utils

import de.tr7zw.nbtapi.NBT
import io.sn.wappinger.WapCore
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.File

object WarpUtils {

    fun save(
        plug: Plugin,
        icon: ItemStack,
        destination: Location,
        id: String,
        follow: Boolean,
        hidden: Boolean
    ) {
        with(File(plug.dataFolder.path + File.separator + "storage" + id + ".yml")) {
            if (!exists()) {
                createNewFile()
            }

            val yml = YamlConfiguration.loadConfiguration(this)

            yml.set("icon", ItemStack(icon))
            yml.set("destination", destination)
            yml.set("id", id)
            yml.set("followp", follow)
            yml.set("hiddenp", hidden)

            yml.save(this)
        }
    }

    private fun getDestination(plug: WapCore, id: String): Location {
        with(File(plug.dataFolder.path + File.separator + "storage" + id + ".yml")) {
            if (!exists()) {
                createNewFile()
            }

            val yml = YamlConfiguration.loadConfiguration(this)
            return yml.getLocation("destination")!!
        }
    }

    fun teleport(plug: WapCore, plr: Player, id: String) {
        with(File(plug.dataFolder.path + File.separator + "storage" + id + ".yml")) {
            if (!exists()) {
                throw Exception("找不到这个地标")
            }

            NBT.modify(plr) {
                it.setBoolean("wappinger-stand-still", true)
            }

            CountdownTimer(
                plug,
                PlayerPermUtils.getWarpDelay(plr, plug.config.getInt("default-delay")),
                { },
                { // end
                    plr.teleport(getDestination(plug, id))
                }) { // each sec
                if (!NBT.get(plr) { nbt -> nbt.getBoolean("wappinger-stand-still") }) {
                    plug.sendsubtitle(plr, "<red>${"|".repeat(it.secondsLeft)}")
                    it.cancel()
                    return@CountdownTimer
                }

                val phase = it.secondsLeft.toDouble() / it.totalSeconds * 2 - 1
                plug.sendsubtitle(plr, "<transition:blue,aqua,white:$phase>${"|".repeat(it.secondsLeft)}</transition>")
            }.scheduleTimer()
        }
    }

}

