package net.perfectdreams.interactions.commands.declarations.wrappers

import net.perfectdreams.interactions.commands.declarations.builders.UserCommandDeclarationBuilder

interface UserCommandDeclarationWrapper : ApplicationCommandDeclarationWrapper {
    fun command(): UserCommandDeclarationBuilder
}