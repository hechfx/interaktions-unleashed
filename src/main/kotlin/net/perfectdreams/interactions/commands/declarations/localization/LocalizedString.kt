package net.perfectdreams.interactions.commands.declarations.localization

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.perfectdreams.interactions.UnleashedCommandManager.Companion.localeManager

data class LocalizedString(
    val value: String,
    val key: String? = null,
    val locale: DiscordLocale = DiscordLocale.ENGLISH_US,
) {
    fun transform(): String = localeManager[locale, this]
    override fun toString(): String = transform()
}