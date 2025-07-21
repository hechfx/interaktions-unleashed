package net.perfectdreams.interactions.events

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.interactions.UnleashedCommandManager
import net.perfectdreams.interactions.commands.SlashCommandArguments
import net.perfectdreams.interactions.commands.SlashCommandArgumentsSource
import net.perfectdreams.interactions.commands.context.ApplicationCommandContext
import net.perfectdreams.interactions.commands.exceptions.CommandException
import net.perfectdreams.interactions.commands.exceptions.SimpleCommandException
import net.perfectdreams.interactions.utils.launchMessageJob

class InteractionsListener(val manager: UnleashedCommandManager) : ListenerAdapter() {
    init {
        manager.jda.addEventListener(this)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = launchMessageJob(event) {
        val slashSearchResult = manager.findSlashCommandByFullCommandName(event.fullCommandName) ?: error("Unknown Slash Command! Are you sure it is registered? ${event.name}")
        val slashDeclaration = slashSearchResult.slashDeclaration
        val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")

        var context: ApplicationCommandContext? = null

        try {
            val guild = event.guild

            val args = SlashCommandArguments(SlashCommandArgumentsSource.SlashCommandArgumentsEventSource(event))
            context = ApplicationCommandContext(event)

            if (guild != null) {
                // Check if Loritta has all the necessary permissions
                val missingPermissions = slashDeclaration.botPermissions.filterNot { guild.selfMember.hasPermission(event.guildChannel, it) }

                if (missingPermissions.isNotEmpty()) {
                    // oh no
                    throw SimpleCommandException("The application doesn't have permissions to execute this command! (${missingPermissions.joinToString(", ")})")
                }
            }

            executor.execute(
                context,
                args
            )
        } catch (e: CommandException) {
            context?.reply(e.ephemeral, e.builder)
        }
    }

    override fun onUserContextInteraction(event: UserContextInteractionEvent) = launchMessageJob(event) {
        val targetDeclaration = manager.findUserCommandByFullCommandName(event.fullCommandName) ?: error("Unknown Message Command! Are you sure it is registered? ${event.name}")
        val slashDeclaration = targetDeclaration
        val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")

        var context: ApplicationCommandContext? = null

        try {
            context = ApplicationCommandContext(event)

            executor.execute(context, event.target)
        } catch (e: CommandException) {
            context?.reply(e.ephemeral, e.builder)
        }
    }
}