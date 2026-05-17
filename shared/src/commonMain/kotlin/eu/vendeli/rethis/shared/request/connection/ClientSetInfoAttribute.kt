package eu.vendeli.rethis.shared.request.connection

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class ClientSetInfoAttribute {
    @RedisOption.Token("LIB-NAME")
    class LibName(@RedisOption.Name("libname") val name: String) : ClientSetInfoAttribute()

    @RedisOption.Token("LIB-VER")
    class LibVer(@RedisOption.Name("libver") val version: String) : ClientSetInfoAttribute()
}
