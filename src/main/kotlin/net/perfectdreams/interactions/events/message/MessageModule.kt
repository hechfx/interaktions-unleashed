package net.perfectdreams.interactions.events.message

import net.dv8tion.jda.api.events.message.GenericMessageEvent

abstract class MessageModule<T : GenericMessageEvent> {
    abstract fun listen(event: T)
    abstract fun canHandle(event: GenericMessageEvent): Boolean
}