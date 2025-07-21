package me.hechfx.bot.commands

import me.hechfx.bot.commands.localization.CommandTranslations
import net.perfectdreams.interactions.UnleashedContext
import net.perfectdreams.interactions.commands.SlashCommandArguments
import net.perfectdreams.interactions.commands.context.LegacyMessageCommandContext
import net.perfectdreams.interactions.commands.declarations.builders.slashCommand
import net.perfectdreams.interactions.commands.declarations.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.interactions.commands.executors.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.interactions.commands.executors.LorittaSlashCommandExecutor
import net.perfectdreams.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.interactions.commands.options.OptionReference

/**
 * Example of a command with localization and components v2
 */
class PingCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(
        CommandTranslations.Ping.name,
        CommandTranslations.Ping.description
    ) {
        enableLegacyMessageSupport = true

        executor = PingExecutor()
    }

    inner class PingExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val target = optionalString("target", CommandTranslations.Ping.Options.Target.description)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val target = args[options.target] ?: ""

            context.reply(false) {
                content = "Pong! $target"
            }

            context.replyV2(false) {
                container {
                    section {
                        thumbnail(context.user.effectiveAvatarUrl)

                        text {
                            append("You suck!")
                        }
                    }

                    separator(false)

                    section {
                        thumbnail(context.jda.selfUser.effectiveAvatarUrl)

                        text {
                            append("I'm incredible!")
                        }
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return mapOf(
                options.target to args.joinToString(" ")
            )
        }
    }
}