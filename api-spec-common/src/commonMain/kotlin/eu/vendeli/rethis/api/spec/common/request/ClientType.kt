package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption.Name("TYPE")
enum class ClientType { NORMAL, MASTER, REPLICA, PUBSUB }
