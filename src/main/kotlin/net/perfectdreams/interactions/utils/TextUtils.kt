package net.perfectdreams.interactions.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import java.text.Normalizer

object TextUtils {
    // https://stackoverflow.com/a/60010299/7271796
    private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    private val snakeRegex = "_[a-zA-Z]".toRegex()
    private val kebabRegex = "-[a-zA-Z]".toRegex()
    private val markdownUrlsRegex = Regex("\\[(.+?)]\\((<?https?://.+?>?)\\)(!|\\.|\\.\\.\\.|\\?|;|:)?")

    // String extensions
    fun camelToSnakeCase(string: String) = camelRegex.replace(string) {
        "_${it.value}"
    }.lowercase()

    fun snakeToLowerCamelCase(string: String) = snakeRegex.replace(string) {
        it.value.replace("_","")
            .uppercase()
    }

    fun kebabToLowerCamelCase(string: String) = kebabRegex.replace(string) {
        it.value.replace("-","")
            .uppercase()
    }

    fun snakeToUpperCamelCase(string: String) = snakeToLowerCamelCase(string).capitalize()

    /**
     * Shortens [this] to [maxLength], if the text would overflow, [suffix] is appended to the end of the string
     */
    fun String.shortenWithEllipsis(maxLength: Int, suffix: String = "..."): String {
        if (this.length >= maxLength)
            return this.take(maxLength - suffix.length) + suffix
        return this
    }

    /**
     * Strips code backticks from [this] and then [shortenWithEllipsis]
     */
    fun String.shortenAndStripCodeBackticks(maxLength: Int, suffix: String = "..."): String =
        this.stripCodeBackticks().shortenWithEllipsis(maxLength, suffix)

    /**
     * Strips code backticks from [this]
     */
    fun String.stripCodeBackticks(): String {
        return this.replace("`", "")
    }

    /**
     * Strips new lines from [this]
     */
    fun String.stripNewLines() = this.replace(Regex("[\\r\\n]"), "")

    /**
     * Converts Markdown links with label such as "[link here](https://loritta.website/)" to "link here https://loritta.website/"
     *
     * Links that have punctuation after the link, such as "[link here](https://loritta.website/)!" are converted to "link here! https://loritta.website/"
     *
     * Useful to convert messages to something that can be sent within bot messages that aren't within a webhook/interaction!
     */
    fun String.convertMarkdownLinksWithLabelsToPlainLinks(): String {
        return this.replace(markdownUrlsRegex) {
            val punctuation = it.groupValues[3]
            if (punctuation.isNotEmpty()) {
                "${it.groupValues[1]}${it.groupValues[3]} ${it.groupValues[2]}"
            } else {
                "${it.groupValues[1]} ${it.groupValues[2]}"
            }
        }
    }

    fun extractUserFromString(
        input: String,
        usersInContext: List<User>? = null,
        guild: Guild? = null,
        extractUserViaMention: Boolean = true,
        extractUserViaNameAndDiscriminator: Boolean = true,
        extractUserViaEffectiveName: Boolean = true,
        extractUserViaUsername: Boolean = true,
    ): User? {
        if (input.isEmpty()) // If empty, just ignore it
            return null

        if (usersInContext != null && extractUserViaMention) {
            for (user in usersInContext) {
                if (user.asMention == input.replace("!", "")) {
                    return user
                }
            }
        }

        if (guild != null) {
            if (extractUserViaNameAndDiscriminator) {
                // TODO: Support names with space (maybe impossible)
                val split = input.split("#")
                if (split.size == 2) {
                    val discriminator = split.last()
                    val name = split.dropLast(1).joinToString(" ")
                    try {
                        val matchedMember = guild.getMemberByTag(name, discriminator)
                        if (matchedMember != null)
                            return matchedMember.user
                    } catch (e: IllegalArgumentException) {} // We don't really care if it is in a invalid format
                }
            }

            // Ok então... se não é link e nem menção... Que tal então verificar por nome?
            if (extractUserViaEffectiveName) {
                val matchedMembers = guild.getMembersByEffectiveName(input, true)
                val matchedMember = matchedMembers.firstOrNull()
                if (matchedMember != null)
                    return matchedMember.user
            }

            // Se não, vamos procurar só pelo username mesmo
            if (extractUserViaUsername) {
                val matchedMembers = guild.getMembersByName(input, true)
                val matchedMember = matchedMembers.firstOrNull()
                if (matchedMember != null)
                    return matchedMember.user
            }
        }

        return null
    }


    fun String.normalize(): String {
        val normalizedString = Normalizer.normalize(this, Normalizer.Form.NFD)
        val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return regex.replace(normalizedString, "")
    }
}