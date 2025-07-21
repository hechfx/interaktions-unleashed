package me.hechfx.bot

import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
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

    val commandManager = UnleashedCommandManager(jda, "/locales", true, guildsToRegister = listOf(1251330087211630633))
        .addSupportForLocale(
            DiscordLocale.ENGLISH_US,
            DiscordLocale.PORTUGUESE_BRAZILIAN
        )

    fun start() {
        commandManager.messageListener.setNewMentionMessage {
            content = "oi"
        }

        commandManager.messageListener.modules.add(MessageReceivedDiscordLinkModule())
        commandManager.messageListener.modules.add(MessageUpdateDiscordLinkModule())

        commandManager.register(PingCommand())
        commandManager.register(PongCommand())

        jda.awaitReady()
    }
}