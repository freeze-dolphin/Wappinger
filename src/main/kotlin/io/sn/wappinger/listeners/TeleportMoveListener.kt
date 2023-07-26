package io.sn.wappinger.listeners

import io.sn.wappinger.WapCore
import io.sn.wappinger.utils.WarpUtils
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent

class TeleportMoveListener(@Suppress("unused") private val plug: WapCore) : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onMove(evt: PlayerMoveEvent) {
        if (evt.hasChangedBlock() && WarpUtils.moveDetectMap.containsKey(evt.player.uniqueId)) {
            val timer = WarpUtils.moveDetectMap[evt.player.uniqueId]
            timer?.cancel()
            plug.sendtitle(evt.player, "<red><bold>✘", "<gray>${plug.getIndicator().repeat(timer!!.totalSeconds)}</gray>")
            evt.player.playSound(evt.player.location, Sound.ENTITY_ITEM_BREAK, 1F, 1F)
            WarpUtils.moveDetectMap.remove(evt.player.uniqueId)
        }
    }

    @EventHandler
    fun onJoin(evt: PlayerJoinEvent) {
        WarpUtils.moveDetectMap.remove(evt.player.uniqueId)
    }

}
