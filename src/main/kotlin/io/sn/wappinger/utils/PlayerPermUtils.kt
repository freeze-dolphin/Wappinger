package io.sn.wappinger.utils

import org.bukkit.entity.Player

object PlayerPermUtils {

    fun getWarpDelay(plr: Player, default: Int): Int {
        return plr.effectivePermissions.filter {
            it.permission.startsWith("wappinger.delay.")
        }.maxOfOrNull {
            it.permission.split(".")[2].toInt()
        } ?: default
    }

}
