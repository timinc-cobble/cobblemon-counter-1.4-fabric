package us.timinc.mc.cobblemon.counter.command

import com.cobblemon.mod.common.command.argument.PokemonArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import us.timinc.mc.cobblemon.counter.api.KoApi
import us.timinc.mc.cobblemon.counter.store.PokemonIdentifier

object KoCountCommand {
    fun withPlayer(ctx: CommandContext<ServerCommandSource>): Int {
        val pokemonId = Commands.getPokemonIdFromContext(ctx) ?: throw Commands.NO_SPECIES_EXCEPTION.create()
        return check(
            ctx,
            EntityArgumentType.getPlayer(ctx, "player"),
            pokemonId
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
        ctx: CommandContext<ServerCommandSource>,
        player: PlayerEntity,
        pokemonId: PokemonIdentifier
    ): Int {
        val score = KoApi.getCount(player, pokemonId)
        ctx.source.sendMessage(
            Text.translatable(
                "counter.ko.count",
                player.displayName,
                score,
                pokemonId.species,
                pokemonId.form
            )
        )
        return score
    }
}