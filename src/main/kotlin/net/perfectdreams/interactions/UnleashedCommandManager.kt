package net.perfectdreams.interactions

import com.github.benmanes.caffeine.cache.Caffeine
import dev.minn.jda.ktx.interactions.commands.Option
import dev.minn.jda.ktx.interactions.commands.choice
import dev.minn.jda.ktx.interactions.commands.group
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.interactions.commands.updateCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.harmony.logging.slf4j.HarmonyLoggerCreatorSLF4J
import net.perfectdreams.interactions.builders.UnleashedCommandManagerConfigurationBuilder
import net.perfectdreams.interactions.commands.*
import net.perfectdreams.interactions.commands.context.LegacyMessageCommandContext
import net.perfectdreams.interactions.commands.declarations.*
import net.perfectdreams.interactions.commands.declarations.localization.LocaleManager
import net.perfectdreams.interactions.commands.declarations.wrappers.*
import net.perfectdreams.interactions.commands.exceptions.*
import net.perfectdreams.interactions.commands.executors.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.interactions.commands.options.*
import net.perfectdreams.interactions.events.InteractionsListener
import net.perfectdreams.interactions.events.MessageListener
import net.perfectdreams.interactions.utils.TextUtils.normalize
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class UnleashedCommandManager(val jda: JDA, optionsBuilder: UnleashedCommandManagerConfigurationBuilder.() -> Unit): ListenerAdapter() {
    companion object {
        init {
            HarmonyLoggerFactory.setLoggerCreator(HarmonyLoggerCreatorSLF4J())
        }

        val guildLocales = Caffeine
            .newBuilder()
            .expireAfterWrite(5.minutes.toJavaDuration())
            .build<Long, DiscordLocale>()
            .asMap()

        val userLocales = Caffeine
            .newBuilder()
            .expireAfterWrite(5.minutes.toJavaDuration())
            .build<Long, DiscordLocale>()
            .asMap()

        lateinit var localeManager: LocaleManager

        private val logger by HarmonyLoggerFactory.logger {}
    }

    val slashCommands = mutableListOf<SlashCommandDeclaration>()
    val userCommands = mutableListOf<UserCommandDeclaration>()
    val messageCommands = mutableListOf<MessageCommandDeclaration>()
    val applicationCommands: List<ExecutableApplicationCommandDeclaration>
        get() = slashCommands + userCommands + messageCommands

    val options = UnleashedCommandManagerConfigurationBuilder().apply(optionsBuilder).build()

    val interactivityManager = InteractivityManager()

    val messageListener = MessageListener(this, options.prefix)

    private var slashCommandPathToDeclarations = mutableMapOf<String, CommandDeclarationPair>()
    private var userCommandPathToDeclarations = mutableMapOf<String, UserCommandDeclaration>()
    private var messageCommandPathToDeclarations = mutableMapOf<String, MessageCommandDeclaration>()
    var legacyCommandPathToDeclarations = mutableMapOf<String, CommandDeclarationPair>()
    var commandMentions: CommandMentions? = null

    private var hasAlreadyGloballyUpdatedTheCommands = false
    private var hasAlreadyUpdatedTheEmojis = false

    init {
        localeManager = LocaleManager(options.localePath, this)

        InteractionsListener(this)

        jda.addEventListener(this)
    }

    override fun onReady(event: ReadyEvent) {
        GlobalScope.launch {
            if (options.registerCommandGlobally && !hasAlreadyGloballyUpdatedTheCommands) {
                hasAlreadyGloballyUpdatedTheCommands = true

                val registeredCommands = updateCommands(
                    0
                ) { commands ->
                    event.jda.updateCommands {
                        addCommands(*commands.toTypedArray())
                    }.complete()
                }

                logger.info { "We have ${registeredCommands.size} registered commands, converting it into command mentions..." }

                commandMentions = CommandMentions(registeredCommands)

                logger.info { "Successfully converted ${registeredCommands.size} registered commands into command mentions!" }
            } else {
                jda.guilds.filter { it.idLong in options.guildsToRegisterCommands }
                    .forEach {
                        val registeredCommands = updateCommands(
                            it.idLong
                        ) { commands ->
                            it.updateCommands {
                                addCommands(*commands.toTypedArray())
                            }.complete()
                        }

                        logger.info { "We have ${registeredCommands.size} registered commands, converting it into command mentions..." }

                        commandMentions = CommandMentions(registeredCommands)

                        logger.info { "Successfully converted ${registeredCommands.size} registered commands into command mentions!" }
                    }
            }
        }

        updateCommandPathToDeclarations()
    }

    fun register(declaration: SlashCommandDeclarationWrapper) {
        val builtDeclaration = declaration.command().build()

        if (builtDeclaration.enableLegacyMessageSupport) {
            // Validate if all executors inherit LorittaLegacyMessageCommandExecutor
            val executors = mutableListOf<Any>()
            if (builtDeclaration.executor != null)
                executors += builtDeclaration.executor

            for (subcommand in builtDeclaration.subcommands) {
                if (subcommand.defaultMemberPermissions != null)
                    error("Subcommands cannot have defaultMemberPermissions!")

                if (subcommand.executor != null)
                    executors += subcommand.executor
            }

            for (subcommandGroup in builtDeclaration.subcommandGroups) {
                for (subcommand in subcommandGroup.subcommands) {
                    if (subcommand.defaultMemberPermissions != null)
                        error("Subcommands cannot have defaultMemberPermissions!")

                    if (subcommand.executor != null)
                        executors += subcommand.executor
                }
            }

            for (executor in executors) {
                if (executor !is LorittaLegacyMessageCommandExecutor)
                    error("${executor::class.simpleName} does not inherit LorittaLegacyMessageCommandExecutor, but enable legacy message support is enabled!")
            }
        }

        slashCommands += builtDeclaration
    }

    fun register(declaration: UserCommandDeclarationWrapper) {
        userCommands += declaration.command().build()
    }

    fun register(declaration: MessageCommandDeclarationWrapper) {
        messageCommands += declaration.command().build()
    }

    private fun updateCommandPathToDeclarations() {
        this.slashCommandPathToDeclarations = createSlashCommandPathToDeclarations()
        this.userCommandPathToDeclarations = createGenericCommandPathToDeclarations(this.userCommands)
        this.messageCommandPathToDeclarations = createGenericCommandPathToDeclarations(this.messageCommands)
        this.legacyCommandPathToDeclarations = createLegacyCommandPathToDeclarations()
    }

    private fun createSlashCommandPathToDeclarations(): MutableMap<String, CommandDeclarationPair> {
        val slashCommandPathToDeclarations = mutableMapOf<String, CommandDeclarationPair>()
        for (rootDeclaration in slashCommands) {
            if (isDeclarationExecutable(rootDeclaration)) {
                slashCommandPathToDeclarations[rootDeclaration.name.value] =
                    CommandDeclarationPair(rootDeclaration, rootDeclaration)
            }

            for (subcommand in rootDeclaration.subcommands) {
                if (isDeclarationExecutable(subcommand)) {
                    slashCommandPathToDeclarations["${rootDeclaration.name.value} ${subcommand.name.value}"] =
                        CommandDeclarationPair(rootDeclaration, subcommand)
                }
            }

            for (subcommandGroup in rootDeclaration.subcommandGroups) {
                for (subcommand in subcommandGroup.subcommands) {
                    if (isDeclarationExecutable(subcommand)) {
                        slashCommandPathToDeclarations["${rootDeclaration.name.value} ${subcommandGroup.name.value} ${subcommand.name.value}"] =
                            CommandDeclarationPair(rootDeclaration, subcommand)
                    }
                }
            }
        }
        return slashCommandPathToDeclarations
    }

    private fun <T : ExecutableApplicationCommandDeclaration> createGenericCommandPathToDeclarations(declarations: List<T>): MutableMap<String, T> {
        val pathToDeclarations = mutableMapOf<String, T>()
        for (declaration in declarations) {
            pathToDeclarations[declaration.name.value] = declaration
        }
        return pathToDeclarations
    }

    private fun createLegacyCommandPathToDeclarations(): MutableMap<String, CommandDeclarationPair> {
        val commandPathToDeclarations = mutableMapOf<String, CommandDeclarationPair>()

        fun putNormalized(
            key: String,
            rootDeclaration: SlashCommandDeclaration,
            slashDeclaration: SlashCommandDeclaration
        ) {
            commandPathToDeclarations[key.normalize()] = CommandDeclarationPair(
                rootDeclaration,
                slashDeclaration
            )
        }

        // Get all executors that have enabled legacy message support enabled and add them to the command path
        for (declaration in slashCommands.filter { it.enableLegacyMessageSupport }) {
            val rootLabels = declaration.alternativeLegacyLabels + declaration.name.value

            if (isDeclarationExecutable(declaration)) {
                for (rootLabel in rootLabels) {
                    putNormalized(rootLabel, declaration, declaration)
                }

                // And add the absolute commands!
                for (absolutePath in declaration.alternativeLegacyAbsoluteCommandPaths) {
                    putNormalized(absolutePath, declaration, declaration)
                }
            }

            declaration.subcommands.forEach { subcommand ->
                if (isDeclarationExecutable(subcommand)) {
                    val subcommandLabels = subcommand.name.value + subcommand.alternativeLegacyLabels

                    for (rootLabel in rootLabels) {
                        for (subcommandLabel in subcommandLabels) {
                            putNormalized("$rootLabel $subcommandLabel", declaration, subcommand)
                        }
                    }

                    // And add the absolute commands!
                    for (absolutePath in subcommand.alternativeLegacyAbsoluteCommandPaths) {
                        putNormalized(absolutePath, declaration, subcommand)
                    }
                }
            }

            declaration.subcommandGroups.forEach { group ->
                val subcommandGroupLabels = group.name.value + group.alternativeLegacyLabels

                group.subcommands.forEach { subcommand ->
                    if (isDeclarationExecutable(subcommand)) {
                        val subcommandLabels = subcommand.name.value + subcommand.alternativeLegacyLabels

                        for (rootLabel in rootLabels) {
                            for (subcommandGroupLabel in subcommandGroupLabels) {
                                for (subcommandLabel in subcommandLabels) {
                                    putNormalized(
                                        "$rootLabel $subcommandGroupLabel $subcommandLabel",
                                        declaration,
                                        subcommand
                                    )
                                }
                            }
                        }

                        // And add the absolute commands!
                        for (absolutePath in subcommand.alternativeLegacyAbsoluteCommandPaths) {
                            putNormalized(absolutePath.normalize(), declaration, subcommand)
                        }
                    }
                }
            }
        }

        return commandPathToDeclarations
    }

    fun findSlashCommandByFullCommandName(fullCommandName: String) =
        this.slashCommandPathToDeclarations[fullCommandName]

    fun findUserCommandByFullCommandName(fullCommandName: String) = this.userCommandPathToDeclarations[fullCommandName]
    fun findMessageCommandByFullCommandName(fullCommandName: String) =
        this.messageCommandPathToDeclarations[fullCommandName]

    private fun isDeclarationExecutable(declaration: SlashCommandDeclaration) = declaration.executor != null

    /**
     * Checks if the command should be handled (if all conditions are valid, like labels, etc)
     *
     * This is used if a command has [enableLegacyMessageSupport]
     *
     * @param event          the event wrapped in a LorittaMessageEvent
     * @param legacyServerConfig        the server configuration
     * @param legacyLocale      the language of the server
     * @param lorittaUser the user that is executing this command
     * @return            if the command was handled or not
     */
    suspend fun matches(event: MessageListener.MessageEvent, rawArguments: List<String>): Boolean {
        var rootDeclaration: SlashCommandDeclaration? = null
        var slashDeclaration: SlashCommandDeclaration? = null

        var argumentsToBeDropped = 0

        var bestMatch: CommandDeclarationPair? = null
        var absolutePathSize = 0

        commandDeclarationsLoop@ for ((commandPath, declaration) in legacyCommandPathToDeclarations) {
            argumentsToBeDropped = 0

            val absolutePathSplit = commandPath.split(" ")

            if (absolutePathSize > absolutePathSplit.size)
                continue // Too smol, the current command is a better match

            for ((index, pathSection) in absolutePathSplit.withIndex()) {
                val rawArgument =
                    rawArguments.getOrNull(index)?.lowercase()?.normalize() ?: continue@commandDeclarationsLoop

                if (pathSection.normalize() == rawArgument) {
                    argumentsToBeDropped++
                } else {
                    continue@commandDeclarationsLoop
                }
            }

            bestMatch = declaration
            absolutePathSize = argumentsToBeDropped
        }

        if (bestMatch != null) {
            rootDeclaration = bestMatch.rootDeclaration
            slashDeclaration = bestMatch.slashDeclaration
            argumentsToBeDropped = absolutePathSize
        }

        // No match, bail out!
        if (rootDeclaration == null || slashDeclaration == null)
            return false

        val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")
        if (executor !is LorittaLegacyMessageCommandExecutor)
            error("$executor doesn't inherit LorittaLegacyMessageCommandExecutor!")

        // These variables are used in the catch { ... } block, to make our lives easier
        var context: UnleashedContext? = null

        try {
            val rawArgumentsAfterDrop = rawArguments.drop(argumentsToBeDropped)

            context = LegacyMessageCommandContext(
                if (event.guild != null) guildLocales[event.guild.idLong] else null,
                userLocales[event.author.idLong] ?: DiscordLocale.ENGLISH_US,
                event,
                rawArgumentsAfterDrop,
                rootDeclaration,
                slashDeclaration,
                this
            )

            val guild = context.guildOrNull

            if (rootDeclaration.isGuildOnly && guild == null)
                throw SimpleCommandException("This command should be used only inside guilds!")

            // Are we in a guild?
            if (guild != null) {
                // Get the permissions
                // To mimick how slash commands work, we only check the root declaration permissions
                // We need to get it as raw because JDA does not have an easy way to get each permission set in the command
                val requiredPermissionsRaw = rootDeclaration.defaultMemberPermissions?.permissionsRaw
                if (requiredPermissionsRaw != null) {
                    val requiredPermissions = Permission.getPermissions(requiredPermissionsRaw)

                    val missingPermissions = requiredPermissions.filter {
                        !context.member.hasPermission(
                            context.channel as GuildChannel,
                            it
                        )
                    }

                    if (missingPermissions.isNotEmpty())
                        throw SimpleCommandException("User doesn't have necessary permissions to use this command. ($missingPermissions)")
                }

                // If we are in a guild... (because private messages do not have any permissions)
                // Check if Loritta has all the necessary permissions
                val missingPermissions = slashDeclaration.botPermissions.filterNot {
                    guild.selfMember.hasPermission(
                        event.channel as GuildChannel,
                        it
                    )
                }

                if (missingPermissions.isNotEmpty()) {
                    // oh no
                    throw SimpleCommandException("Loritta doesn't have necessary permissions to execute this command. ($missingPermissions)")
                }
            }

            val argMap = executor.convertToInteractionsArguments(context, rawArgumentsAfterDrop)

            // If the argument map is null, bail out!
            if (argMap != null) {
                val args = SlashCommandArguments(SlashCommandArgumentsSource.SlashCommandArgumentsMapSource(argMap))

                executor.execute(
                    context,
                    args
                )
            }
        } catch (e: CommandException) {
            context?.reply(e.ephemeral, e.builder)
        } catch (e: SimpleCommandException) {
            logger.error(e) { e.reason }
        }

        return true
    }

    /**
     * Gets the declaration path (command -> group -> subcommand, and anything in between)
     */
    fun findDeclarationPath(endDeclaration: SlashCommandDeclaration): List<Any> {
        for (declaration in slashCommands) {
            if (declaration == endDeclaration) {
                return listOf(declaration)
            }

            for (subcommandDeclaration in declaration.subcommands) {
                if (subcommandDeclaration == endDeclaration)
                    return listOf(declaration, subcommandDeclaration)
            }

            for (group in declaration.subcommandGroups) {
                for (subcommandDeclaration in group.subcommands) {
                    if (subcommandDeclaration == endDeclaration)
                        return listOf(declaration, group, subcommandDeclaration)
                }
            }
        }

        error("Declaration path is null for $endDeclaration! This should never happen! Are you trying to find a declaration that isn't registered in InteraKTions Unleashed?")
    }

    /**
     * Converts a InteraKTions Unleashed [declaration] to JDA
     */
    fun convertDeclarationToJDA(declaration: SlashCommandDeclaration): SlashCommandData {
        return Commands.slash(declaration.name.value, declaration.description.value).apply {
            if (declaration.defaultMemberPermissions != null)
                this.defaultPermissions = declaration.defaultMemberPermissions
            this.setContexts(declaration.interactionContexts[0], *declaration.interactionContexts.toTypedArray())
            this.setIntegrationTypes(declaration.integrationTypes[0], *declaration.integrationTypes.toTypedArray())

            setNameLocalizations(localeManager.getAll(declaration.name))
            setDescriptionLocalizations(localeManager.getAll(declaration.description))

            if (declaration.subcommands.isNotEmpty() || declaration.subcommandGroups.isNotEmpty()) {
                // If legacy message support is enabled, then the executor *can* actually be used via message command, so we will skip this check
                if (declaration.executor != null && !declaration.enableLegacyMessageSupport)
                    error("Command ${declaration::class.simpleName} has a root executor, but it also has subcommand/subcommand groups!")

                for (subcommand in declaration.subcommands) {
                    subcommand(subcommand.name.value, subcommand.description.value) {
                        val executor = subcommand.executor ?: error("Subcommand does not have a executor!")

                        setNameLocalizations(localeManager.getAll(subcommand.name))
                        setDescriptionLocalizations(localeManager.getAll(subcommand.description))

                        for (ref in executor.options.registeredOptions) {
                            try {
                                addOptions(*createOption(ref).toTypedArray())
                            } catch (e: Exception) {
                                logger.error(e) { "Something went wrong while trying to add options of $executor" }
                            }
                        }
                    }
                }

                for (group in declaration.subcommandGroups) {
                    group(group.name.value, group.description.value) {
                        setNameLocalizations(localeManager.getAll(group.name))
                        setDescriptionLocalizations(localeManager.getAll(group.description))

                        for (subcommand in group.subcommands) {
                            subcommand(subcommand.name.value, subcommand.description.value) {
                                val executor = subcommand.executor ?: error("Subcommand does not have a executor!")

                                setNameLocalizations(localeManager.getAll(subcommand.name))
                                setDescriptionLocalizations(localeManager.getAll(subcommand.description))

                                for (ref in executor.options.registeredOptions) {
                                    try {
                                        addOptions(*createOption(ref).toTypedArray())
                                    } catch (e: Exception) {
                                        logger.error(e) { "Something went wrong while trying to add options of $executor" }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val executor = declaration.executor

                if (executor != null) {
                    for (ref in executor.options.registeredOptions) {
                        try {
                            addOptions(*createOption(ref).toTypedArray())
                        } catch (e: Exception) {
                            logger.error(e) { "Something went wrong while trying to add options of $executor" }
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts a InteraKTions Unleashed [declaration] to JDA
     */
    fun convertDeclarationToJDA(declaration: UserCommandDeclaration): CommandData {
        return Commands.user(declaration.name.value).apply {
            if (declaration.defaultMemberPermissions != null)
                this.defaultPermissions = declaration.defaultMemberPermissions
            this.setContexts(declaration.interactionContexts[0], *declaration.interactionContexts.toTypedArray())
            this.setIntegrationTypes(declaration.integrationTypes[0], *declaration.integrationTypes.toTypedArray())

            setNameLocalizations(localeManager.getAll(declaration.name))
        }
    }

    /**
     * Converts a InteraKTions Unleashed [declaration] to JDA
     */
    fun convertDeclarationToJDA(declaration: MessageCommandDeclaration): CommandData {
        return Commands.message(declaration.name.value).apply {
            if (declaration.defaultMemberPermissions != null)
                this.defaultPermissions = declaration.defaultMemberPermissions
            this.setContexts(declaration.interactionContexts[0], *declaration.interactionContexts.toTypedArray())
            this.setIntegrationTypes(declaration.integrationTypes[0], *declaration.integrationTypes.toTypedArray())

            setNameLocalizations(localeManager.getAll(declaration.name))
        }
    }

    private fun createOption(interaKTionsOption: OptionReference<*>): List<OptionData> {
        when (interaKTionsOption) {
            is DiscordOptionReference -> {
                val description = interaKTionsOption.description

                when (interaKTionsOption) {
                    is LongDiscordOptionReference -> {
                        return listOf(
                            Option<Long>(
                                interaKTionsOption.name,
                                description.value,
                                interaKTionsOption.required
                            ).apply {
                                if (interaKTionsOption.autocompleteExecutor != null) {
                                    isAutoComplete = true
                                }

                                this.setDescriptionLocalizations(localeManager.getAll(description))

                                if (interaKTionsOption.requiredRange != null) {
                                    setRequiredRange(
                                        interaKTionsOption.requiredRange.first,
                                        interaKTionsOption.requiredRange.last
                                    )
                                }
                            }
                        )
                    }

                    is NumberDiscordOptionReference -> {
                        return listOf(
                            Option<Double>(
                                interaKTionsOption.name,
                                description.value,
                                interaKTionsOption.required
                            ).apply {
                                if (interaKTionsOption.autocompleteExecutor != null) {
                                    isAutoComplete = true
                                }

                                this.setDescriptionLocalizations(localeManager.getAll(description))

                                if (interaKTionsOption.requiredRange != null) {
                                    setRequiredRange(
                                        interaKTionsOption.requiredRange.start,
                                        interaKTionsOption.requiredRange.endInclusive
                                    )
                                }
                            }
                        )
                    }

                    is StringDiscordOptionReference -> {
                        return listOf(
                            Option<String>(
                                interaKTionsOption.name,
                                description.value,
                                interaKTionsOption.required
                            ).apply {
                                if (interaKTionsOption.autocompleteExecutor != null) {
                                    isAutoComplete = true
                                }

                                this.setDescriptionLocalizations(localeManager.getAll(description))

                                if (interaKTionsOption.range != null)
                                    setRequiredLength(interaKTionsOption.range.first, interaKTionsOption.range.last)

                                for (choice in interaKTionsOption.choices) {
                                    when (choice) {
                                        is StringDiscordOptionReference.Choice.RawChoice -> choice(
                                            choice.name,
                                            choice.value
                                        )

                                        is StringDiscordOptionReference.Choice.LocalizedChoice -> {
                                            addChoices(
                                                Command.Choice(
                                                    choice.name.value,
                                                    choice.value.value
                                                ).apply {
                                                    setNameLocalizations(localeManager.getAll(choice.name))
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    is ChannelDiscordOptionReference -> {
                        return listOf(
                            Option<GuildChannel>(
                                interaKTionsOption.name,
                                description.value,
                                interaKTionsOption.required
                            ).apply {
                                setDescriptionLocalizations(localeManager.getAll(description))
                            }
                        )
                    }

                    is UserDiscordOptionReference -> {
                        return listOf(
                            Option<User>(
                                interaKTionsOption.name,
                                description.value,
                                interaKTionsOption.required
                            ).apply {
                                setDescriptionLocalizations(localeManager.getAll(description))
                            }
                        )
                    }

                    is RoleDiscordOptionReference -> {
                        return listOf(
                            Option<Role>(
                                interaKTionsOption.name,
                                description.value,
                                interaKTionsOption.required
                            ).apply {
                                setDescriptionLocalizations(localeManager.getAll(description))
                            }
                        )
                    }

                    is AttachmentDiscordOptionReference -> {
                        return listOf(
                            Option<Message.Attachment>(
                                interaKTionsOption.name,
                                description.value,
                                interaKTionsOption.required
                            ).apply {
                                setDescriptionLocalizations(localeManager.getAll(description))
                            }
                        )
                    }

                    is BooleanDiscordOptionReference -> {
                        return listOf(
                            Option<Boolean>(
                                interaKTionsOption.name,
                                description.value,
                                interaKTionsOption.required
                            ).apply {
                                setDescriptionLocalizations(localeManager.getAll(description))
                            }
                        )
                    }
                }
            }

            is ImageReferenceDiscordOptionReference -> {
                return listOf(
                    Option<String>(
                        interaKTionsOption.name,
                        "User, URL or Emoji",
                        true
                    )
                )
            }

            is ImageReferenceOrAttachmentDiscordOptionReference -> {
                return listOf(
                    Option<String>(
                        interaKTionsOption.name + "_data",
                        "User, URL or Emoji",
                        false
                    ),
                    Option<Message.Attachment>(
                        interaKTionsOption.name + "_attachment",
                        "Image Attachment",
                        false
                    )
                )
            }
        }
    }

    private fun Map<Locale, String>.mapKeysToJDALocales() = this.mapKeys {
        val language = it.key.language
        val country = it.key.country
        var locale = language
        if (country != null)
            locale += "-$country"

        DiscordLocale.from(locale)
    }

    // Used for the commandPathToDeclarations
    data class CommandDeclarationPair(
        val rootDeclaration: SlashCommandDeclaration,
        val slashDeclaration: SlashCommandDeclaration
    )

    private fun updateCommands(guildId: Long, action: (List<CommandData>) -> (List<Command>)): List<DiscordCommand> {
        logger.info { "Updating slash command on guild $guildId..." }

        val applicationCommands =
            slashCommands.map { convertDeclarationToJDA(it) } + userCommands.map {
                convertDeclarationToJDA(it)
            } + messageCommands.map { convertDeclarationToJDA(it) }
        logger.info { "Successfully converted all application command declarations to JDA! Total commands: ${applicationCommands.size}" }
        Command.Type.entries.filter { it != Command.Type.UNKNOWN }.forEach { type ->
            logger.info { "Command Type ${type.name}: ${applicationCommands.filter { type == it.type }.size} commands" }
        }

        while (true) {
            val lockId = "interaktions-unleashed-application-command-updater-$guildId".hashCode()

            try {
                val updatedCommands = action.invoke(applicationCommands)
                val updatedCommandsData = updatedCommands.map {
                    DiscordCommand.from(it)
                }

                return updatedCommandsData
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while trying to update slash commands! Retrying... Lock: $lockId" }
            }
        }
    }
}