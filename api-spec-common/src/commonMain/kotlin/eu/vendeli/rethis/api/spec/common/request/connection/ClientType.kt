package eu.vendeli.rethis.api.spec.common.request.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption.Token("TYPE")
enum class ClientType { NORMAL, MASTER, REPLICA, PUBSUB }
