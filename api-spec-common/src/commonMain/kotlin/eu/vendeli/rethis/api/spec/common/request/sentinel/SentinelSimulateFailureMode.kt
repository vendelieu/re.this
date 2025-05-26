package eu.vendeli.rethis.api.spec.common.request.sentinel

enum class SentinelSimulateFailureMode {
    CRASH_AFTER_ELECTION,
    CRASH_AFTER_PROMOTION,
    HELP
}
