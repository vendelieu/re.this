package eu.vendeli.rethis.types.options

sealed class TrimmingStrategy : XAddOption()
data object MAXLEN : TrimmingStrategy()
data object MINID : TrimmingStrategy()

sealed class Exactement : XAddOption()
data object Equal : Exactement()
data object AlmostEqual : Exactement()

sealed class XAddOption {
    data object NOMKSTREAM : XAddOption()

    sealed class ID : XAddOption()
    data class Id(
        val id: String,
    ) : ID() {
        override fun toString(): String = id
    }

    data object Asterisk : ID()
}
