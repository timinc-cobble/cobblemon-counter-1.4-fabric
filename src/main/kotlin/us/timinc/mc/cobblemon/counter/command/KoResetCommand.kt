package us.timinc.mc.cobblemon.counter.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import us.timinc.mc.cobblemon.counter.api.CaptureApi

object KoResetCommand {
    fun resetCount(ctx: CommandContext<ServerCommandSource>): Int {
        val target = EntityArgumentType.getPlayer(ctx, "player")
        CaptureApi.resetCount(target)
        ctx.source.sendMessage(Text.translatable("counter.capture.count.reset", target.name))
        return 0
    }

    fun resetStreak(ctx: CommandContext<ServerCommandSource>): Int {
        val target = EntityArgumentType.getPlayer(ctx, "player")
        CaptureApi.resetStreak(target)
        ctx.source.sendMessage(Text.translatable("counter.capture.streak.reset", target.name))
        return 0
    }

    fun reset(ctx: CommandContext<ServerCommandSource>): Int {
        val target = EntityArgumentType.getPlayer(ctx, "player")
        CaptureApi.reset(target)
        ctx.source.sendMessage(Text.translatable("counter.capture.all.reset", target.name))
        return 0
    }
}