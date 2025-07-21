package net.perfectdreams.interactions.commands.options

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.perfectdreams.interactions.commands.declarations.localization.LocalizedString

open class ApplicationCommandOptions {
    companion object {
        val NO_OPTIONS = object : ApplicationCommandOptions() {}
    }

    val registeredOptions = mutableListOf<OptionReference<*>>()

    fun string(name: String, description: String, range: IntRange? = null, builder: StringDiscordOptionReference<String>.() -> (Unit) = {}) =
        string(name, LocalizedString(description), range, builder)

    fun string(name: String, description: LocalizedString, range: IntRange? = null, builder: StringDiscordOptionReference<String>.() -> (Unit) = {}) =
        StringDiscordOptionReference<String>(name, description, required = true, range)
            .apply(builder)
            .also { registeredOptions.add(it) }

    fun optionalString(name: String, description: String, range: IntRange? = null, builder: StringDiscordOptionReference<String?>.() -> (Unit) = {}) =
        optionalString(name, LocalizedString(description), range, builder)

    fun optionalString(name: String, description: LocalizedString, range: IntRange? = null, builder: StringDiscordOptionReference<String?>.() -> (Unit) = {}) = StringDiscordOptionReference<String?>(name, description, false, range)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun boolean(name: String, description: LocalizedString, builder: BooleanDiscordOptionReference<Boolean>.() -> (Unit) = {}) = BooleanDiscordOptionReference<Boolean>(name, description, true)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun optionalBoolean(name: String, description: LocalizedString, builder: BooleanDiscordOptionReference<Boolean?>.() -> (Unit) = {}) = BooleanDiscordOptionReference<Boolean?>(name, description, false)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun long(name: String, description: LocalizedString, requiredRange: LongRange? = null) = LongDiscordOptionReference<Long>(name, description, true, requiredRange)
        .also { registeredOptions.add(it) }

    fun optionalLong(name: String, description: LocalizedString, requiredRange: LongRange? = null) = LongDiscordOptionReference<Long?>(name, description, false, requiredRange)
        .also { registeredOptions.add(it) }

    fun double(name: String, description: LocalizedString, requiredRange: ClosedFloatingPointRange<Double>? = null) = NumberDiscordOptionReference<Double>(name, description, true, requiredRange)
        .also { registeredOptions.add(it) }

    fun optionalDouble(name: String, description: LocalizedString, requiredRange: ClosedFloatingPointRange<Double>? = null) = NumberDiscordOptionReference<Double?>(name, description, false, requiredRange)
        .also { registeredOptions.add(it) }

    fun channel(name: String, description: LocalizedString) = ChannelDiscordOptionReference<GuildChannel>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalChannel(name: String, description: LocalizedString) = ChannelDiscordOptionReference<GuildChannel?>(name,description, false)
        .also { registeredOptions.add(it) }

    fun user(name: String, description: LocalizedString) = UserDiscordOptionReference<UserAndMember>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalUser(name: String, description: LocalizedString) = UserDiscordOptionReference<UserAndMember?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun role(name: String, description: LocalizedString) = RoleDiscordOptionReference<Role>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalRole(name: String, description: LocalizedString) = RoleDiscordOptionReference<Role?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun attachment(name: String, description: LocalizedString) = AttachmentDiscordOptionReference<Message.Attachment>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalAttachment(name: String, description: LocalizedString) = AttachmentDiscordOptionReference<Message.Attachment?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun imageReference(name: String) = ImageReferenceDiscordOptionReference<ImageReference>(name)
        .also { registeredOptions.add(it) }

    fun imageReferenceOrAttachment(name: String, description: String) = ImageReferenceOrAttachmentDiscordOptionReference<ImageReferenceOrAttachment>(name)
        .also { registeredOptions.add(it) }
}