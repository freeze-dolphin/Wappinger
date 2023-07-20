package io.sn.wappinger

import io.sn.wappinger.setup.CommandBus
import io.sn.wappinger.setup.ListenerBus
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.plugin.java.JavaPlugin
import xyz.janboerman.guilib.GuiLibrary
import xyz.janboerman.guilib.api.GuiListener
import java.io.File
import java.time.Duration

class WapCore : JavaPlugin() {

    lateinit var guilistener: GuiListener

    companion object {
        val minimsg: MiniMessage = MiniMessage.miniMessage()
    }

    override fun onEnable() {
        val guilib = server.pluginManager.getPlugin("GuiLib") as GuiLibrary
        guilistener = guilib.guiListener

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

        with(File(dataFolder.path + File.separator + "storage")) {
            if (!exists()) {
                mkdir()
            }
        }
    }

    fun sendmsg(audience: Audience, msg: String) {
        audience.sendMessage(minimsg.deserialize(config.getString("prefix") + msg))
    }

    fun sendtitle(audience: Audience, main: String, sub: String) {
        audience.clearTitle()
        audience.showTitle(
            Title.title(
                minimsg.deserialize(main),
                minimsg.deserialize(sub),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
            )
        )
        //, minimsg.deserialize(config.getString("prefix") + msg))
    }

    fun getIndicator(): String {
        return " ${config.getString("indicator")}"
    }

}
