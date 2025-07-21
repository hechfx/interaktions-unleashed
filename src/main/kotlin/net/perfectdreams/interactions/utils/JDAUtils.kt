package net.perfectdreams.interactions.utils

import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.components.container.Container
import net.dv8tion.jda.api.components.container.ContainerChildComponent
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.section.SectionAccessoryComponent
import net.dv8tion.jda.api.components.section.SectionContentComponent
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.thumbnail.Thumbnail

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