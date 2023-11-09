package us.timinc.mc.cobblemon.counter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.cobblemon.mod.common.util.getPlayer
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.entity.player.PlayerEntity
import us.timinc.mc.cobblemon.counter.store.CaptureCount
import us.timinc.mc.cobblemon.counter.store.CaptureStreak
import us.timinc.mc.cobblemon.counter.store.KoCount
import us.timinc.mc.cobblemon.counter.store.KoStreak
import java.util.*
import net.minecraft.server.command.CommandManager.*
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import us.timinc.mc.cobblemon.counter.config.CounterConfig

object Counter : ModInitializer {
    @Suppress("unused")
    const val MOD_ID = "cobbled_counter"

    private var logger: Logger = LogManager.getLogger(MOD_ID)
    private var config : CounterConfig = CounterConfig.Builder.load()

    override fun onInitialize() {
        PlayerDataExtensionRegistry.register(KoCount.NAME, KoCount::class.java)
        PlayerDataExtensionRegistry.register(KoStreak.NAME, KoStreak::class.java)
        PlayerDataExtensionRegistry.register(CaptureCount.NAME, CaptureCount::class.java)
        PlayerDataExtensionRegistry.register(CaptureStreak.NAME, CaptureStreak::class.java)

        CobblemonEvents.POKEMON_CAPTURED.subscribe { handlePokemonCapture(it) }
        CobblemonEvents.BATTLE_FAINTED.subscribe { handleWildDefeat(it) }
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("counter")
                    .then(
                        literal("ko")
                            .then(
                                literal("count")
                                    .then(
                                        argument("species", StringArgumentType.greedyString())
                                            .executes { checkKoCount(it) }
                                    )
                            )
                            .then(
                                literal("streak")
                                    .executes { checkKoStreak(it) }
                            )
                    )
                    .then(
                        literal("capture")
                            .then(
                                literal("count")
                                    .then(
                                        argument("species", StringArgumentType.greedyString())
                                            .executes { checkCaptureCount(it) }
                                    )
                            )
                            .then(
                                literal("streak")
                                    .executes { checkCaptureStreak(it) }
                            )
                    )
            )
        }
    }

    private fun checkKoCount(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player ?: return 0
        val species = StringArgumentType.getString(ctx, "species")
        val score = getPlayerKoCount(player, species)
        ctx.source.sendMessage(Text.translatable("counter.ko.count", score, species))
        return Command.SINGLE_SUCCESS
    }

    private fun checkKoStreak(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player ?: return 0
        val streakData = getPlayerKoStreak(player)
        val species = streakData.first
        val score = streakData.second
        ctx.source.sendMessage(Text.translatable("counter.ko.streak", score, species))
        return Command.SINGLE_SUCCESS
    }

    private fun checkCaptureCount(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player ?: return 0
        val species = StringArgumentType.getString(ctx, "species")
        val score = getPlayerCaptureCount(player, species)
        ctx.source.sendMessage(Text.translatable("counter.capture.count", score, species))
        return Command.SINGLE_SUCCESS
    }

    private fun checkCaptureStreak(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player ?: return 0
        val streakData = getPlayerCaptureStreak(player)
        val species = streakData.first
        val score = streakData.second
        ctx.source.sendMessage(Text.translatable("counter.capture.streak", score, species))
        return Command.SINGLE_SUCCESS
    }

    private fun handlePokemonCapture(event: PokemonCapturedEvent) {
        val species = event.pokemon.species.name.lowercase()

        val data = Cobblemon.playerData.get(event.player)

        val captureCount: CaptureCount = data.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount
        captureCount.add(species)

        val captureStreak: CaptureStreak =
            data.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak
        captureStreak.add(species)

        info("Player ${event.player.displayName.string} captured a $species streak(${captureStreak.count}) count(${captureCount.get(species)})")

        Cobblemon.playerData.saveSingle(data)
    }

    private fun handleWildDefeat(battleVictoryEvent: BattleFaintedEvent) {
        val targetEntity = battleVictoryEvent.killed.entity ?: return
        val targetPokemon = targetEntity.pokemon
        if (!targetPokemon.isWild()) {
            return
        }
        val species = targetPokemon.species.name.lowercase()

        battleVictoryEvent.battle.playerUUIDs.mapNotNull(UUID::getPlayer).forEach { player ->
            val data = Cobblemon.playerData.get(player)
            val koCount: KoCount = data.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount
            val koStreak: KoStreak = data.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak

            koCount.add(species)
            koStreak.add(species)

            info("Player ${player.displayName.string} KO'd a $species streak(${koStreak.count}) count(${koCount.get(species)})")

            Cobblemon.playerData.saveSingle(data)
        }
    }

    @Suppress("unused")
    fun getPlayerKoStreak(player: PlayerEntity, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak).get(species)
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getPlayerKoStreak(player: PlayerEntity): Pair<String, Int> {
        val playerData = Cobblemon.playerData.get(player)
        val koStreakData = (playerData.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak)
        return Pair(koStreakData.species, koStreakData.count)
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getPlayerKoCount(player: PlayerEntity, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount).get(species)
    }

    @Suppress("unused")
    fun getPlayerCaptureStreak(player: PlayerEntity, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak).get(species)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getPlayerCaptureStreak(player: PlayerEntity): Pair<String, Int> {
        val playerData = Cobblemon.playerData.get(player)
        val captureStreakData = (playerData.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak)
        return Pair(captureStreakData.species, captureStreakData.count)
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getPlayerCaptureCount(player: PlayerEntity, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount).get(species)
    }

    fun info(msg: String) {
        if (!config.debug) return
        logger.info(msg)
    }
}