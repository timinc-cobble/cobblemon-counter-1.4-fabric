package us.timinc.mc.cobblemon.counter.store

import com.cobblemon.mod.common.api.storage.player.PlayerDataExtension
import com.google.gson.JsonObject

class KoCount : PlayerDataExtension {
    companion object {
        const val NAME = "koCount"
    }

    private val koCounts = mutableMapOf<PokemonIdentifier, Int>()

    fun reset() {
        koCounts.clear()
    }

    fun add(identifier: PokemonIdentifier) {
        koCounts[identifier] = get(identifier) + 1
    }

    fun get(identifier: PokemonIdentifier): Int {
        return koCounts.getOrDefault(identifier, 0)
    }

    fun total(): Int {
        return koCounts.values.reduceOrNull { acc, i -> acc + i } ?: 0
    }

    override fun deserialize(json: JsonObject): KoCount {
        val defeatsData = json.getAsJsonObject("defeats")
        for (jsonKey in defeatsData.keySet()) {
            val pokemonId = PokemonIdentifier.fromJsonKey(jsonKey)
            koCounts[pokemonId] = defeatsData.get(jsonKey).asInt
        }

        return this
    }

    override fun name(): String {
        return NAME
    }

    override fun serialize(): JsonObject {
        val json = JsonObject()
        json.addProperty("name", NAME)

        val defeatsData = JsonObject()
        for (identifier in koCounts.keys) {
            val key = identifier.jsonKey()
            defeatsData.addProperty(key, koCounts[identifier])
        }
        json.add("defeats", defeatsData)

        return json
    }
}