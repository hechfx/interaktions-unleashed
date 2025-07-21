package net.perfectdreams.interactions.commands.declarations.builders

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.perfectdreams.interactions.commands.InteraKTionsUnleashedDsl
import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString
import net.perfectdreams.interactions.commands.declarations.SlashCommandGroupDeclaration

@InteraKTionsUnleashedDsl
class SlashCommandGroupDeclarationBuilder(
    val name: LocalizedString,
    val description: LocalizedString,
    private val integrationTypes: List<IntegrationType>,
    private val interactionContexts: List<InteractionContextType>
) {
    // Groups can't have executors!
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    var alternativeLegacyLabels = mutableListOf<String>()

    fun subcommand(name: LocalizedString, description: LocalizedString, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SlashCommandDeclarationBuilder(
            name,
            description,
        ).apply {
            this.integrationTypes = this@SlashCommandGroupDeclarationBuilder.integrationTypes
            this.interactionContexts = this@SlashCommandGroupDeclarationBuilder.interactionContexts
        }.apply(block)
    }

    fun subcommand(name: String, description: String, block: SlashCommandDeclarationBuilder.() -> Unit) =
        subcommand(LocalizedString(name), LocalizedString(description), block)

    fun build(): SlashCommandGroupDeclaration {
        return SlashCommandGroupDeclaration(
            name,
            description,
            alternativeLegacyLabels,
            subcommands.map { it.build() }
        )
    }
}