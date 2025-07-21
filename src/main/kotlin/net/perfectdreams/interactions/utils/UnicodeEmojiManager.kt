package net.perfectdreams.interactions.utils

import net.perfectdreams.interactions.UnleashedCommandManager
import kotlin.getValue

class UnicodeEmojiManager {
    companion object {
        val codePoints: MutableSet<List<Int>> = mutableSetOf()

        val regex by lazy {
            codePoints.sortedByDescending { it.size }
                .joinToString("|") { Regex.escape(it.joinToString("") { Character.toChars(it).concatToString() }) }
                .let {
                    Regex(it)
                }
        }
    }

    init {
        // https://unicode.org/Public/emoji/14.0/emoji-sequences.txt
        loadFromPath("/emojis/emoji-sequences.txt")
        loadFromPath("/emojis/emoji-zwj-sequences.txt")
    }

    private fun loadFromPath(path: String) {
        UnleashedCommandManager::class.java.getResourceAsStream(path)
            .readAllBytes()
            .toString(Charsets.UTF_8)
            .lines()
            .filter { !it.startsWith("#") && it.isNotBlank() }
            .forEach {
                val data = it.substringBefore(";").trim()

                if (data.contains("..")) {
                    val split = data.split("..")

                    val minRange = Integer.parseInt(split[0], 16)
                    val maxRange = Integer.parseInt(split[1], 16)

                    for (codePoint in minRange..maxRange) {
                        codePoints.add(listOf(codePoint))
                    }
                } else {
                    codePoints.add(
                        data.split(" ").map {
                            Integer.parseInt(it, 16)
                        }
                    )
                }
            }
    }
}