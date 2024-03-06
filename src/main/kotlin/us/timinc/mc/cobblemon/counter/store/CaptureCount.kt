package us.timinc.mc.cobblemon.counter.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonObject
import us.timinc.mc.cobblemon.counter.Counter

class CaptureCount : PlayerDataExtension {
    companion object {
        const val NAME = "captureCount"
    }

    private val captureCounts = mutableMapOf<PokemonIdentifier, Int>()

    fun reset() {
        captureCounts.clear()
    }

    fun add(pokemonIdentifier: PokemonIdentifier) {
        captureCounts[pokemonIdentifier] =
            get(pokemonIdentifier) + 1
    }

    fun get(pokemonIdentifier: PokemonIdentifier): Int {
        return captureCounts.getOrDefault(pokemonIdentifier, 0)
    }

    fun total(): Int {
        return captureCounts.values.reduceOrNull { acc, i -> acc + i } ?: 0
    }

    override fun deserialize(json: JsonObject): CaptureCount {
        val defeatsData = json.getAsJsonObject("defeats")
        for (jsonKey in defeatsData.keySet()) {
            val pokemonId = PokemonIdentifier.fromJsonKey(jsonKey)
            captureCounts[pokemonId] = defeatsData.get(jsonKey).asInt
        }

        return this
    }

    override fun name(): String {
        return NAME
    }

    // "name": "captureCount",
    // "defeats": {
    //      "sneasel": 7,
    //      "sneasel+hisuian": 2
    // }

    override fun serialize(): JsonObject {
        val json = JsonObject()
        json.addProperty("name", NAME)

        val defeatsData = JsonObject()
        for (pokemonId in captureCounts.keys) {
            defeatsData.addProperty(pokemonId.jsonKey(), captureCounts[pokemonId])
        }
        json.add("defeats", defeatsData)

        return json
    }
}