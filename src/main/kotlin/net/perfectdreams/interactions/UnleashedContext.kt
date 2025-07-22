package net.perfectdreams.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.interactions.commands.exceptions.CommandException
import net.perfectdreams.interactions.utils.ActionRowBuilder
import net.perfectdreams.interactions.utils.ComponentV2MessageBuilder

abstract class UnleashedContext(
    val discordGuildLocale: DiscordLocale?,
    val discordUserLocale: DiscordLocale,
    val manager: UnleashedCommandManager,
    val jda: JDA,
    val mentions: UnleashedMentions,
    val user: User,
    val memberOrNull: Member?,
    val guildOrNull: Guild?,
    val channelOrNull: MessageChannel?,
    val discordInteractionOrNull: Interaction?
) {
    var alwaysEphemeral = false
    var wasInitiallyDeferredEphemerally: Boolean? = null

    val guildId
        get() = guildOrNull?.idLong

    val guild: Guild
        get() = guildOrNull ?: error("This interaction was not sent in a guild!")

    val member: Member
        get() = memberOrNull ?: error("This interaction was not sent in a guild!")

    val channel: MessageChannel
        get() = channelOrNull ?: error("This interaction was not sent in a message channel!")

    val discordInteraction: Interaction
        get() = discordInteractionOrNull ?: error("This is not executed by an interaction!")

    val actionRows = mutableListOf<ActionRow>()

    fun actionRow(builder: ActionRowBuilder.() -> Unit) {
        val b = ActionRowBuilder(manager).apply(builder).build()

        actionRows.add(b)
    }

    abstract suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook

    abstract suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit = {}): InteractionMessage

    abstract suspend fun replyV2(ephemeral: Boolean, componentBuilder: suspend ComponentV2MessageBuilder.() -> Unit = {}): InteractionMessage

    /**
     * Throws a [CommandException] with a specific message [block], halting command execution
     *
     * @param reply the message that will be sent
     * @see fail
     * @see CommandException
     */
    fun fail(ephemeral: Boolean, builder: InlineMessage<*>.() -> Unit = {}): Nothing = throw CommandException(ephemeral) {
        builder()
    }
}