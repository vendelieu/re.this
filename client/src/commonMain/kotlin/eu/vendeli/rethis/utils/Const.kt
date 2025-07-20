package eu.vendeli.rethis.utils

import io.ktor.utils.io.core.*

const val CLIENT_NAME: String = "Re.This"
const val DEFAULT_HOST: String = "127.0.0.1"
const val DEFAULT_PORT: Int = 6379

val EOL: ByteArray = "\r\n".toByteArray()

const val REDIS_JSON_ROOT_PATH = "$"
