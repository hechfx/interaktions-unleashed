package net.perfectdreams.interactions.modals

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.modals.ModalInteraction
import net.perfectdreams.interactions.InteractionContext
import net.perfectdreams.interactions.UnleashedCommandManager
import net.perfectdreams.interactions.UnleashedHook
import net.perfectdreams.interactions.UnleashedMentions

/**
 * Context of the executed command
 */
class ModalContext(
    val event: ModalInteraction,
    manager: UnleashedCommandManager
) : InteractionContext(UnleashedMentions(emptyList(), emptyList(), emptyList(), emptyList()), manager, event) {
    suspend fun deferEdit() = UnleashedHook.InteractionHook(event.deferEdit().await())
}