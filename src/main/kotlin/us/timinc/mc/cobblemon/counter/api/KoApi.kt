package us.timinc.mc.cobblemon.counter.api

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.entity.player.PlayerEntity
import us.timinc.mc.cobblemon.counter.store.KoCount
import us.timinc.mc.cobblemon.counter.store.KoStreak

object KoApi {
    fun getTotal(player: PlayerEntity): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount).total()
    }

    fun getCount(player: PlayerEntity, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount).get(species)
    }

    fun getStreak(player: PlayerEntity): Pair<String, Int> {
        val playerData = Cobblemon.playerData.get(player)
        val koStreakData = (playerData.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak)
        return Pair(koStreakData.species, koStreakData.count)
    }

    fun resetCount(player: PlayerEntity) {
        val playerData = Cobblemon.playerData.get(player)
        val koCountData = (playerData.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount)
        koCountData.reset()
    }

    fun resetStreak(player: PlayerEntity) {
        val playerData = Cobblemon.playerData.get(player)
        val koStreakData = (playerData.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak)
        koStreakData.reset()
    }

    fun reset(player: PlayerEntity) {
        resetCount(player)
        resetStreak(player)
    }
}