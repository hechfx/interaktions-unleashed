package net.perfectdreams.interactions.commands.context

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.interactions.InteractionMessage
import net.perfectdreams.interactions.commands.declarations.SlashCommandDeclaration

interface CommandContext {
    val jda: JDA
    val user: User
    val rootDeclaration: SlashCommandDeclaration
    val commandDeclaration: SlashCommandDeclaration

    suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit): InteractionMessage
}