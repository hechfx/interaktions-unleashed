package net.perfectdreams.interactions.commands.context

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.interactions.InteractionMessage
import net.perfectdreams.interactions.UnleashedCommandManager
import net.perfectdreams.interactions.UnleashedContext
import net.perfectdreams.interactions.UnleashedHook
import net.perfectdreams.interactions.UnleashedMentions
import net.perfectdreams.interactions.commands.declarations.SlashCommandDeclaration
import net.perfectdreams.interactions.commands.options.UserAndMember
import net.perfectdreams.interactions.events.MessageListener
import net.perfectdreams.interactions.extensions.referenceIfPossible
import net.perfectdreams.interactions.utils.ComponentV2MessageBuilder
import net.perfectdreams.interactions.utils.TextUtils.convertMarkdownLinksWithLabelsToPlainLinks
import net.perfectdreams.interactions.utils.TextUtils.extractUserFromString
import java.util.EnumSet

/**
 * A command context that provides compatibility with legacy message commands.
 *
 * Ephemeral message state is ignored when using it with normal non-interactions commands. Don't use it to show sensitive information!
 */
class LegacyMessageCommandContext(
    val guildLocale: DiscordLocale?,
    val userLocale: DiscordLocale,
    val event: MessageListener.MessageEvent,
    val args: List<String>,
    override val rootDeclaration: SlashCommandDeclaration,
    override val commandDeclaration: SlashCommandDeclaration,
    manager: UnleashedCommandManager
) : UnleashedContext(
    discordGuildLocale = guildLocale,
    discordUserLocale = userLocale,
    jda = event.jda,
    mentions = UnleashedMentions(
        event.message.mentions.users,
        event.message.mentions.channels,
        event.message.mentions.customEmojis,
        event.message.mentions.roles
    ),
    user = event.author,
    memberOrNull = event.member,
    guildOrNull = event.guild,
    channelOrNull = event.channel,
    manager = manager,
    discordInteractionOrNull = null
), CommandContext {
    override suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook.LegacyMessageHook {
        // Message commands do not have deferring like slash commands...
        // But instead of doing just a noop, we can get cheeky with it, heh
        event.channel.sendTyping().queue()

        return UnleashedHook.LegacyMessageHook()
    }

    override suspend fun reply(
        ephemeral: Boolean,
        builder: suspend InlineMessage<MessageCreateData>.() -> Unit
    ): InteractionMessage {
        val inlineBuilder = MessageCreate {
            // Don't let ANY mention through, you can still override the mentions in the builder
            allowedMentionTypes = EnumSet.of(
                Message.MentionType.CHANNEL,
                Message.MentionType.EMOJI,
                Message.MentionType.SLASH_COMMAND
            )

            // We need to do this because "builder" is suspendable, because we can't inline this function due to it being in an interface
            builder()

            // We are going to replace any links with labels with just links, since Discord does not support labels with links if it isn't a webhook or an interaction
            content = content?.convertMarkdownLinksWithLabelsToPlainLinks()
        }

        // This isn't a real follow-up interaction message, but we do have the message data, so that's why we are using it
        return InteractionMessage.FollowUpInteractionMessage(
            event.channel.sendMessage(inlineBuilder)
                .referenceIfPossible(event.message)
                .failOnInvalidReply(false)
                .await()
        )
    }

    override suspend fun replyV2(
        ephemeral: Boolean,
        componentBuilder: suspend ComponentV2MessageBuilder.() -> Unit
    ): InteractionMessage {
        val components = ComponentV2MessageBuilder().apply {
            componentBuilder()
        }.childs

        // This isn't a real follow-up interaction message, but we do have the message data, so that's why we are using it
        return InteractionMessage.FollowUpInteractionMessage(
            event.channel.sendMessage("")
                .referenceIfPossible(event.message)
                .failOnInvalidReply(false)
                .useComponentsV2()
                .setComponents(components)
                .await()
        )
    }

    fun getImage(index: Int) = event.message.attachments.getOrNull(index)

    fun getEmoji(index: Int) = event.message.mentions.customEmojis.getOrNull(index)

    /**
     * Gets a [User] reference from the argument at the specified [index]
     */
    suspend fun getUser(index: Int): User? {
        val arg = args.getOrNull(index) ?: return null

        return extractUserFromString(
            arg,
            mentions.users,
            event.guild
        )
    }

    /**
     * Gets a [UserAndMember] reference from the argument at the specified [index]
     */
    suspend fun getUserAndMember(index: Int): UserAndMember? {
        val user = getUser(index) ?: return null

        val guild = event.guild

        val member = guild?.getMember(user)

        return UserAndMember(
            user,
            member
        )
    }
}