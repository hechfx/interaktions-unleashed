package net.perfectdreams.interactions.commands.declarations.builders

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.interactions.commands.InteraKTionsUnleashedDsl
import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString
import net.perfectdreams.interactions.commands.declarations.MessageCommandDeclaration
import net.perfectdreams.interactions.commands.executors.LorittaMessageCommandExecutor

// ===[ MESSAGE COMMANDS ]===
fun messageCommand(name: LocalizedString, executor: LorittaMessageCommandExecutor, block: MessageCommandDeclarationBuilder.() -> (Unit) = {}) =
    MessageCommandDeclarationBuilder(name, executor).apply(block)

fun messageCommand(name: String, executor: LorittaMessageCommandExecutor, block: MessageCommandDeclarationBuilder.() -> (Unit) = {}) =
    messageCommand(LocalizedString(name), executor, block)

@InteraKTionsUnleashedDsl
class MessageCommandDeclarationBuilder(
    val name: LocalizedString,
    val executor: LorittaMessageCommandExecutor
) {
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var isGuildOnly = false
    var integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

    fun build(): MessageCommandDeclaration {
        return MessageCommandDeclaration(
            name,
            defaultMemberPermissions,
            isGuildOnly,
            integrationTypes,
            interactionContexts,
            executor
        )
    }
}