package net.perfectdreams.interactions.commands.context

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.perfectdreams.interactions.InteractionContext
import net.perfectdreams.interactions.UnleashedMentions

/**
 * Context of the executed command
 */
class ApplicationCommandContext(
    val event: GenericCommandInteractionEvent
) : InteractionContext(
    UnleashedMentions(
        event.options.flatMap { it.mentions.users },
        event.options.flatMap { it.mentions.channels },
        event.options.flatMap { it.mentions.customEmojis },
        event.options.flatMap { it.mentions.roles }
    ),
    event
)