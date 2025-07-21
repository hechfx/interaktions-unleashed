<p align="center">
<img src="https://cdn.discordapp.com/attachments/1082340413156892682/1387084709799923742/card.png?ex=687fa74d&is=687e55cd&hm=61c70b792f6c6799c2908336860425ba445944134e3d4faa1ac5759fc1e9f3ca&" width="256" height="256">
</p>

<p align="center">

<h1 align="center">📦 Discord InteraKTions Unleashed</h1>

</p>

<p align="center">
<a href="LICENSE"><img src="https://img.shields.io/badge/license-AGPL%20v3-blue.svg"></a>
</p>

**A lib that makes life easier while creating Discord commands, with full support for Slash Commands and legacy message commands! Extracted from [LorittaBot](https://github.com/LorittaBot/Loritta).**

A demonstration bot is available in the module `demo-bot`!

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    implementation("me.hechfx:interaktions-unleashed:1.0.0")
}
```

## 🚀 Usage

```kotlin
class Bot(val token: String) {
  val jda by lazy {
    JDABuilder.createLight(token)
      .setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
      .build()
  }

  val commandManager = UnleashedCommandManager(jda, localePath = "/locales", registerGlobally = true, guildsToRegister = listOf(123L))
    .addSupportForLocale(
        DiscordLocale.ENGLISH_US,
        DiscordLocale.PORTUGUESE_BRAZILIAN
    )

  fun start() {
    commandManager.register(PingCommand())
    
    jda.awaitReady()
  }

  class PingCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(LocalizedString("ping", "commands.command.ping.name"), LocalizedString("ping command description", "commands.command.ping.description")) {
      executor = PingExecutor()
    }

    inner class PingExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
      override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.reply(false) {
                content = "Pong!"
        }
      }

      override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }
  }
}
```
For full awareness that you can do with this, you can look at the `demo-bot` module.

## 🌐 Locale Support (translations)

You can load translations from JSON in your resources:

```json
{
  "commands.command.ping.name": "ping",
  "commands.command.ping.description": "Replies with pong!"
}
```
