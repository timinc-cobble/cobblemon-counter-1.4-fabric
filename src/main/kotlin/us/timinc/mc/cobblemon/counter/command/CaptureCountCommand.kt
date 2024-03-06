package us.timinc.mc.cobblemon.counter.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import us.timinc.mc.cobblemon.counter.api.CaptureApi
import us.timinc.mc.cobblemon.counter.store.PokemonIdentifier

object CaptureCountCommand {
    fun withPlayer(ctx: CommandContext<ServerCommandSource>): Int {
        val pokemonId = Commands.getPokemonIdFromContext(ctx) ?: throw Commands.NO_SPECIES_EXCEPTION.create()
        return check(
            ctx, EntityArgumentType.getPlayer(ctx, "player"), pokemonId
        )
    }

    fun withoutPlayer(ctx: CommandContext<ServerCommandSource>): Int {
        val pokemonId = Commands.getPokemonIdFromContext(ctx) ?: throw Commands.NO_SPECIES_EXCEPTION.create()
        return ctx.source.player?.let { player ->
            check(
                ctx,
                player,
                pokemonId
            )
        } ?: 0
    }

    private fun check(
        ctx: CommandContext<ServerCommandSource>, player: PlayerEntity, pokemonIdentifier: PokemonIdentifier
    ): Int {
        val score = CaptureApi.getCount(player, pokemonIdentifier)
        ctx.source.sendMessage(
            Text.translatable(
                "counter.capture.count", player.displayName, score, pokemonIdentifier.species, pokemonIdentifier.form
            )
        )
        return score
    }
}