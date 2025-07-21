package net.perfectdreams.interactions.commands.declarations.builders

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.interactions.commands.InteraKTionsUnleashedDsl
import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString
import net.perfectdreams.interactions.commands.declarations.SlashCommandDeclaration
import net.perfectdreams.interactions.commands.executors.LorittaSlashCommandExecutor

// ===[ SLASH COMMANDS ]===
fun slashCommand(name: LocalizedString, description: LocalizedString, block: SlashCommandDeclarationBuilder.() -> (Unit)) = SlashCommandDeclarationBuilder(
    name,
    description,
).apply(block)

fun slashCommand(name: String, description: String, block: SlashCommandDeclarationBuilder.() -> Unit) =
    slashCommand(LocalizedString(name), LocalizedString(description), block)

@InteraKTionsUnleashedDsl
class SlashCommandDeclarationBuilder(
    val name: LocalizedString,
    val description: LocalizedString,
) {
    var examples: List<String>? = null
    var executor: LorittaSlashCommandExecutor? = null
    var botPermissions: Set<Permission>? = null
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var isGuildOnly = false
    var enableLegacyMessageSupport = false
    var alternativeLegacyLabels = mutableListOf<String>()
    var alternativeLegacyAbsoluteCommandPaths = mutableListOf<String>()
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<SlashCommandGroupDeclarationBuilder>()
    var integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

    fun subcommand(name: LocalizedString, description: LocalizedString, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands.add(
            SlashCommandDeclarationBuilder(
                name,
                description,
            ).apply {
                this.integrationTypes = this@SlashCommandDeclarationBuilder.integrationTypes
                this.interactionContexts = this@SlashCommandDeclarationBuilder.interactionContexts
            }.apply(block)
        )
    }

    fun subcommand(name: String, description: String, block: SlashCommandDeclarationBuilder.() -> (Unit)) =
        subcommand(LocalizedString(name), LocalizedString(description), block)

    fun subcommandGroup(name: LocalizedString, description: LocalizedString, block: SlashCommandGroupDeclarationBuilder.() -> (Unit)) {
        subcommandGroups.add(
            SlashCommandGroupDeclarationBuilder(
                name,
                description,
                integrationTypes,
                interactionContexts
            ).apply(block)
        )
    }

    fun subcommandGroup(name: String, description: String, block: SlashCommandGroupDeclarationBuilder.() -> Unit) =
        subcommandGroup(LocalizedString(name), LocalizedString(description), block)

    fun build(): SlashCommandDeclaration {
        return SlashCommandDeclaration(
            name,
            description,
            examples,
            botPermissions ?: emptySet(),
            defaultMemberPermissions,
            isGuildOnly,
            enableLegacyMessageSupport,
            alternativeLegacyLabels,
            alternativeLegacyAbsoluteCommandPaths,
            integrationTypes,
            interactionContexts,
            executor,
            subcommands.map { it.build() },
            subcommandGroups.map { it.build() }
        )
    }
}