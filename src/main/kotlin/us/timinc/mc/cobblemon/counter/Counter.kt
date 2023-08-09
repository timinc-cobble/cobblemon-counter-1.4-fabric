package us.timinc.mc.cobblemon.counter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.cobblemon.mod.common.util.getPlayer
import net.fabricmc.api.ModInitializer
import net.minecraft.world.entity.player.Player
import us.timinc.mc.cobblemon.counter.store.CaptureCount
import us.timinc.mc.cobblemon.counter.store.CaptureStreak
import us.timinc.mc.cobblemon.counter.store.KoCount
import us.timinc.mc.cobblemon.counter.store.KoStreak
import java.util.*

object Counter : ModInitializer {
    const val MOD_ID = "cobbled_counter"

    override fun onInitialize() {
        PlayerDataExtensionRegistry.register(KoCount.NAME, KoCount::class.java)
        PlayerDataExtensionRegistry.register(KoStreak.NAME, KoStreak::class.java)
        PlayerDataExtensionRegistry.register(CaptureCount.NAME, CaptureCount::class.java)
        PlayerDataExtensionRegistry.register(CaptureStreak.NAME, CaptureStreak::class.java)

        CobblemonEvents.POKEMON_CAPTURED.subscribe { handlePokemonCapture(it) }
        CobblemonEvents.BATTLE_VICTORY.subscribe { handleWildDefeat(it) }
    }

    private fun handlePokemonCapture(event: PokemonCapturedEvent) {
        val species = event.pokemon.species.name.lowercase()

        val data = Cobblemon.playerData.get(event.player)

        val captureCount: CaptureCount =
            data.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount
        captureCount.add(species)

        val captureStreak: CaptureStreak =
            data.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak
        captureStreak.add(species)

        Cobblemon.playerData.saveSingle(data)
    }

    private fun handleWildDefeat(battleVictoryEvent: BattleVictoryEvent) {
        val wildPokemons = battleVictoryEvent.battle.actors.flatMap { it.pokemonList }.map { it.originalPokemon }
            .filter { !it.isPlayerOwned() }

        battleVictoryEvent.winners
            .flatMap { it.getPlayerUUIDs().mapNotNull(UUID::getPlayer) }
            .forEach { player ->
                val data = Cobblemon.playerData.get(player)

                val koCount: KoCount =
                    data.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount
                val koStreak: KoStreak = data.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak

                wildPokemons.forEach { wildPokemon ->
                    val species = wildPokemon.species.name.lowercase()

                    koCount.add(species)
                    koStreak.add(species)
                }

                Cobblemon.playerData.saveSingle(data)
            }
    }

    fun getPlayerKoStreak(player: Player, species: String): Int {
        val data = Cobblemon.playerData.get(player)
        return (data.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak).get(species)
    }

    fun getPlayerKoCount(player: Player, species: String): Int {
        val data = Cobblemon.playerData.get(player)
        return (data.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount).get(species)
    }

    fun getPlayerCaptureStreak(player: Player, species: String): Int {
        val data = Cobblemon.playerData.get(player)
        return (data.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak).get(species)
    }

    fun getPlayerCaptureCount(player: Player, species: String): Int {
        val data = Cobblemon.playerData.get(player)
        return (data.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount).get(species)
    }
}