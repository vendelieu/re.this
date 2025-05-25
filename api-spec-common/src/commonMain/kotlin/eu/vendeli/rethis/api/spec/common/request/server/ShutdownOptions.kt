package eu.vendeli.rethis.api.spec.common.request.server

sealed class ShutdownOptions {
    data object NOW : ShutdownOptions()
    data object FORCE : ShutdownOptions()
    data object ABORT : ShutdownOptions()
}

sealed class SaveSelector {
    data object SAVE : SaveSelector()
    data object NOSAVE : SaveSelector()
}
