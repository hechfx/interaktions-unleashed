package net.perfectdreams.interactions.extensions

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.ActionComponent
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction

/**
 * Make the message a reply to the referenced message.
 *
 * This checks if the bot has [net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] and, if it has, the message is referenced.
 *
 * @param message The target message
 *
 * @return Updated MessageAction for chaining convenience
 */
fun MessageCreateAction.referenceIfPossible(message: Message): MessageCreateAction {
    if (message.isFromGuild && !message.guild.selfMember.hasPermission(message.channel as GuildChannel, Permission.MESSAGE_HISTORY))
        return this
    return this.setMessageReference(message)
}

/**
 * Make the message a reply to the referenced message.
 *
 * This has the same checks as [referenceIfPossible] plus a check to see if [addInlineReply] is enabled and to check if [ServerConfig.deleteMessageAfterCommand] is false.
 *
 * @param message The target message
 *
 * @return Updated MessageAction for chaining convenience
 */
fun MessageCreateAction.referenceIfPossible(message: Message, addInlineReply: Boolean = true): MessageCreateAction {
    // We check if deleteMessageAfterCommand is true because it doesn't matter trying to reply to a message that's going to be deleted.
    if (!addInlineReply)
        return this
    return this.referenceIfPossible(message)
}

fun Guild.getGuildMessageChannelByName(channelName: String, ignoreCase: Boolean) = this.channels
    .asSequence()
    .filterIsInstance<GuildMessageChannel>().filter { it.name.equals(channelName, ignoreCase) }
    .firstOrNull()

fun Guild.getGuildMessageChannelById(channelId: String) = getGuildMessageChannelById(channelId.toLong())

fun Guild.getGuildMessageChannelById(channelId: Long) = this.getGuildChannelById(channelId) as? GuildMessageChannel