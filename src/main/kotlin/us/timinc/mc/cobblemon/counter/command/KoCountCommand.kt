package us.timinc.mc.cobblemon.counter.command

import com.cobblemon.mod.common.command.argument.PokemonArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import us.timinc.mc.cobblemon.counter.api.KoApi

object KoCountCommand {
    fun withPlayer(ctx: CommandContext<ServerCommandSource>): Int {
        return check(
            ctx,
            EntityArgumentType.getPlayer(ctx, "player"),
            PokemonArgumentType.getPokemon(ctx, "species").name.lowercase()
        )
    }

    fun withoutPlayer(ctx: CommandContext<ServerCommandSource>): Int {
        return ctx.source.player?.let { player ->
            check(
                ctx,
                player,
                PokemonArgumentType.getPokemon(ctx, "species").name.lowercase()
            )
        } ?: 0
    }

    private fun check(ctx: CommandContext<ServerCommandSource>, player: PlayerEntity, species: String): Int {
        val score = KoApi.getCount(player, species)
        ctx.source.sendMessage(Text.translatable("counter.ko.count", player.displayName, score, species))
        return score
    }
}