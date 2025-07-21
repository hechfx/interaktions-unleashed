package net.perfectdreams.interactions.commands.declarations.wrappers

import net.perfectdreams.interactions.commands.declarations.builders.SlashCommandDeclarationBuilder

interface SlashCommandDeclarationWrapper {
    fun command(): SlashCommandDeclarationBuilder
}
