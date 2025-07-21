package me.hechfx.bot.commands.localization

import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString

object CommandTranslations {
    object Ping {
        val name = LocalizedString("ping", "commands.command.ping.name")
        val description = LocalizedString("ping command description", "commands.command.ping.description")

        object Options {
            object Target {
                val name = LocalizedString("something", "commands.command.ping.options.target.name")
                val description = LocalizedString("the bot will ping something!", "commands.command.ping.options.target.description")
            }
        }
    }
}