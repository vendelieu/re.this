package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

@RedisOptionContainer
sealed class UpdateStrategyOption {
    @RedisOptionContainer
    sealed class ExistenceRule : UpdateStrategyOption()
    data object NX : ExistenceRule()
    data object XX : ExistenceRule()

    @RedisOptionContainer
    sealed class ComparisonRule : UpdateStrategyOption()
    data object GT : ComparisonRule()
    data object LT : ComparisonRule()
}
