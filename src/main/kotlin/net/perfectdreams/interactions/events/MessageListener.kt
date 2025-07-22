package net.perfectdreams.interactions.events

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.interactions.UnleashedCommandManager
import net.perfectdreams.interactions.commands.exceptions.SimpleCommandException
import net.perfectdreams.interactions.events.message.MessageModule
import net.perfectdreams.interactions.utils.launchMessageJob
import java.lang.Exception

class MessageListener(val manager: UnleashedCommandManager, var prefix: String? = "+") : ListenerAdapter() {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    val modules = mutableListOf<MessageModule<out GenericMessageEvent>>()
    val unavailableMessages = mutableListOf<Long>()

    init {
        manager.jda.addEventListener(this)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        for (module in modules) {
            if (module.canHandle(event)) {
                (module as MessageModule<GenericMessageEvent>).listen(event)
            }
        }

        if (event.author.isBot)
            return

        if (event.message.type != MessageType.DEFAULT && event.message.type != MessageType.INLINE_REPLY)
            return

        launchMessageJob(event) {
            try {
                if (event.isFromType(ChannelType.PRIVATE)) {
                    if (isMentioningOnlyMe(event.message.contentRaw)) {
                        throw SimpleCommandException("Using commands in direct messages!")
                    }
                } else {
                    event.member ?: run {
                        logger.warn { "${event.author} left the guild ${event.guild.id} before I was able to process the message." }
                        return@launchMessageJob
                    }

                    if (isMentioningOnlyMe(event.message.contentRaw)) {
                        if (event.channel.canTalk() && manager.options.mentionMessage != null) {
                            event.channel.sendMessage(manager.options.mentionMessage).queue()
                        }
                    }
                }

                val e = MessageEvent(
                    event.author,
                    null,
                    event.message,
                    event.messageIdLong,
                    null,
                    event.channel,
                    null,
                    event.jda
                )

                if (checkCommandsAndDispatch(e))
                    return@launchMessageJob
            } catch (e: SimpleCommandException) {
                logger.error(e) { e.reason }
            } catch (e: Exception) {
                logger.error(e) { "[${event.guild.id}] Error while processing message of ${event.author.name} (${event.author.id} - ${event.message.contentRaw})" }
            }
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (event.message.isPinned) unavailableMessages.add(event.messageIdLong)
        if (unavailableMessages.contains(event.messageIdLong)) return

        if (System.currentTimeMillis() - 900_000 >= event.message.timeCreated.toEpochSecond() * 1000) return

        if (event.author.isBot)
            return

        if (event.channel.type == ChannelType.TEXT) {
            GlobalScope.launch {
                event.member ?: run {
                    logger.warn { "${event.author} left the guild ${event.guild.id} before I was able to process the message." }
                    return@launch
                }

                val messageEvent = MessageEvent(
                    event.author,
                    event.member,
                    event.message,
                    event.messageIdLong,
                    event.guild,
                    event.channel,
                    event.channel.asTextChannel(),
                    event.jda
                )

                for (module in modules) {
                    if (module.canHandle(event)) {
                        (module as MessageModule<GenericMessageEvent>).listen(event)
                    }
                }

                if (event.message.isEdited) {
                    if (checkCommandsAndDispatch(messageEvent))
                        return@launch
                }
            }
        }
    }

    suspend fun checkCommandsAndDispatch(event: MessageEvent): Boolean {
        if (event.channel is TextChannel && !event.channel.canTalk())
            return false

        val rawMessage = event.message.contentRaw
        val rawArguments = rawMessage
            .split(Regex(" +"))
            .toMutableList()

        val firstLabel = rawArguments.first()
        val startsWithCommandPrefix = firstLabel.startsWith(prefix!!)
        val startsWithBotMention = firstLabel == "<@${manager.jda.selfUser.idLong}>" || firstLabel == "<@!${manager.jda.selfUser.idLong}>"

        if (startsWithCommandPrefix || startsWithBotMention) {
            if (startsWithCommandPrefix) // If it is a command prefix, remove the prefix
                rawArguments[0] = rawArguments[0].removePrefix(prefix!!)
            else if (startsWithBotMention) { // If it is a mention, remove the first argument (which is Loritta's mention)
                rawArguments.removeAt(0)
                if (rawArguments.isEmpty()) // If it is empty, then it means that it was only Loritta's mention, so just return false
                    return false
            }

            if (rawArguments[0].contains("\n")) {
                val splitNewLines = rawArguments[0].split(Regex("(?=\n+)", RegexOption.MULTILINE))
                rawArguments[0] = splitNewLines[0]
                    .replace("\n", "")

                rawArguments.addAll(1, splitNewLines.drop(1))
            }

            if (manager.matches(event, rawArguments)) return true
        }

        return false
    }

    /**
     * Checks if the message contains only a mention for me
     *
     * @param contentRaw the raw content of the message
     * @returns if the message is mentioning only me
     */
    fun isMentioningOnlyMe(contentRaw: String): Boolean = contentRaw.replace("!", "").trim() == "<@${manager.jda.selfUser.idLong}>"

    data class MessageEvent(
        val author: User,
        val member: Member? = null,
        val message: Message,
        val messageId: Long,
        val guild: Guild? = null,
        val channel: MessageChannelUnion,
        val textChannel: TextChannel?,
        val jda: JDA
    )
}