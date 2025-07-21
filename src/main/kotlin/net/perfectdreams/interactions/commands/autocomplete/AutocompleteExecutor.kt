package net.perfectdreams.interactions.commands.autocomplete

fun interface AutocompleteExecutor<T> {
    suspend fun execute(
        context: AutocompleteContext
    ): Map<String, T>
}