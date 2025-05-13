package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption
enum class ClusterResetMode { HARD, SOFT }
