package eu.vendeli.rethis.types.core

fun interface ReThisExceptionHandler {
    suspend fun handle(ex: Exception)
}
