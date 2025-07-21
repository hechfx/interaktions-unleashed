package net.perfectdreams.interactions.commands.declarations

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString
import net.perfectdreams.interactions.commands.executors.LorittaSlashCommandExecutor

data class SlashCommandDeclaration(
    override val name: LocalizedString,
    val description: LocalizedString,
    val examples: List<String>?,
    val botPermissions: Set<Permission>,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    var enableLegacyMessageSupport: Boolean,
    var alternativeLegacyLabels: List<String>,
    var alternativeLegacyAbsoluteCommandPaths: List<String>,
    override val integrationTypes: List<IntegrationType>,
    override val interactionContexts: List<InteractionContextType>,
    val executor: LorittaSlashCommandExecutor?,
    val subcommands: List<SlashCommandDeclaration>,
    val subcommandGroups: List<SlashCommandGroupDeclaration>
) : ExecutableApplicationCommandDeclaration()