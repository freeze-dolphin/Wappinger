package io.sn.wappinger

import io.sn.wappinger.setup.CommandBus
import io.sn.wappinger.setup.ListenerBus
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.TitlePart
import org.bukkit.plugin.java.JavaPlugin

class WapCore : JavaPlugin() {

    companion object {
        private val minimsg: MiniMessage = MiniMessage.miniMessage()
    }

    override fun onEnable() {
        setupConfig()
        setupCommands()
        setupListeners()
    }

    private fun setupListeners() {
        ListenerBus(this).init()
    }

    private fun setupCommands() {
        CommandBus(this).init()
    }

    private fun setupConfig() {
        saveDefaultConfig()
    }

    fun sendmsg(audience: Audience, msg: String) {
        audience.sendMessage(minimsg.deserialize(config.getString("prefix") + msg))
    }

    fun sendsubtitle(audience: Audience, msg: String) {
        audience.sendTitlePart(TitlePart.SUBTITLE, minimsg.deserialize(config.getString("prefix") + msg))
    }

}
