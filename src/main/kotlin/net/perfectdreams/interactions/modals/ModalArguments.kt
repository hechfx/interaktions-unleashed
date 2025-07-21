package net.perfectdreams.interactions.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.perfectdreams.interactions.modals.options.DiscordModalOptionReference
import net.perfectdreams.interactions.modals.options.ModalOptionReference

class ModalArguments(private val event: ModalInteractionEvent) {
    operator fun <T> get(argument: ModalOptionReference<T>): T {
        return when (argument) {
            is DiscordModalOptionReference -> {
                val option = event.getValue(argument.name)

                if (option == null) {
                    if (argument.required)
                        throw RuntimeException("Missing argument ${argument.name}!")

                    return null as T
                }

                return argument.get(option)
            }
        }
    }
}