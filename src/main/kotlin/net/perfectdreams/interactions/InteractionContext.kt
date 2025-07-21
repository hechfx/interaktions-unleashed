package net.perfectdreams.interactions

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.interactions.utils.ComponentV2MessageBuilder
import java.util.EnumSet

abstract class InteractionContext(
    mentions: UnleashedMentions,
    private val replyCallback: IReplyCallback
) : UnleashedContext(
    if (replyCallback.isFromGuild) replyCallback.guildLocale else null,
    replyCallback.userLocale,
    replyCallback.jda,
    mentions,
    replyCallback.user,
    replyCallback.member,
    replyCallback.guild,
    replyCallback.messageChannel,
    replyCallback.hook.interaction
) {
    override suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook.InteractionHook {
        val realEphemeralState = if (alwaysEphemeral) true else ephemeral

        val hook = replyCallback.deferReply().setEphemeral(realEphemeralState).await()
        wasInitiallyDeferredEphemerally = realEphemeralState
        return UnleashedHook.InteractionHook(hook)
    }

    override suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit): InteractionMessage {
        val realEphemeralState = if (alwaysEphemeral) true else ephemeral

        val createdMessage = InlineMessage(MessageCreateBuilder()).apply {
            // Don't let ANY mention through, you can still override the mentions in the builder
            allowedMentionTypes = EnumSet.of(
                Message.MentionType.CHANNEL,
                Message.MentionType.EMOJI,
                Message.MentionType.SLASH_COMMAND
            )

            builder()
        }.build()

        // We could actually disable the components when their state expires, however this is hard to track due to "@original" or ephemeral messages not having an ID associated with it
        // So, if the message is edited, we don't know if we *can* disable the components when their state expires!
        return if (replyCallback.isAcknowledged) {
            val message = replyCallback.hook.sendMessage(createdMessage).setEphemeral(realEphemeralState).await()
            InteractionMessage.FollowUpInteractionMessage(message)
        } else {
            val hook = replyCallback.reply(createdMessage).setEphemeral(realEphemeralState).await()
            wasInitiallyDeferredEphemerally = realEphemeralState
            InteractionMessage.InitialInteractionMessage(hook)
        }
    }

    override suspend fun replyV2(
        ephemeral: Boolean,
        componentBuilder: suspend ComponentV2MessageBuilder.() -> Unit
    ): InteractionMessage {
        val realEphemeralState = if (alwaysEphemeral) true else ephemeral

        val components = ComponentV2MessageBuilder().apply {
            componentBuilder()
        }.childs

        println(components)

        // We could actually disable the components when their state expires, however this is hard to track due to "@original" or ephemeral messages not having an ID associated with it
        // So, if the message is edited, we don't know if we *can* disable the components when their state expires!
        return if (replyCallback.isAcknowledged) {
            val message = replyCallback.hook.sendMessage("")
                .setEphemeral(realEphemeralState)
                .useComponentsV2()
                .setComponents(components)
                .await()
            InteractionMessage.FollowUpInteractionMessage(message)
        } else {
            val hook = replyCallback.reply("")
                .setEphemeral(realEphemeralState)
                .useComponentsV2()
                .setComponents(components)
                .await()
            wasInitiallyDeferredEphemerally = realEphemeralState
            InteractionMessage.InitialInteractionMessage(hook)
        }
    }
}
