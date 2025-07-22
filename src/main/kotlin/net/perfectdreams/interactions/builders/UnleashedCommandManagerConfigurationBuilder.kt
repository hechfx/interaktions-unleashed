package net.perfectdreams.interactions.builders

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageCreateData

class UnleashedCommandManagerConfiguration(
    val useLocalization: Boolean,
    val registerCommandGlobally: Boolean,
    val guildsToRegisterCommands: Set<Long>,
    val supportedLocales: Set<DiscordLocale>,
    val localePath: String,
    val prefix: String,
    val mentionMessage: MessageCreateData?,
    val expiredComponentMessage: MessageCreateData?
)

class UnleashedCommandManagerConfigurationBuilder {
    private var useLocalization = false
    private var registerCommandGlobally = false
    private var localePath = ""
    var prefix = "+"

    // Set to avoid duplicated elements.
    private val guildsToRegisterCommands = mutableSetOf<Long>()
    private val supportedLocales = mutableSetOf<DiscordLocale>()
    private var mentionMessage: MessageCreateData? = null
    private var expiredComponentMessage: MessageCreateData? = null

    fun enableLocale(path: String) {
        if (useLocalization)
            error("Localization is already active!")

        useLocalization = true
        localePath = path
    }

    fun forGuild(guildId: Long) {
        guildsToRegisterCommands.add(guildId)
    }

    fun forGuilds(vararg guildId: Long) {
        guildsToRegisterCommands.addAll(guildId.toList())
    }

    fun supportLocale(locale: DiscordLocale) {
        supportedLocales.add(locale)
    }

    fun supportLocales(vararg locale: DiscordLocale) {
        supportedLocales.addAll(locale.toList())
    }

    fun setMentionMessage(block: InlineMessage<*>.() -> Unit) {
        val builtMessage = MessageCreate {
            block()
        }

        mentionMessage = builtMessage
    }

    fun expiredComponentMessage(builder: InlineMessage<*>.() -> Unit) {
        val builtMessage = MessageCreate {
            builder()
        }

        expiredComponentMessage = builtMessage
    }

    fun build() = UnleashedCommandManagerConfiguration(
        useLocalization,
        registerCommandGlobally,
        guildsToRegisterCommands,
        supportedLocales,
        localePath,
        prefix,
        mentionMessage,
        expiredComponentMessage
    )
}