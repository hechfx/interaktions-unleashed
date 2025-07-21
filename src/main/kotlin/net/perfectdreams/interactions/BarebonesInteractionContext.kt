package net.perfectdreams.interactions

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreateBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook

class BarebonesInteractionContext(
    val jda: JDA,
    val token: String
) {
    val hook = InteractionHook.from(jda, token)

    suspend fun reply(ephemeral: Boolean, block: suspend InlineMessage<*>.() -> Unit): Message? {
        val builtMessage = MessageCreateBuilder {
            block.invoke(this)
        }.build()

        return hook.setEphemeral(ephemeral)
            .sendMessage(builtMessage)
            .await()
    }
}