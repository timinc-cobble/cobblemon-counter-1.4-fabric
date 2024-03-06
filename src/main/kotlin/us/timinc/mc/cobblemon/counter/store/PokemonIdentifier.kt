package us.timinc.mc.cobblemon.counter.store

class PokemonIdentifier(val species: String, form: String) {
    // Filter out "Normal" form to "".
    val form: String = if (form.lowercase() == "normal") "" else form

    companion object {
        fun fromJsonKey(key: String): PokemonIdentifier {
            val strings = key.split('+', limit = 2)
            return if (strings.size == 1) PokemonIdentifier(strings[0].replaceFirstChar { it.uppercaseChar() }, "")
            else PokemonIdentifier(
                    strings[0].replaceFirstChar { it.uppercaseChar() },
                    strings[1].replaceFirstChar { it.uppercaseChar() })
        }
    }

    fun jsonKey(): String {
        return "${species.lowercase()}+${form.lowercase()}"
    }
}