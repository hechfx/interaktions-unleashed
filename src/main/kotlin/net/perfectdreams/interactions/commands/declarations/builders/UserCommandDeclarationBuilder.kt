package net.perfectdreams.interactions.commands.declarations.builders

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.interactions.commands.InteraKTionsUnleashedDsl
import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString
import net.perfectdreams.interactions.commands.declarations.UserCommandDeclaration
import net.perfectdreams.interactions.commands.executors.LorittaUserCommandExecutor

// ===[ USER COMMANDS ]===
fun userCommand(name: LocalizedString, executor: LorittaUserCommandExecutor, block: UserCommandDeclarationBuilder.() -> (Unit) = {}) =
    UserCommandDeclarationBuilder(name,  executor).apply(block)

fun userCommand(name: String, executor: LorittaUserCommandExecutor, block: UserCommandDeclarationBuilder.() -> Unit) =
    userCommand(LocalizedString(name), executor, block)

@InteraKTionsUnleashedDsl
class UserCommandDeclarationBuilder(
    val name: LocalizedString,
    val executor: LorittaUserCommandExecutor,
) {
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var isGuildOnly = false
    var integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

    fun build(): UserCommandDeclaration {
        return UserCommandDeclaration(
            name,
            defaultMemberPermissions,
            isGuildOnly,
            integrationTypes,
            interactionContexts,
            executor
        )
    }
}