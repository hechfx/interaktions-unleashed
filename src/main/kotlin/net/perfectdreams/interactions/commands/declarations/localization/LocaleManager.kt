package net.perfectdreams.interactions.commands.declarations.localization

import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.perfectdreams.interactions.UnleashedCommandManager

class LocaleManager(var path: String, val manager: UnleashedCommandManager) {
    private val cache = mutableMapOf<DiscordLocale, Map<String, String>>()

    operator fun get(locale: DiscordLocale, str: LocalizedString): String {
        val data = cache.getOrPut(locale) {
            loadLocale(locale)
        }

        return data[str.key] ?: str.value
    }

    fun getAll(key: LocalizedString): Map<DiscordLocale, String> {
        return manager.options.supportedLocales.associateWith { get(it, key) }
    }

    private fun loadLocale(locale: DiscordLocale): Map<String, String> {
        val path = "$path/${locale.locale}.json"

        val stream = LocaleManager::class.java.getResourceAsStream(path)
            ?: return emptyMap()

        val text = stream.bufferedReader().use { it.readText() }
        return Json.decodeFromString(text)
    }
}
