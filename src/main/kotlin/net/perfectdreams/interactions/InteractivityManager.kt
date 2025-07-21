package net.perfectdreams.interactions

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.interactions.components.ComponentContext
import net.perfectdreams.interactions.components.UnleashedButton
import net.perfectdreams.interactions.components.UnleashedComponentId
import net.perfectdreams.interactions.components.exceptions.ComponentException
import net.perfectdreams.interactions.modals.ModalArguments
import net.perfectdreams.interactions.modals.ModalContext
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class InteractivityManager(invalidationDelay: Duration = 5.minutes) {
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val buttonInteractionCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(invalidationDelay.toJavaDuration())
        .build<UUID, ButtonInteractionCallback>()
        .asMap()
    val selectMenuInteractionCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(invalidationDelay.toJavaDuration())
        .build<UUID, SelectMenuInteractionCallback>()
        .asMap()
    val selectMenuEntityInteractionCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(invalidationDelay.toJavaDuration())
        .build<UUID, SelectMenuEntityInteractionCallback>()
        .asMap()
    val modalCallbacks = Caffeine
        .newBuilder()
        .expireAfterWrite(invalidationDelay.toJavaDuration())
        .build<UUID, ModalInteractionCallback>()
        .asMap()

    /**
     * Creates an interactive button
     */
    fun buttonForUser(
        targetUser: User,
        callbackAlwaysEphemeral: Boolean,
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {},
        callback: suspend (ComponentContext) -> (Unit)
    ) = buttonForUser(targetUser.idLong, callbackAlwaysEphemeral, style, label, builder, callback)

    /**
     * Creates an interactive button, the ID in the [button] will be replaced with a [UnleashedComponentId]
     */
    fun buttonForUser(
        targetUser: User,
        callbackAlwaysEphemeral: Boolean,
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ) = buttonForUser(targetUser.idLong, callbackAlwaysEphemeral, button, callback)

    /**
     * Creates an interactive button
     */
    fun buttonForUser(
        targetUserId: Long,
        callbackAlwaysEphemeral: Boolean,
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {},
        callback: suspend (ComponentContext) -> (Unit)
    ) = button(
        callbackAlwaysEphemeral,
        style,
        label,
        builder
    ) {
        if (targetUserId != it.user.idLong)
            throw ComponentException("The user '${it.user.idLong}' who interacted with this component isn't the same as the target user '$targetUserId'!")

        callback.invoke(it)
    }

    /**
     * Creates an interactive button, the ID in the [button] will be replaced with a [UnleashedComponentId]
     */
    fun buttonForUser(
        targetUserId: Long,
        callbackAlwaysEphemeral: Boolean,
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ) = button(
        callbackAlwaysEphemeral,
        button
    ) {
        if (targetUserId != it.user.idLong)
            throw ComponentException("The user '${it.user.idLong}' who interacted with this component isn't the same as the target user '$targetUserId'!")

        callback.invoke(it)
    }

    /**
     * Creates an interactive button
     */
    fun button(
        callbackAlwaysEphemeral: Boolean,
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {},
        callback: suspend (ComponentContext) -> (Unit)
    ) = button(
        callbackAlwaysEphemeral,
        UnleashedButton.of(style, label, null)
            .let {
                JDAButtonBuilder(it).apply(builder).button
            },
        callback
    )

    /**
     * Creates an interactive button, the ID in the [button] will be replaced with a [UnleashedComponentId]
     */
    fun button(
        callbackAlwaysEphemeral: Boolean,
        button: Button,
        callback: suspend (ComponentContext) -> (Unit)
    ): Button {
        val buttonId = UUID.randomUUID()
        buttonInteractionCallbacks[buttonId] = ButtonInteractionCallback(callbackAlwaysEphemeral, callback)
        return button
            .withId(UnleashedComponentId(buttonId).toString())
    }

    /**
     * Creates an disabled button
     */
    fun disabledButton(
        callbackAlwaysEphemeral: Boolean,
        style: ButtonStyle,
        label: String = "",
        builder: (JDAButtonBuilder).() -> (Unit) = {}
    ): Button {
        val buttonId = UUID.randomUUID()
        // In recent JDA updates, JDA trips a check if the label && emoji are empty
        // This is bad for us, because we use this as a builder and, in some things, we set the emoji after the button is created, which
        // completely borks out any buttons that have an empty label + button
        //
        // To work around this, we set a " " label to bypass the check
        // This MUST be refactored later, because if JDA changes the check again, this WILL break!
        return Button.of(style, "disabled:$buttonId", label.let { if (it.isEmpty()) " " else it })
            .let {
                JDAButtonBuilder(it).apply(builder)
                    .apply {
                        disabled = true
                    }
                    .button
            }
    }

    /**
     * Creates an interactive select menu
     */
    fun stringSelectMenuForUser(
        targetUser: User,
        callbackAlwaysEphemeral: Boolean,
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ) = stringSelectMenuForUser(targetUser.idLong, callbackAlwaysEphemeral, builder, callback)

    /**
     * Creates an interactive select menu
     */
    fun stringSelectMenuForUser(
        targetUserId: Long,
        callbackAlwaysEphemeral: Boolean,
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ) = stringSelectMenu(
        callbackAlwaysEphemeral,
        builder
    ) { context, strings ->
        if (targetUserId != context.user.idLong)
            throw ComponentException("The user '${context.user.idLong}' who interacted with this component isn't the same as the target user '$targetUserId'!")

        callback.invoke(context, strings)
    }

    /**
     * Creates an interactive select menu
     */
    fun stringSelectMenu(
        callbackAlwaysEphemeral: Boolean,
        builder: (StringSelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<String>) -> (Unit)
    ): StringSelectMenu {
        val buttonId = UUID.randomUUID()
        selectMenuInteractionCallbacks[buttonId] = SelectMenuInteractionCallback(callbackAlwaysEphemeral, callback)
        return StringSelectMenu.create(UnleashedComponentId(buttonId).toString())
            .apply(builder)
            .build()
    }

    /**
     * Creates an interactive select menu
     */
    fun entitySelectMenuForUser(
        targetUser: User,
        callbackAlwaysEphemeral: Boolean,
        builder: (EntitySelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<IMentionable>) -> (Unit)
    ) = entitySelectMenuForUser(targetUser.idLong, callbackAlwaysEphemeral, builder, callback)

    /**
     * Creates an interactive select menu
     */
    fun entitySelectMenuForUser(
        targetUserId: Long,
        callbackAlwaysEphemeral: Boolean,
        builder: (EntitySelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<IMentionable>) -> (Unit)
    ) = entitySelectMenu(
        callbackAlwaysEphemeral,
        builder
    ) { context, strings ->
        if (targetUserId != context.user.idLong)
            throw ComponentException("The user '${context.user.idLong}' who interacted with this component isn't the same as the target user '$targetUserId'!")

        callback.invoke(context, strings)
    }

    /**
     * Creates an interactive select menu
     */
    fun entitySelectMenu(
        callbackAlwaysEphemeral: Boolean,
        builder: (EntitySelectMenu.Builder).() -> (Unit) = {},
        callback: suspend (ComponentContext, List<IMentionable>) -> (Unit)
    ): EntitySelectMenu {
        val buttonId = UUID.randomUUID()
        selectMenuEntityInteractionCallbacks[buttonId] = SelectMenuEntityInteractionCallback(callbackAlwaysEphemeral, callback)
        return EntitySelectMenu.create(UnleashedComponentId(buttonId).toString(), listOf(EntitySelectMenu.SelectTarget.CHANNEL))
            .apply(builder)
            .build()
    }

    class JDAButtonBuilder(internal var button: Button) {
        // https://youtrack.jetbrains.com/issue/KT-6519
        @get:JvmSynthetic // Hide from Java callers
        var emoji: Emoji
            @Deprecated("", level = DeprecationLevel.ERROR) // Prevent Kotlin callers
            get() = throw UnsupportedOperationException()
            set(value) {
                button = button.withEmoji(value)
            }

        var disabled
            get() = button.isDisabled
            set(value) {
                this.button = button.withDisabled(value)
            }
    }

    data class ButtonInteractionCallback(
        /**
         * If true, the created context will always be ephemeral
         */
        val alwaysEphemeral: Boolean,
        val callback: suspend (ComponentContext) -> (Unit)
    )

    data class SelectMenuInteractionCallback(
        /**
         * If true, the created context will always be ephemeral
         */
        val alwaysEphemeral: Boolean,
        val callback: suspend (ComponentContext, List<String>) -> (Unit)
    )

    data class SelectMenuEntityInteractionCallback(
        /**
         * If true, the created context will always be ephemeral
         */
        val alwaysEphemeral: Boolean,
        val callback: suspend (ComponentContext, List<IMentionable>) -> (Unit)
    )

    data class ModalInteractionCallback(
        /**
         * If true, the created context will always be ephemeral
         */
        val alwaysEphemeral: Boolean,
        val callback: suspend (ModalContext, ModalArguments) -> (Unit)
    )
}