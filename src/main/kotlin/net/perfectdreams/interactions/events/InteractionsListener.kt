package net.perfectdreams.interactions.events

import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.interactions.UnleashedCommandManager
import net.perfectdreams.interactions.commands.SlashCommandArguments
import net.perfectdreams.interactions.commands.SlashCommandArgumentsSource
import net.perfectdreams.interactions.commands.autocomplete.AutocompleteContext
import net.perfectdreams.interactions.commands.autocomplete.AutocompleteExecutor
import net.perfectdreams.interactions.commands.context.ApplicationCommandContext
import net.perfectdreams.interactions.commands.exceptions.CommandException
import net.perfectdreams.interactions.commands.exceptions.SimpleCommandException
import net.perfectdreams.interactions.commands.options.LongDiscordOptionReference
import net.perfectdreams.interactions.commands.options.NumberDiscordOptionReference
import net.perfectdreams.interactions.commands.options.StringDiscordOptionReference
import net.perfectdreams.interactions.components.ComponentContext
import net.perfectdreams.interactions.components.UnleashedComponentId
import net.perfectdreams.interactions.modals.ModalArguments
import net.perfectdreams.interactions.modals.ModalContext
import net.perfectdreams.interactions.utils.launchMessageJob

class InteractionsListener(val manager: UnleashedCommandManager) : ListenerAdapter() {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

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
            context = ApplicationCommandContext(event, manager)

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
            context = ApplicationCommandContext(event, manager)

            executor.execute(context, event.target)
        } catch (e: CommandException) {
            context?.reply(e.ephemeral, e.builder)
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        GlobalScope.launch {
            val componentId = try {
                UnleashedComponentId(event.componentId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            var context: ComponentContext? = null

            try {
                val callbackData = manager.interactivityManager.buttonInteractionCallbacks[componentId.uniqueId]

                context = ComponentContext(event, manager)

                if (callbackData == null) {
                    return@launch if (manager.options.expiredComponentMessage != null) {
                        event.reply(manager.options.expiredComponentMessage)
                            .setEphemeral(true)
                            .queue()
                    } else {
                        throw SimpleCommandException("Couldn't handle expired or non existent callback components!")
                    }
                }

                context.alwaysEphemeral = callbackData.alwaysEphemeral

                callbackData.callback.invoke(context)
            } catch (e: SimpleCommandException) {
                logger.error(e) { e.reason }
            } catch (e: Exception) {
                throw e // propagate
            }
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        GlobalScope.launch {
            val componentId = try {
                UnleashedComponentId(event.componentId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            var context: ComponentContext? = null

            try {
                val callbackData = manager.interactivityManager.selectMenuInteractionCallbacks[componentId.uniqueId]

                context = ComponentContext(event, manager)

                if (callbackData == null) {
                    return@launch if (manager.options.expiredComponentMessage != null) {
                        event.reply(manager.options.expiredComponentMessage)
                            .setEphemeral(true)
                            .queue()
                    } else {
                        throw SimpleCommandException("Couldn't handle expired or non existent callback components!")
                    }
                }

                context.alwaysEphemeral = callbackData.alwaysEphemeral

                callbackData.callback.invoke(context, event.interaction.values)
            } catch (e: SimpleCommandException) {
                logger.error(e) { e.reason }
            } catch (e: Exception) {
                throw e // propagate
            }
        }
    }

    override fun onEntitySelectInteraction(event: EntitySelectInteractionEvent) {
        GlobalScope.launch {
            val componentId = try {
                UnleashedComponentId(event.componentId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            var context: ComponentContext? = null

            try {
                val callbackData = manager.interactivityManager.selectMenuEntityInteractionCallbacks[componentId.uniqueId]

                context = ComponentContext(event, manager)

                if (callbackData == null) {
                    return@launch if (manager.options.expiredComponentMessage != null) {
                        event.reply(manager.options.expiredComponentMessage)
                            .setEphemeral(true)
                            .queue()
                    } else {
                        throw SimpleCommandException("Couldn't handle expired or non existent callback components!")
                    }
                }

                context.alwaysEphemeral = callbackData.alwaysEphemeral

                callbackData.callback.invoke(context, event.interaction.values)
            } catch (e: SimpleCommandException) {
                logger.error(e) { e.reason }
            } catch (e: Exception) {
                throw e // propagate
            }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        GlobalScope.launch {
            val componentId = try {
                UnleashedComponentId(event.modalId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            var context: ModalContext? = null

            try {
                val callbackData = manager.interactivityManager.modalCallbacks[componentId.uniqueId]

                context = ModalContext(event, manager)

                if (callbackData == null) {
                    return@launch if (manager.options.expiredComponentMessage != null) {
                        event.reply(manager.options.expiredComponentMessage)
                            .setEphemeral(true)
                            .queue()
                    } else {
                        throw SimpleCommandException("Couldn't handle expired or non existent callback components!")
                    }
                }

                context.alwaysEphemeral = callbackData.alwaysEphemeral

                callbackData.callback.invoke(context, ModalArguments(event))
            } catch (e: SimpleCommandException) {
                logger.error(e) { e.reason }
            } catch (e: Exception) {
                throw e // propagate
            }
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) = launchMessageJob(event) {
        val slashSearchResult = manager.findSlashCommandByFullCommandName(event.fullCommandName) ?: error("Unknown Slash Command! Are you sure it is registered? ${event.name}")

        val rootDeclaration = slashSearchResult.rootDeclaration
        val slashDeclaration = slashSearchResult.slashDeclaration

        // No executor, bail out!
        val executor = slashDeclaration.executor ?: return@launchMessageJob

        val autocompletingOption = executor.options.registeredOptions
            .firstOrNull {
                it.name == event.focusedOption.name
            } ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the option doesn't exist!")

        try {
            when (autocompletingOption) {
                is StringDiscordOptionReference -> {
                    val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<String>
                        ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                    val map = autocompleteCallback.execute(
                        AutocompleteContext(event)
                    ).map {
                        Choice(it.key, it.value)
                    }

                    event.replyChoices(map).await()
                }
                is LongDiscordOptionReference -> {
                    val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<Long>
                        ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                    val map = autocompleteCallback.execute(
                        AutocompleteContext(event)
                    ).map {
                        Choice(it.key, it.value)
                    }

                    event.replyChoices(map).await()
                }
                is NumberDiscordOptionReference -> {
                    val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<Double>
                        ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                    val map = autocompleteCallback.execute(
                        AutocompleteContext(
                            event
                        )
                    ).map {
                        Choice(it.key, it.value)
                    }

                    event.replyChoices(map).await()
                }
                else -> error("Unsupported option reference for autocomplete ${autocompletingOption::class.simpleName}")
            }
        } catch (e: SimpleCommandException) {
            logger.error(e) { e.reason }
        } catch (e: Exception) {
            throw e // propagate
        }
    }
}