package net.perfectdreams.interactions.utils

import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent
import net.dv8tion.jda.api.components.section.SectionContentComponent
import net.dv8tion.jda.api.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.interactions.InteractivityManager
import net.perfectdreams.interactions.UnleashedCommandManager
import net.perfectdreams.interactions.components.ComponentContext

class ComponentV2MessageBuilder {
    class ContainerBuilder {
        val childs = mutableListOf<ContainerChildComponent>()

        fun addChild(child: ContainerChildComponent) = childs.add(child)

        fun text(content: String) = TextDisplay.of(content).also(::addChild)
        fun section(block: SectionBuilder.() -> Unit) = SectionBuilder().apply(block).build().also(::addChild)

        fun separator(invisible: Boolean = false, spacing: Separator.Spacing? = null) = (if (invisible) {
            Separator.createInvisible(spacing ?: Separator.Spacing.SMALL)
        } else {
            Separator.createDivider(spacing ?: Separator.Spacing.SMALL)
        }).also(::addChild)

        fun build() = Container.of(childs)
    }

    class SectionBuilder {
        val childs = mutableListOf<SectionContentComponent>()
        private var accessory: SectionAccessoryComponent? = null

        private fun addChild(c: SectionContentComponent) = childs.add(c)

        fun thumbnail(url: String) {
            if (accessory != null)
                error("This section already has an accessory, it's not supposed to have two.")

            accessory = Thumbnail.fromUrl(url)
        }

        fun text(content: String) = TextDisplay.of(content).also(::addChild)
        fun text(block: StringBuilder.() -> Unit) {
            val text = StringBuilder().apply(block).toString()

            TextDisplay.of(text).also(::addChild)
        }

        fun build() = Section.of(
            accessory ?: error("Accessory cannot be null!"),
            childs
        )
    }

    val childs = mutableListOf<MessageTopLevelComponent>()

    private fun addChild(c: MessageTopLevelComponent) = childs.add(c)

    fun text(content: String) = TextDisplay.of(content).also(::addChild)
    fun text(block: StringBuilder.() -> Unit) {
        val text = StringBuilder().apply(block).toString()

        TextDisplay.of(text).also(::addChild)
    }
    fun container(block: ContainerBuilder.() -> Unit) = ContainerBuilder().apply(block).build().also(::addChild)
    fun section(block: SectionBuilder.() -> Unit) = SectionBuilder().apply(block).build().also(::addChild)

    fun separator(invisible: Boolean = false, spacing: Separator.Spacing? = null) = (if (invisible) {
        Separator.createInvisible(spacing ?: Separator.Spacing.SMALL)
    } else {
        Separator.createDivider(spacing ?: Separator.Spacing.SMALL)
    }).also(::addChild)
}

class ActionRowBuilder(val manager: UnleashedCommandManager) {
    val components = mutableListOf<ActionRowChildComponent>()

    private fun insertComponent(c: ActionRowChildComponent) = components.add(c)

    fun buttonForUser(targetUser: Long, alwaysEphemeral: Boolean, style: ButtonStyle, label: String = "", designBuilder: InteractivityManager.JDAButtonBuilder.() -> Unit = {}, callbackBuilder: suspend (ComponentContext) -> Unit = {}) =
        manager.interactivityManager.buttonForUser(targetUser, alwaysEphemeral, style, label, designBuilder, callbackBuilder).also(::insertComponent)

    fun stringSelectMenuForUser(targetUser: Long, alwaysEphemeral: Boolean, designBuilder: StringSelectMenu.Builder.() -> Unit = {}, callbackBuilder: suspend (ComponentContext, List<String>) -> Unit) =
        manager.interactivityManager.stringSelectMenuForUser(targetUser, alwaysEphemeral, designBuilder, callbackBuilder).also(::insertComponent)

    fun stringSelectMenu(alwaysEphemeral: Boolean, designBuilder: StringSelectMenu.Builder.() -> Unit = {}, callbackBuilder: suspend (ComponentContext, List<String>) -> Unit) =
        manager.interactivityManager.stringSelectMenu(alwaysEphemeral, designBuilder, callbackBuilder).also(::insertComponent)

    fun entitySelectMenuForUser(targetUser: Long, alwaysEphemeral: Boolean, designBuilder: EntitySelectMenu.Builder.() -> Unit, callbackBuilder: suspend (ComponentContext, List<IMentionable>) -> Unit) =
        manager.interactivityManager.entitySelectMenuForUser(targetUser, alwaysEphemeral, designBuilder, callbackBuilder).also(::insertComponent)

    fun entitySelectMenu(alwaysEphemeral: Boolean, designBuilder: EntitySelectMenu.Builder.() -> Unit = {}, callbackBuilder: suspend (ComponentContext, List<IMentionable>) -> Unit) =
        manager.interactivityManager.entitySelectMenu(alwaysEphemeral, designBuilder, callbackBuilder).also(::insertComponent)

    fun disabledButton(alwaysEphemeral: Boolean, style: ButtonStyle, label: String = "", builder: InteractivityManager.JDAButtonBuilder.() -> Unit = {}) =
        manager.interactivityManager.disabledButton(alwaysEphemeral, style, label, builder)

    fun build() = ActionRow.of(components)
}

sealed class ModalInputs(open val id: String) {
    class TextInput(
        override val id: String,
        val label: String,
        val style: TextInputStyle,
        val required: Boolean = false,
        val value: String? = null,
        val placeholder: String? = null,
        val requiredLength: IntRange? = null
    ) : ModalInputs(id)
}