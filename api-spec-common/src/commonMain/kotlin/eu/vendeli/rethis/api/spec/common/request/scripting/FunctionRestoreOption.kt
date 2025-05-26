package eu.vendeli.rethis.api.spec.common.request.scripting

sealed class FunctionRestoreOption {
    data object FLUSH : FunctionRestoreOption()
    data object APPEND : FunctionRestoreOption()
    data object REPLACE : FunctionRestoreOption()
}
