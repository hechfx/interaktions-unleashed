package net.perfectdreams.interactions.commands.declarations

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString

sealed class ExecutableApplicationCommandDeclaration {
    abstract val name: LocalizedString
    abstract val integrationTypes: List<IntegrationType>
    abstract val interactionContexts: List<InteractionContextType>
}