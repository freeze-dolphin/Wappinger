package io.sn.wappinger.listeners

import de.tr7zw.nbtapi.NBT
import io.sn.wappinger.WapCore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class TeleportMoveListener(@Suppress("unused") private val plug: WapCore) : Listener {

    @EventHandler
    fun onMove(evt: PlayerMoveEvent) {
        if (evt.isCancelled) return
        if (evt.hasChangedBlock()) {
            NBT.modify(evt.player) {
                it.setBoolean("wappinger-stand-still", false)
            }
        }
    }

}
