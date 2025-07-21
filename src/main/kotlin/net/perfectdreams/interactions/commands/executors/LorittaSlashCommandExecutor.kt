package net.perfectdreams.interactions.commands.executors

import net.perfectdreams.interactions.UnleashedContext
import net.perfectdreams.interactions.commands.SlashCommandArguments
import net.perfectdreams.interactions.commands.options.ApplicationCommandOptions

abstract class LorittaSlashCommandExecutor {
    open val options: ApplicationCommandOptions = ApplicationCommandOptions.NO_OPTIONS

    abstract suspend fun execute(context: UnleashedContext, args: SlashCommandArguments)
}