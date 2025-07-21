package net.perfectdreams.interactions

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageEditData

abstract class UnleashedHook {
    class InteractionHook(val jdaHook: net.dv8tion.jda.api.interactions.InteractionHook) : UnleashedHook() {
        suspend fun editOriginal(message: MessageEditData): Message {
            return jdaHook.editOriginal(message).await()
        }

        suspend fun editOriginal(message: InlineMessage<MessageEditData>.() -> (Unit)): Message = editOriginal(
            MessageEdit {
                message()
            }
        )
    }

    class LegacyMessageHook : UnleashedHook()
}