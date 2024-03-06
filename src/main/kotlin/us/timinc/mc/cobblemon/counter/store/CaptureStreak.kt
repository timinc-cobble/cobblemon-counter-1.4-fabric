package us.timinc.mc.cobblemon.counter.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonObject

class CaptureStreak : PlayerDataExtension {
    companion object {
        const val NAME = "captureStreak"
    }

    var pokemonId = PokemonIdentifier("", "")
    var count = 0

    fun reset() {
        pokemonId = PokemonIdentifier("", "")
        count = 0
    }

    fun add(identifier: PokemonIdentifier) {
        if (identifier == pokemonId) {
            count++
        } else {
            pokemonId = identifier
            count = 1
        }
    }

    fun get(identifier: PokemonIdentifier): Int {
        if (identifier == pokemonId) {
            return count
        }
        return 0
    }

    override fun deserialize(json: JsonObject): CaptureStreak {
        val species = json.get("species").asString
        val form = if (json.has("form")) {
            json.get("form").asString
        } else ""
        pokemonId = PokemonIdentifier(species, form)
        count = json.get("count").asInt

        return this
    }

    override fun name(): String {
        return NAME
    }

    override fun serialize(): JsonObject {
        val json = JsonObject()

        json.addProperty("name", NAME)
        json.addProperty("species", pokemonId.species)
        json.addProperty("form", pokemonId.form)
        json.addProperty("count", count)

        return json
    }
}