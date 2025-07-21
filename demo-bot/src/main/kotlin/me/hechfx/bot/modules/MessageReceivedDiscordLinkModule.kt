package me.hechfx.bot.modules

import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.perfectdreams.interactions.events.message.MessageModule

/**
 * Example of a simple message module
 */
class MessageReceivedDiscordLinkModule : MessageModule<MessageReceivedEvent>() {
    override fun canHandle(event: GenericMessageEvent): Boolean = event is MessageReceivedEvent

    override fun listen(event: MessageReceivedEvent) {
        if (event.isFromGuild) {
            if (event.message.contentRaw.contains("discord.com")) {
                event.message.delete().queue()
                event.channel.sendMessage("you can't send discord links here").queue()
            }
        }
    }
}

class MessageUpdateDiscordLinkModule : MessageModule<MessageUpdateEvent>() {
    override fun canHandle(event: GenericMessageEvent): Boolean = event is MessageUpdateEvent

    override fun listen(event: MessageUpdateEvent) {
        if (event.isFromGuild) {
            if (event.message.contentRaw.contains("discord.com")) {
                event.message.delete().queue()
                event.channel.sendMessage("you can't send discord links here").queue()
            }
        }
    }
}