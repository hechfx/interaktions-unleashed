package net.perfectdreams.interactions.commands.executors

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.interactions.commands.context.ApplicationCommandContext

abstract class LorittaUserCommandExecutor {
    abstract suspend fun execute(context: ApplicationCommandContext, user: User)
}