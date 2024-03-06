package us.timinc.mc.cobblemon.counter.command

import com.cobblemon.mod.common.api.text.red
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import us.timinc.mc.cobblemon.counter.store.PokemonIdentifier

object Commands {
    const val PROPERTIES = "properties"
    val NO_SPECIES_EXCEPTION = SimpleCommandExceptionType(Text.literal("Invalid species!").red())
    fun getPokemonIdFromContext(ctx: CommandContext<ServerCommandSource>): PokemonIdentifier? {
        val properties = PokemonPropertiesArgumentType.getPokemonProperties(ctx, "properties")
        if (properties.species == null) {
            return null
        }
        return PokemonIdentifier(properties.species!!, properties.form ?: "")
    }
}