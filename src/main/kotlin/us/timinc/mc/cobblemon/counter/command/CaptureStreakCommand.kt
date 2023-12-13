package us.timinc.mc.cobblemon.counter.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import us.timinc.mc.cobblemon.counter.api.CaptureApi

object CaptureStreakCommand {
    fun withPlayer(ctx: CommandContext<ServerCommandSource>): Int {
        return check(
            ctx,
            EntityArgumentType.getPlayer(ctx, "player"),
        )
    }

    fun withoutPlayer(ctx: CommandContext<ServerCommandSource>): Int {
        return ctx.source.player?.let { player ->
            check(
                ctx,
                player
            )
        } ?: 0
    }

    private fun check(ctx: CommandContext<ServerCommandSource>, player: PlayerEntity): Int {
        val streakData = CaptureApi.getStreak(player)
        val species = streakData.first
        val count = streakData.second
        ctx.source.sendMessage(Text.translatable("counter.capture.streak", player.displayName, count, species))
        return count
    }
}