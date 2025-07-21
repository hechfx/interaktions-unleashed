package me.hechfx.bot.commands

import net.perfectdreams.interactions.UnleashedContext
import net.perfectdreams.interactions.commands.SlashCommandArguments
import net.perfectdreams.interactions.commands.declarations.builders.slashCommand
import net.perfectdreams.interactions.commands.declarations.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.interactions.commands.executors.LorittaSlashCommandExecutor

/**
 * Example of a command without localization whatsoever
 */
class PongCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("pong", "pong command yay") {
        executor = PongExecutor()
    }

    inner class PongExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                content = "ping!"
            }
        }
    }
}