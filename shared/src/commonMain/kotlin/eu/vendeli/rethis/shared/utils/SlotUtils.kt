package eu.vendeli.rethis.shared.utils

import eu.vendeli.rethis.shared.types.CrossSlotOperationException

fun validateSlot(prev: Int?, next: Int): Int = when {
    prev == null -> next
    prev != next -> throw CrossSlotOperationException("Cross‐slot operations are not supported")
    else -> prev
}
