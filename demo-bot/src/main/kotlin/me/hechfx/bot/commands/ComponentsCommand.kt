package me.hechfx.bot.commands

import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.perfectdreams.interactions.UnleashedContext
import net.perfectdreams.interactions.commands.SlashCommandArguments
import net.perfectdreams.interactions.commands.declarations.builders.slashCommand
import net.perfectdreams.interactions.commands.declarations.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.interactions.commands.executors.LorittaSlashCommandExecutor
import net.perfectdreams.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.interactions.modals.options.modalString

class ComponentsCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("components", "show off components yay") {
        executor = ComponentsExecutor()
    }

    inner class ComponentsExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val type = string("type", "component type") {
                choice("buttons components", "buttons")
                choice("string select menu", "string-select-menu")
                choice("entity select menu", "entity-select-menu")
                choice("modals", "modal")
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val option = args[options.type]

            when (option) {
                "buttons" -> {
                    context.reply(false) {
                        content = "wow a button"

                        context.actionRow {
                            buttonForUser(context.user.idLong, false, ButtonStyle.PRIMARY, "Button") {
                                it.reply(false) {
                                    content = "You pressed the button!"
                                }
                            }
                        }
                    }
                }

                "string-select-menu" -> {
                    context.reply(false) {
                        content = "wow a string select menu"

                        context.actionRow {
                            stringSelectMenuForUser(context.user.idLong, false, designBuilder = {
                                addOption("foo", "bar")
                                addOption("oop", "poo")
                            }) { context, strings ->
                                context.reply(false) {
                                    content = "you've chosen ${strings}"
                                }
                            }
                        }
                    }
                }

                "entity-select-menu" -> {
                    context.reply(false) {
                        content = "wow an entity select menu"

                        context.actionRow {
                            entitySelectMenuForUser(context.user.idLong, false, {
                                this.setEntityTypes(EntitySelectMenu.SelectTarget.USER)
                            }) { context, mentionables ->
                                context.reply(false) {
                                    content = "you've chosen $mentionables"
                                }
                            }
                        }
                    }
                }

                "modal" -> {
                    context.reply(false) {
                        content = "if you click on the button below, a modal will appear"

                        context.actionRow {
                            buttonForUser(context.user.idLong, false, ButtonStyle.PRIMARY, "show modal") {
                                val textArea = modalString("secret", TextInputStyle.SHORT)

                                it.sendModal("send me your secret, i won't tell anyone...", listOf(textArea.unleash())) { it, args ->
                                    it.reply(false) {
                                        content = "hey everyone!!! the secret of ${context.user.asMention} is \"${args[textArea]}\""
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}