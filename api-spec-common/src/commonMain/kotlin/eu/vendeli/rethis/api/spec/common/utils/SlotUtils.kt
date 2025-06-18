package eu.vendeli.rethis.api.spec.common.utils

inline fun validateSlot(prev: Int?, next: Int): Int = when {
    prev == null -> next
    prev != next -> throw IllegalArgumentException("Cross‐slot operations are not supported")
    else -> prev
}
