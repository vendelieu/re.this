package eu.vendeli.rethis.shared.request.server

sealed class TrimSlotsOption {
    class SlotRange(val startSlot: Long, val endSlot: Long) : TrimSlotsOption()

    class Other(vararg val args: String) : TrimSlotsOption()
}
