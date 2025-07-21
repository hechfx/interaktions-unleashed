package net.perfectdreams.interactions.commands

import net.perfectdreams.interactions.commands.options.OptionReference

class SlashCommandArguments(private val event: SlashCommandArgumentsSource) {
    operator fun <T> get(argument: OptionReference<T>) = event[argument]
}