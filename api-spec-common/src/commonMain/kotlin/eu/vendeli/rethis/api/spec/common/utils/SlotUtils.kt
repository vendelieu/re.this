package eu.vendeli.rethis.api.spec.common.utils

import eu.vendeli.rethis.api.spec.common.types.CrossSlotOperationException

inline fun validateSlot(prev: Int?, next: Int): Int = when {
    prev == null -> next
    prev != next -> throw CrossSlotOperationException("Cross‐slot operations are not supported")
    else -> prev
}
