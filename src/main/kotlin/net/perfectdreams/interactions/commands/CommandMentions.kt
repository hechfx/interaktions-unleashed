package net.perfectdreams.interactions.commands

import net.dv8tion.jda.api.interactions.DiscordLocale

class CommandMentions(private val registeredCommands: List<DiscordCommand>) {
    /**
     * Creates a command mention of [path]. If the command doesn't exist, an error will be thrown.
     */
    private fun commandMention(path: String): String {
        // This seems weird, but hear me out:
        // In the past we did use the command label here, which is english
        // However, this is bad because all the strings are first created in portuguese, then translated to english
        // So a command that was not translated yet to english WILL cause issues after it is translated
        // (ESPECIALLY if it is being used as a command mention!)
        //
        // However, because we are relying on the portuguese version of the commands, we need to get the original canonical english name
        val commandParts = path.split(" ")
        when (commandParts.size) {
            1 -> {
                val rootLabel = commandParts[0]
                val registeredCommand = registeredCommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == rootLabel } ?: error("Couldn't find a command with label $rootLabel!")
                return "</$rootLabel:${registeredCommand.id}>"
            }
            2 -> {
                val rootLabel = commandParts[0]
                val subcommandLabel = commandParts[1]

                val registeredCommand = registeredCommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == rootLabel } ?: error("Couldn't find a command with label $rootLabel!")
                val subcommand = registeredCommand.subcommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == subcommandLabel } ?: error("Couldn't find a subcommand with label $subcommandLabel!")

                return "</${registeredCommand.name} ${subcommand.name}:${registeredCommand.id}>"
            }
            3 -> {
                val rootLabel = commandParts[0]
                val subcommandGroupLabel = commandParts[1]
                val subcommandLabel = commandParts[2]

                val registeredCommand = registeredCommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == rootLabel } ?: error("Couldn't find a command with label $rootLabel!")
                val subcommandGroup = registeredCommand.subcommandGroups.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == subcommandGroupLabel } ?: error("Couldn't find a subcommand group with label $subcommandGroupLabel!")
                val subcommand = subcommandGroup.subcommands.firstOrNull { it.nameLocalizations[DiscordLocale.PORTUGUESE_BRAZILIAN] == subcommandLabel } ?: error("Couldn't find a subcommand with label $subcommandLabel!")

                return "</${registeredCommand.name} ${subcommandGroup.name} ${subcommand.name}:${registeredCommand.id}>"
            }
            else -> error("Unsupported command format for $path, there are ${commandParts.size} parts, only 1, 2 or 3 are supported!")
        }
    }
}