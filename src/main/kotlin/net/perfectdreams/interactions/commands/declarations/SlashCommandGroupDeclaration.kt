package net.perfectdreams.interactions.commands.declarations

import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString

data class SlashCommandGroupDeclaration(
    val name: LocalizedString,
    val description: LocalizedString,
    var alternativeLegacyLabels: List<String>,
    val subcommands: List<SlashCommandDeclaration>
)