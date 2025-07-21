package net.perfectdreams.interactions.commands.declarations

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString
import net.perfectdreams.interactions.commands.executors.LorittaUserCommandExecutor

data class UserCommandDeclaration(
    override val name: LocalizedString,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    override val integrationTypes: List<IntegrationType>,
    override val interactionContexts: List<InteractionContextType>,
    val executor: LorittaUserCommandExecutor?
) : ExecutableApplicationCommandDeclaration()