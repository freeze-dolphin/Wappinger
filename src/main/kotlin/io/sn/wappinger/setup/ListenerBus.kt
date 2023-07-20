package io.sn.wappinger.setup

import io.sn.wappinger.WapCore
import io.sn.wappinger.listeners.TeleportMoveListener

class ListenerBus(private val plug: WapCore) {

    fun init() {
        plug.server.pluginManager.registerEvents(TeleportMoveListener(plug), plug)
    }

}
