package us.timinc.mc.cobblemon.counter.api

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.entity.player.PlayerEntity
import us.timinc.mc.cobblemon.counter.store.CaptureCount
import us.timinc.mc.cobblemon.counter.store.CaptureStreak
import us.timinc.mc.cobblemon.counter.store.PokemonIdentifier

object CaptureApi {
    fun getTotal(player: PlayerEntity): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount).total()
    }

    fun getCount(player: PlayerEntity, pokemonId: PokemonIdentifier): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount).get(
            pokemonId
        )
    }

    fun getStreak(player: PlayerEntity): Pair<PokemonIdentifier, Int> {
        val playerData = Cobblemon.playerData.get(player)
        val captureStreakData = (playerData.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak)
        return Pair(captureStreakData.pokemonId, captureStreakData.count)
    }

    fun resetCount(player: PlayerEntity) {
        val playerData = Cobblemon.playerData.get(player)
        val captureCountData = (playerData.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount)
        captureCountData.reset()
    }

    fun resetStreak(player: PlayerEntity) {
        val playerData = Cobblemon.playerData.get(player)
        val captureStreakData = (playerData.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak)
        captureStreakData.reset()
    }

    fun reset(player: PlayerEntity) {
        resetCount(player)
        resetStreak(player)
    }
}