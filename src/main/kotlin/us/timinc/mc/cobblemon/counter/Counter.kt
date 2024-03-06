package us.timinc.mc.cobblemon.counter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.util.getPlayer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import us.timinc.mc.cobblemon.counter.api.CaptureApi
import us.timinc.mc.cobblemon.counter.command.*
import us.timinc.mc.cobblemon.counter.config.CounterConfig
import us.timinc.mc.cobblemon.counter.store.*
import java.util.*

object Counter : ModInitializer {
    @Suppress("unused")
    const val MOD_ID = "cobbled_counter"

    private var logger: Logger = LogManager.getLogger(MOD_ID)
    private var config: CounterConfig = CounterConfig.Builder.load()

    override fun onInitialize() {
        PlayerDataExtensionRegistry.register(KoCount.NAME, KoCount::class.java)
        PlayerDataExtensionRegistry.register(KoStreak.NAME, KoStreak::class.java)
        PlayerDataExtensionRegistry.register(CaptureCount.NAME, CaptureCount::class.java)
        PlayerDataExtensionRegistry.register(CaptureStreak.NAME, CaptureStreak::class.java)

        CobblemonEvents.POKEMON_CAPTURED.subscribe { handlePokemonCapture(it) }
        CobblemonEvents.BATTLE_FAINTED.subscribe { handleWildDefeat(it) }
        CobblemonEvents.POKEMON_CATCH_RATE.subscribe { repeatBallBooster(it) }
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(literal("counter").then(literal("ko").then(literal("count").then(argument(
                Commands.PROPERTIES, PokemonPropertiesArgumentType.properties()
            ).then(argument(
                "player", EntityArgumentType.player()
            ).executes { KoCountCommand.withPlayer(it) }).executes { KoCountCommand.withoutPlayer(it) })
            ).then(literal("streak").then(argument(
                    "player", EntityArgumentType.player()
                ).executes { KoStreakCommand.withPlayer(it) }).executes { KoStreakCommand.withoutPlayer(it) }

                ).then(literal("total").then(argument(
                    "player", EntityArgumentType.player()
                ).executes { KoTotalCommand.withPlayer(it) }).executes { KoTotalCommand.withoutPlayer(it) })
                .then(literal("reset").requires { source -> source.hasPermissionLevel(2) }.then(
                        literal("count").then(argument(
                            "player", EntityArgumentType.player()
                        ).executes { KoResetCommand.resetCount(it) })
                    ).then(
                        literal("streak").then(argument(
                            "player", EntityArgumentType.player()
                        ).executes { KoResetCommand.resetStreak(it) })
                    ).then(
                        literal("all").then(argument(
                            "player", EntityArgumentType.player()
                        ).executes { KoResetCommand.reset(it) })
                    )
                )
            ).then(literal("capture").then(literal("count").then(argument(
                    Commands.PROPERTIES, PokemonPropertiesArgumentType.properties()
                ).then(argument(
                    "player", EntityArgumentType.player()
                ).executes { CaptureCountCommand.withPlayer(it) }).executes { CaptureCountCommand.withoutPlayer(it) })
                ).then(literal("streak").then(argument(
                    "player", EntityArgumentType.player()
                ).executes { CaptureStreakCommand.withPlayer(it) }).executes { CaptureStreakCommand.withoutPlayer(it) })
                    .then(literal("total").then(argument(
                        "player", EntityArgumentType.player()
                    ).executes { CaptureTotalCommand.withPlayer(it) })
                        .executes { CaptureTotalCommand.withoutPlayer(it) })
                    .then(literal("reset").requires { source -> source.hasPermissionLevel(2) }.then(
                            literal("count").then(argument(
                                "player", EntityArgumentType.player()
                            ).executes { CaptureResetCommand.resetCount(it) })
                        ).then(
                            literal("streak").then(argument(
                                "player", EntityArgumentType.player()
                            ).executes { CaptureResetCommand.resetStreak(it) })
                        ).then(
                            literal("all").then(argument(
                                "player", EntityArgumentType.player()
                            ).executes { CaptureResetCommand.reset(it) })
                        )
                    )
                )
            )
        }
    }

    private fun repeatBallBooster(event: PokemonCatchRateEvent) {
        val thrower = event.thrower

        if (thrower !is ServerPlayerEntity) return

        val pokemon = event.pokemonEntity.pokemon

        if (event.pokeBallEntity.pokeBall == PokeBalls.REPEAT_BALL && CaptureApi.getCount(
                thrower, PokemonIdentifier(pokemon.species.name, pokemon.form.name)
            ) > 0
        ) {
            event.catchRate *= 2.5f
        }
    }

    private fun handlePokemonCapture(event: PokemonCapturedEvent) {
        val pokemonId = PokemonIdentifier(event.pokemon.species.name, event.pokemon.form.name)

        val data = Cobblemon.playerData.get(event.player)

        val captureCount: CaptureCount = data.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount
        captureCount.add(pokemonId)

        val captureStreak: CaptureStreak =
            data.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak
        captureStreak.add(pokemonId)

        info(
            "Player ${event.player.displayName.string} captured a $pokemonId streak(${captureStreak.count}) count(${
                captureCount.get(
                    pokemonId
                )
            })"
        )
        if (config.broadcastCapturesToPlayer) {
            event.player.sendMessage(
                Text.translatable(
                    "counter.capture.confirm",
                    pokemonId.species,
                    pokemonId.form,
                    captureCount.get(pokemonId),
                    captureStreak.count
                )
            )
        }

        Cobblemon.playerData.saveSingle(data)
    }

    private fun handleWildDefeat(event: BattleFaintedEvent) {
        val targetEntity = event.killed.entity ?: return
        val targetPokemon = targetEntity.pokemon
        if (!targetPokemon.isWild()) {
            return
        }
        val pokemonId = PokemonIdentifier(targetPokemon.species.name, targetPokemon.form.name)

        event.battle.playerUUIDs.mapNotNull(UUID::getPlayer).forEach { player ->
            val data = Cobblemon.playerData.get(player)
            val koCount: KoCount = data.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount
            val koStreak: KoStreak = data.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak

            koCount.add(pokemonId)
            koStreak.add(pokemonId)

            info(
                "Player ${player.displayName.string} KO'd a $pokemonId streak(${koStreak.count}) count(${
                    koCount.get(
                        pokemonId
                    )
                })"
            )
            if (config.broadcastKosToPlayer) {
                player.sendMessage(
                    Text.translatable(
                        "counter.ko.confirm", pokemonId.species, pokemonId.form, koCount.get(pokemonId), koStreak.count
                    )
                )
            }

            Cobblemon.playerData.saveSingle(data)
        }
    }

    private fun info(msg: String) {
        if (!config.debug) return
        logger.info(msg)
    }
}