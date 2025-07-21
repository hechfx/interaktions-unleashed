package net.perfectdreams.interactions.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

private val logger by HarmonyLoggerFactory.logger {}

val coroutineMessageExecutor = createThreadPool("Message Executor Thread %d")
val coroutineMessageDispatcher = coroutineMessageExecutor.asCoroutineDispatcher() // Coroutine Dispatcher
val pendingMessages = ConcurrentLinkedQueue<Job>()

fun createThreadPool(name: String) = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())

fun launchMessageJob(event: Event, block: suspend CoroutineScope.() -> Unit) {
    val coroutineName = when (event) {
        is MessageReceivedEvent -> "Message ${event.message} by user ${event.author} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
        is SlashCommandInteractionEvent -> "Slash Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
        is UserContextInteractionEvent -> "User Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
        is MessageContextInteractionEvent -> "User Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
        is CommandAutoCompleteInteractionEvent -> "Autocomplete for Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
        else -> throw IllegalArgumentException("You can't dispatch a $event in a launchMessageJob!")
    }

    val start = System.currentTimeMillis()
    val job = GlobalScope.launch(
        coroutineMessageDispatcher + CoroutineName(coroutineName),
        block = block
    )
    // Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
    // added to the list, causing leaks.
    // invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
    pendingMessages.add(job)
    job.invokeOnCompletion {
        pendingMessages.remove(job)

        val diff = System.currentTimeMillis() - start
        if (diff >= 60_000) {
            logger.warn { "Message Coroutine $job took too long to process! ${diff}ms" }
        }
    }
}