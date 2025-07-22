package me.hechfx.bot

import me.hechfx.bot.commands.ComponentsCommand
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.GatewayIntent
import me.hechfx.bot.commands.PingCommand
import me.hechfx.bot.commands.PongCommand
import me.hechfx.bot.modules.MessageReceivedDiscordLinkModule
import me.hechfx.bot.modules.MessageUpdateDiscordLinkModule
import net.dv8tion.jda.api.JDABuilder
import net.perfectdreams.interactions.UnleashedCommandManager

class Bot(val token: String) {
    val jda by lazy {
        JDABuilder.createLight(token)
            .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .build()
    }

    val commandManager = UnleashedCommandManager(jda) {
        prefix = "+"

        setMentionMessage {
            content = "oi"
        }

        expiredComponentMessage {
            content = "expired component! please use the command again."
        }

        supportLocales(DiscordLocale.PORTUGUESE_BRAZILIAN, DiscordLocale.ENGLISH_US)

        enableLocale("/locales")

        forGuild(1251330087211630633)
    }

    fun start() {
        commandManager.messageListener.modules.add(MessageReceivedDiscordLinkModule())
        commandManager.messageListener.modules.add(MessageUpdateDiscordLinkModule())

        commandManager.register(PingCommand())
        commandManager.register(PongCommand())
        commandManager.register(ComponentsCommand())

        jda.awaitReady()
    }
}