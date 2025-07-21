package net.perfectdreams.interactions.commands.declarations.wrappers

import net.perfectdreams.interactions.commands.declarations.builders.MessageCommandDeclarationBuilder

interface MessageCommandDeclarationWrapper : ApplicationCommandDeclarationWrapper {
    fun command(): MessageCommandDeclarationBuilder
}