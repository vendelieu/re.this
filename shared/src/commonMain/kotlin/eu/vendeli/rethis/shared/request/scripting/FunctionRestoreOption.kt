package eu.vendeli.rethis.shared.request.scripting

sealed class FunctionRestoreOption {
    data object FLUSH : FunctionRestoreOption()
    data object APPEND : FunctionRestoreOption()
    data object REPLACE : FunctionRestoreOption()
}
