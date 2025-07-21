package net.perfectdreams.interactions.commands.executors

import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.interactions.commands.context.ApplicationCommandContext

abstract class LorittaMessageCommandExecutor {
    abstract suspend fun execute(context: ApplicationCommandContext, message: Message)
}